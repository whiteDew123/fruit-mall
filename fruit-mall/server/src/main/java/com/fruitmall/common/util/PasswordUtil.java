package com.fruitmall.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希工具。
 *
 * 算法：BCrypt（spring-security-crypto），自带随机盐，成本因子写在密文中可读，
 * 是 OWASP 推荐的口令存储方案。密码禁止明文、禁止 MD5。
 *
 * 存储格式：$2a$10$...（60 字符，含算法版本、成本因子与随机盐）
 */
public final class PasswordUtil {

    /** 成本因子 10：单次校验约几十毫秒，兼顾安全与登录体验 */
    private static final int STRENGTH = 10;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(STRENGTH);

    private PasswordUtil() {
    }

    /**
     * 生成密码密文
     *
     * @param rawPassword 明文密码
     * @return 可直接入库的密文
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与密文是否匹配。
     * BCrypt 内部使用定长比较，避免时序侧信道。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 数据库中的密文
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 命令行生成密文，供初始化演示账号时使用：
     * java -cp target/classes com.fruitmall.common.util.PasswordUtil 明文密码
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("用法：java com.fruitmall.common.util.PasswordUtil <明文密码>");
            return;
        }
        String encoded = encode(args[0]);
        System.out.println(encoded);
        System.out.println("自检：" + matches(args[0], encoded));
    }
}
