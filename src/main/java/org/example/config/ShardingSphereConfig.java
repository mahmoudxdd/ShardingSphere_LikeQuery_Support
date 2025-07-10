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

    static {
        try {
            dataSource = createDataSource();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize ShardingSphere DataSource", e);
        }
    }
    public static DataSource getDataSource() {
        return dataSource;
    }
    public static DataSource createDataSource() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb");
        hikariConfig.setUsername("mah");
        hikariConfig.setPassword("mah");
        hikariConfig.setSchema("public");
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("testdb", hikariDataSource);
        EncryptRuleConfiguration encryptRuleConfig = createEncryptRuleConfiguration();
        return ShardingSphereDataSourceFactory.createDataSource(
                "testdb",
                dataSourceMap,
                Collections.singleton(encryptRuleConfig),
                props
        );
    }
    private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        Properties aesProps = new Properties();
        aesProps.setProperty("aes-key-value", "123456abc");
        encryptors.put("aes_encryptor", new AlgorithmConfiguration("AES", aesProps));
        encryptors.put("custom_like", new AlgorithmConfiguration("CUSTOM_LIKE", new Properties()));
        List<EncryptColumnRuleConfiguration> columns = new ArrayList<>();
        columns.add(new EncryptColumnRuleConfiguration(
                "email",
                "email",
                "",
                "email_chunks",
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
        EncryptTableRuleConfiguration encryptTableConfig = new EncryptTableRuleConfiguration(
                "users",
                columns,
                true
        );
        return new EncryptRuleConfiguration(
                Collections.singleton(encryptTableConfig),
                encryptors
        );
    }
}
