package com.jh.userservice.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESUtil {

    private static final String KEY = "1234567890123456"; // 16자리 고정 키

    // AES 암호화
    public String encrypt(String value) {
        try {
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes); // 랜덤 IV 생성
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(value.getBytes());

            // IV와 암호문을 Base64로 인코딩 후 결합
            return Base64.getEncoder().encodeToString(ivBytes) + ":" + Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("암호화에 실패했습니다.", e);
        }
    }


    // AES 복호화
    public String decrypt(String encryptedValue) {
        try {
            String[] parts = encryptedValue.split(":");
            byte[] ivBytes = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("복호화에 실패했습니다.", e);
        }
    }
}