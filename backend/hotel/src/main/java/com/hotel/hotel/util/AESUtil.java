package com.hotel.hotel.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密工具类
 * 用于敏感数据加密存储
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    // 默认密钥 - 生产环境应该从配置文件或环境变量读取
    private static final String DEFAULT_KEY = "hotel-management-key";

    /**
     * AES加密
     * @param content 待加密内容
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_KEY);
    }

    /**
     * AES加密（自定义密钥）
     * @param content 待加密内容
     * @param key 加密密钥
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content, String key) {
        try {
            // 创建密钥
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(key), ALGORITHM);

            // 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // 加密
            byte[] encryptedBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            // Base64编码
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES解密
     * @param encryptedContent 加密后的Base64字符串
     * @return 解密后的原文
     */
    public static String decrypt(String encryptedContent) {
        return decrypt(encryptedContent, DEFAULT_KEY);
    }

    /**
     * AES解密（自定义密钥）
     * @param encryptedContent 加密后的Base64字符串
     * @param key 解密密钥
     * @return 解密后的原文
     */
    public static String decrypt(String encryptedContent, String key) {
        try {
            // 创建密钥
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(key), ALGORITHM);

            // 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Base64解码
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 获取密钥字节数组
     * @param key 密钥字符串
     * @return 密钥字节数组
     */
    private static byte[] getKeyBytes(String key) {
        // AES密钥必须是16字节（128位）
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[16];
        System.arraycopy(keyBytes, 0, result, 0, Math.min(keyBytes.length, 16));
        return result;
    }

    /**
     * 生成随机密钥
     * @return Base64编码的随机密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }
}