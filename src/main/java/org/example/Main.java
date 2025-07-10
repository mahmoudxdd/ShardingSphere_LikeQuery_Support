package org.example;

import org.example.config.ShardingSphereConfig;
import org.example.encrypt.CustomLikeEncryptor;
import org.example.model.User;
import org.example.repository.UserRepository;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            CustomLikeEncryptor encryptor = new CustomLikeEncryptor();
            DataSource dataSource = ShardingSphereConfig.getDataSource();
            UserRepository userRepo = new UserRepository(dataSource, encryptor);
            User newUser = new User();
            newUser.setUsername("mahmoud");
            newUser.setEmail("mahmoud@gmail.com");
            newUser.setPassword("mahmoud23");
          // userRepo.createUser(newUser);
           List<User> matchedUsers = userRepo.findByEmailLike("%gmail%");
            for (User user : matchedUsers) {
                System.out.println(user.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }
}