package org.example.encrypt;

import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CustomLikeEncryptor implements LikeEncryptAlgorithm {
    private static final String TYPE = "CUSTOM_LIKE";
    private Properties props;

    @Override
    public void init(Properties props) {
        this.props = props;
    }
    @Override
    public Object encrypt(Object plainValue, EncryptContext encryptContext) {
        if (plainValue == null) {
            return null;
        }
        String email = plainValue.toString().toLowerCase();
        return generateAllChunks(email).toArray(new String[0]);
    }
    @Override
    public String getType() {
        return TYPE;
    }
    public List<String> generateAllChunks(String email) {
        List<String> chunks = new ArrayList<>();
        int chunkSize = 3;
        for (int i = 0; i <= email.length() - chunkSize; i++) {
            String chunk = email.substring(i, i + chunkSize);
            chunks.add(hashChunk(chunk));
        }
        return chunks;
    }
    private String hashChunk(String chunk) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(chunk.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}