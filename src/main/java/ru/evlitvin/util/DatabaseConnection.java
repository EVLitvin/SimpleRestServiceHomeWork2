package ru.evlitvin.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConnection {

    private static final HikariDataSource dataSource;

    private DatabaseConnection() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        try {
            Properties properties = loadProperties();
            HikariConfig config = new HikariConfig();
            String dbType = properties.getProperty("database.type");
            if ("h2".equalsIgnoreCase(dbType)) {
                config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
                config.setDriverClassName("org.h2.Driver");
            } else {
                config.setJdbcUrl(properties.getProperty("jdbc.url"));
                config.setUsername(properties.getProperty("jdbc.username"));
                config.setPassword(properties.getProperty("jdbc.password"));
                config.setDriverClassName(properties.getProperty("jdbc.driverClassName"));
            }
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize DatabaseConnection", e);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("database.properties file not found");
            }
            properties.load(input);
        }
        return properties;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

}
