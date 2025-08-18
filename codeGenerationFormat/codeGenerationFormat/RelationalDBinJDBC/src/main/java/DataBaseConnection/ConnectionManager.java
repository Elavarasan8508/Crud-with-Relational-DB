package DataBaseConnection;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DatabaseConfig.getDataSource().getConnection();
            System.out.println("🔄 Connection obtained from pool");
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Failed to get connection from pool: " + e.getMessage());
            throw e;
        }
    }
    
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // Returns connection to pool
                System.out.println("🔄 Connection returned to pool");
            } catch (SQLException e) {
                System.err.println("❌ Error returning connection to pool: " + e.getMessage());
            }
        }
    }
}
