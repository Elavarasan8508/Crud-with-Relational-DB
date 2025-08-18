package DataBaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    
    public static <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection connection = null;
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);

            // Execute the user code inside transaction
            T result = callback.execute(connection);

            // Commit transaction
            connection.commit();
            System.out.println(" Transaction committed successfully");
            return result;
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    System.err.println(" Transaction rolled back due to an error");
                } catch (SQLException rollbackEx) {
                    System.err.println(" Rollback failed: " + rollbackEx.getMessage());
                    e.addSuppressed(rollbackEx);
                }
            }

            // Log full error info for debugging
            System.err.println(" Transaction failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace(); //  Full stack trace in server logs

            // Throw SQLException with original error details for higher layers (controller) to catch
            if (e instanceof SQLException) {
                throw (SQLException) e; // Already a SQLException, preserve it
            }
            throw new SQLException("Transaction failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    ConnectionManager.closeConnection(connection);
                    System.out.println(" Connection returned to pool");
                } catch (SQLException e) {
                    System.err.println("Error in finally block: " + e.getMessage());
                }
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws SQLException;
    }
}
