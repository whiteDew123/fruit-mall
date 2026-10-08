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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.AntPathMatcher;
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

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final ISysRoleService sysRoleService;
    private final ISysMenuService sysMenuService;

    /**
     * 可选登录的路径：公开接口，但带令牌时仍要识别用户身份。
     * 例如首页推荐对游客可用、对登录会员要做个性化；行为埋点对游客可用、登录后要记录会员ID。
     * 这类路径一旦放进"完全跳过"的白名单，UserContext 就永远为空，个性化与埋点归属都会失效。
     */
    @Value("${fruit-mall.auth.optional-paths:}")
    private String optionalPaths;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean optionalAuth = isOptionalPath(request.getRequestURI());
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            if (optionalAuth) {
                return true;
            }
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (BizException e) {
            // 公开接口带了失效令牌时按游客处理，不能因为令牌过期就打不开首页
            if (optionalAuth) {
                return true;
            }
            throw e;
        }
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

    /** 判断当前请求是否为"可选登录"路径 */
    private boolean isOptionalPath(String uri) {
        if (!StringUtils.hasText(optionalPaths) || uri == null) {
            return false;
        }
        for (String pattern : optionalPaths.split(",")) {
            String trimmed = pattern.trim();
            if (StringUtils.hasText(trimmed) && PATH_MATCHER.match(trimmed, uri)) {
                return true;
            }
        }
        return false;
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
