package com.fruitmall.common.util;

import com.fruitmall.common.constant.AuthConstant;
import com.fruitmall.common.enums.UserTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 * 登录态无状态，不落 Redis；密钥走本地配置（application-local.yml），禁止提交进仓库。
 */
@Slf4j
@Component
public class JwtUtil {

    /** HS256 要求密钥不少于 256 位（32 字节），密钥再长也仍按 HS256 签发 */
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey secretKey;

    /** 令牌有效期（秒） */
    private final long expireSeconds;

    public JwtUtil(@Value("${fruit-mall.jwt.secret}") String secret,
                   @Value("${fruit-mall.jwt.expire-minutes:120}") long expireMinutes) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT 密钥长度不足 32 字节，请在本地配置中更换为更长的随机字符串");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireSeconds = expireMinutes * 60L;
        log.info("JWT 初始化完成，令牌有效期 {} 分钟", expireMinutes);
    }

    /**
     * 签发令牌
     *
     * @param userId   用户ID
     * @param username 登录名
     * @param userType 用户类型，见 UserTypeEnum
     */
    public String generate(Long userId, String username, UserTypeEnum userType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(AuthConstant.CLAIM_USERNAME, username)
                .claim(AuthConstant.CLAIM_USER_TYPE, userType.getCode())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000L))
                // 显式指定 HS256：不指定时 jjwt 会按密钥长度自动升级为 HS384/HS512，
                // 导致更换密钥后令牌头部的算法跟着变化，排查问题时容易困惑
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验令牌，失败统一抛 401。
     *
     * @param token 不含前缀的令牌字符串
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("令牌解析失败：{}", e.getMessage());
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
