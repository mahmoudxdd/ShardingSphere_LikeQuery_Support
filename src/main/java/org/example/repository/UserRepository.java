package org.example.repository;

import org.example.encrypt.CustomLikeEncryptor;
import org.example.model.User;
import org.example.rewrite.CustomSQLRewriter;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public class UserRepository {
    private final DataSource dataSource;
    private final CustomLikeEncryptor encryptor;
    public UserRepository(DataSource dataSource, CustomLikeEncryptor encryptor) {
        this.dataSource = dataSource;
        this.encryptor = encryptor;
    }
    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
        }
        return user;
    }
    public List<User> findByEmailLike(String pattern) throws SQLException {
        CustomSQLRewriter.setOrder(100);
        List<String> searchChunks = encryptor.generateAllChunks(pattern.replaceAll("[%_]", "").toLowerCase());
        String[] searchChunksArray = searchChunks.toArray(new String[0]);
        try {
            String sql = "SELECT username, email FROM users WHERE email_chunks LIKE ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql))
            {
                String pgArrayLiteral = "{" + String.join(",", searchChunksArray) + "}";
                ps.setObject(1, pgArrayLiteral);
                try (ResultSet rs = ps.executeQuery()) {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        User user = new User();
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        users.add(user);
                    }
                    return users;
                }
            }
        } finally {
            CustomSQLRewriter.clearOrder();
        }
    }


}

