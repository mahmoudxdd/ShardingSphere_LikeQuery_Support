package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

public class ShardingSphereConfig {
    private static DataSource dataSource;
    //parameters (add chunks columns want to use in encrypt)
    private static final String CHUNKS_COLUMN_NAME = "email_chunks";
    private static final String AES_KEY = "123456abc";
    // ////////
    static {
        try {
            dataSource = createDataSource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static DataSource getDataSource() {
        return dataSource;
    }
    public static String getChunksColumnName() {
        return CHUNKS_COLUMN_NAME;
    }
    private static DataSource createDataSource() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb");
        hikariConfig.setUsername("mah");
        hikariConfig.setPassword("mah");
        hikariConfig.setSchema("public");
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        return ShardingSphereDataSourceFactory.createDataSource(
                "testdb",
                Collections.singletonMap("testdb", hikariDataSource),
                Collections.singleton(createEncryptRuleConfiguration()),
                props
        );
    }
    private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Properties aesProps = new Properties();
        aesProps.setProperty("aes-key-value", AES_KEY);
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("aes_encryptor", new AlgorithmConfiguration("AES", aesProps));
        encryptors.put("custom_like", new AlgorithmConfiguration("CUSTOM_LIKE", new Properties()));
        List<EncryptColumnRuleConfiguration> columns = new ArrayList<>();
        columns.add(new EncryptColumnRuleConfiguration(
                "email",
                "email",
                "",
                CHUNKS_COLUMN_NAME,
                "",
                "aes_encryptor",
                null,
                "custom_like",
                true
        ));
        columns.add(new EncryptColumnRuleConfiguration(
                "password",
                "password",
                "",
                "",
                "",
                "aes_encryptor",
                null,
                null,
                true
        ));
        return new EncryptRuleConfiguration(
                Collections.singleton(new EncryptTableRuleConfiguration("users", columns, true)),
                encryptors
        );
    }
}