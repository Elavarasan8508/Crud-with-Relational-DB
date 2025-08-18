package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "root";
    private static final String DATABASE_DRIVER = "org.postgresql.Driver";
    
    private static HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DATABASE_URL);
        config.setUsername(DATABASE_USERNAME);
        config.setPassword(DATABASE_PASSWORD);
        config.setDriverClassName(DATABASE_DRIVER);
        
        // Pool configuration
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
        System.out.println("✅ Connection pool initialized");
    }
    
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    // Keep existing methods for backward compatibility
    public static String getUrl() { return DATABASE_URL; }
    public static String getUsername() { return DATABASE_USERNAME; }
    public static String getPassword() { return DATABASE_PASSWORD; }
    public static String getDriverClass() { return DATABASE_DRIVER; }
}
