package com.fruitmall.common.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 密码哈希工具。
 *
 * 算法选型说明：项目文档原计划使用 BCrypt（依赖 spring-security-crypto），但该依赖不在
 * AGENTS.md 的依赖白名单内，为不擅自引入依赖，这里改用 JDK 自带的 PBKDF2-HMAC-SHA256：
 * 加盐、可调迭代次数、慢哈希，是 NIST SP 800-132 与 OWASP 推荐的口令存储方案，
 * 与 BCrypt 属同一类做法。若后续确认引入 spring-security-crypto，只需替换本类的
 * encode 与 matches 两个方法。
 *
 * 存储格式：pbkdf2$sha256$迭代次数$盐(Base64)$哈希(Base64)
 */
public final class PasswordUtil {

    /** JDK 自带的 PBKDF2 实现 */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /** 迭代次数，取 OWASP 对 PBKDF2-HMAC-SHA256 的建议值 */
    private static final int ITERATIONS = 210_000;

    /** 盐长度（字节） */
    private static final int SALT_BYTES = 16;

    /** 派生密钥长度（位） */
    private static final int KEY_BITS = 256;

    /** 存储格式前缀 */
    private static final String PREFIX = "pbkdf2$sha256";

    private static final SecureRandom RANDOM = new SecureRandom();

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
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 校验明文密码与密文是否匹配。
     * 使用 MessageDigest.isEqual 做定长比较，避免时序侧信道。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 数据库中的密文
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 5 || !"pbkdf2".equals(parts[0]) || !"sha256".equals(parts[1])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            // 密文格式非法，按校验失败处理
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("密码哈希计算失败", e);
        } finally {
            spec.clearPassword();
        }
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
