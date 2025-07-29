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
            newUser.setUsername("capucina");
            newUser.setEmail("capucina@gmail.com");
            newUser.setPassword("capcap23");
            //userRepo.createUser(newUser);

            List<User> users = userRepo.findByEmailLike("%capucina%");
            System.out.println(users);

        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }
}