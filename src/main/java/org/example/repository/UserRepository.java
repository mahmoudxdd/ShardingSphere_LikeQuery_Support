package org.example.repository;

import org.example.encrypt.CustomLikeEncryptor;
import org.example.model.User;
import javax.sql.DataSource;
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
    public List<User> findByEmailLike(String emailPattern) throws SQLException {
        List<String> searchChunks = encryptor.generateAllChunks(emailPattern.replaceAll("[%_]", "").toLowerCase());
        String[] searchChunksArray = searchChunks.toArray(new String[0]);
        String sql = "SELECT username, email FROM users " +
                "WHERE email_chunks @> ?::text[]";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Array sqlArray = conn.createArrayOf("text", searchChunksArray);
            ps.setArray(1, sqlArray);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next())
                {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    users.add(user);
                }
                return users;
            }
        }
    }
}

