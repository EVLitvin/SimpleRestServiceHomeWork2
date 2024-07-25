package ru.evlitvin.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class DatabaseConnection {

    private DatabaseConnection() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final HikariDataSource dataSource;

    static {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("C:\\database.properties"))) {
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database properties", e);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbc.url"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        config.setDriverClassName(properties.getProperty("jdbc.driverClassName"));

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
