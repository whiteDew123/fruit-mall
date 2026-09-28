package com.fruitmall.common.interceptor;

import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.constant.AuthConstant;
import com.fruitmall.common.enums.UserTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.JwtUtil;
import com.fruitmall.system.service.ISysMenuService;
import com.fruitmall.system.service.ISysRoleService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 登录态与功能权限校验拦截器。
 * 1）解析请求头中的 JWT，写入 UserContext；
 * 2）若目标方法（或类）标注了 @RequiresPermission，校验当前用户是否具备该权限标识，不足返回 403。
 * 数据归属校验（会员只能访问自己的订单等）在 Service 层完成，不在本类处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ISysRoleService sysRoleService;
    private final ISysMenuService sysMenuService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        Claims claims = jwtUtil.parse(token);
        Long userId = parseUserId(claims);
        String username = claims.get(AuthConstant.CLAIM_USERNAME, String.class);
        Integer userTypeCode = claims.get(AuthConstant.CLAIM_USER_TYPE, Integer.class);
        UserTypeEnum userType = parseUserType(userTypeCode);

        // 后台用户加载角色与权限标识；会员端数据归属在 Service 层校验，这里不带权限
        List<String> roleCodes = new ArrayList<>();
        Set<String> permissions = new HashSet<>();
        if (UserTypeEnum.ADMIN == userType) {
            roleCodes.addAll(sysRoleService.listRoleCodesByUserId(userId));
            permissions.addAll(sysMenuService.listPermsByUserId(userId));
        }

        LoginUser loginUser = new LoginUser(userId, username, userType, roleCodes, permissions);
        UserContext.set(loginUser);

        RequiresPermission requiresPermission = resolveAnnotation(handlerMethod);
        if (requiresPermission != null && !loginUser.hasPermission(requiresPermission.value())) {
            log.warn("权限不足：用户 {} 缺少 {}", username, requiresPermission.value());
            throw new BizException(ResultCode.FORBIDDEN, "无权限：" + requiresPermission.value());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程复用，必须清理，避免下一个请求读到上一个用户的登录态
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AuthConstant.TOKEN_HEADER);
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.startsWith(AuthConstant.TOKEN_PREFIX)) {
            return header.substring(AuthConstant.TOKEN_PREFIX.length()).trim();
        }
        // 兼容未带 Bearer 前缀的写法
        return header.trim();
    }

    private Long parseUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
    }

    private UserTypeEnum parseUserType(Integer code) {
        for (UserTypeEnum type : UserTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new BizException(ResultCode.UNAUTHORIZED);
    }

    private RequiresPermission resolveAnnotation(HandlerMethod handlerMethod) {
        RequiresPermission annotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (annotation != null) {
            return annotation;
        }
        return handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
    }
}
