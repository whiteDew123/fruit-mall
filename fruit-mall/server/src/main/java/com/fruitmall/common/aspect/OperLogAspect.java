package com.fruitmall.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.enums.OperLogResultEnum;
import com.fruitmall.system.domain.SysOperLog;
import com.fruitmall.system.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 操作日志切面：拦截标注 @OperLog 的方法，采集操作信息后异步落库。
 *
 * 两条硬约束：
 * 1）记录日志失败绝不阻塞业务主流程，因此采集与写入都包在 try-catch 中；
 * 2）请求参数里的密码类字段必须掩码后再落库，日志中不允许出现明文口令。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    /** 参数序列化后的最大长度，超出截断，避免超长内容撑大日志表 */
    private static final int MAX_PARAM_LENGTH = 2000;

    /** 目标方法与地址的最大长度，与 sys_oper_log 的列宽一致 */
    private static final int MAX_LENGTH = 200;

    /** 密码类字段掩码规则，命中后把值替换为 *** */
    private static final Pattern SENSITIVE_PATTERN =
            Pattern.compile("(\"(?:password|pwd|oldPassword|newPassword|confirmPassword)\"\\s*:\\s*)\"[^\"]*\"");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final ISysOperLogService sysOperLogService;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        Integer resultStatus = OperLogResultEnum.SUCCESS.getCode();
        String errorMsg = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            resultStatus = OperLogResultEnum.FAIL.getCode();
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                SysOperLog entity = buildLog(joinPoint, operLog, resultStatus, errorMsg,
                        System.currentTimeMillis() - start);
                sysOperLogService.saveLogAsync(entity);
            } catch (Exception e) {
                log.error("操作日志采集失败：module={}, action={}", operLog.module(), operLog.action(), e);
            }
        }
    }

    private SysOperLog buildLog(ProceedingJoinPoint joinPoint, OperLog operLog,
                                Integer resultStatus, String errorMsg, long costTime) {
        LocalDateTime now = LocalDateTime.now();
        SysOperLog entity = new SysOperLog();
        entity.setModule(operLog.module());
        entity.setAction(operLog.action());
        entity.setResultStatus(resultStatus);
        entity.setErrorMsg(truncate(errorMsg, MAX_PARAM_LENGTH));
        entity.setCostTime(costTime);
        entity.setOperTime(now);
        entity.setCreateTime(now);

        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            entity.setUserId(loginUser.getUserId());
            entity.setUsername(loginUser.getUsername());
        }

        String method = joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName();
        entity.setMethod(truncate(method, MAX_LENGTH));

        HttpServletRequest request = currentRequest();
        if (request != null) {
            entity.setRequestUrl(truncate(request.getRequestURI(), MAX_LENGTH));
            entity.setIp(clientIp(request));
        }
        entity.setRequestParam(buildParam(joinPoint));
        return entity;
    }

    /**
     * 序列化请求参数，跳过无法序列化的 Web 对象，并对密码类字段掩码。
     */
    private String buildParam(ProceedingJoinPoint joinPoint) {
        List<Object> args = new ArrayList<>();
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof InputStream) {
                continue;
            }
            args.add(arg);
        }
        if (args.isEmpty()) {
            return null;
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(args);
            return truncate(SENSITIVE_PATTERN.matcher(json).replaceAll("$1\"***\""), MAX_PARAM_LENGTH);
        } catch (Exception e) {
            // 参数无法序列化时退化为类名列表，不能因为日志采集影响业务
            return args.stream().map(arg -> arg.getClass().getSimpleName()).toList().toString();
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取来源 IP，优先取反向代理传递的真实 IP。
     */
    private String clientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return comma > 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
