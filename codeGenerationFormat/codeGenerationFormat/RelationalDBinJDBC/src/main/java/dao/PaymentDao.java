package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO payment (customer_id, staff_id, rental_id, amount, payment_date, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update " +
        "FROM payment WHERE payment_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update " +
        "FROM payment ORDER BY payment_id";
    
    private static final String FIND_BY_CUSTOMER_ID_SQL = 
        "SELECT payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update " +
        "FROM payment WHERE customer_id = ? ORDER BY payment_date DESC";
    
    private static final String FIND_BY_RENTAL_ID_SQL = 
        "SELECT payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update " +
        "FROM payment WHERE rental_id = ? ORDER BY payment_date DESC";
    
    private static final String UPDATE_SQL = 
        "UPDATE payment SET customer_id = ?, staff_id = ?, rental_id = ?, amount = ?, " +
        "payment_date = ?, last_update = ? WHERE payment_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM payment WHERE payment_id = ?";
    
    public int insert(Connection connection, Payment payment) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Extract customer ID from Customer object
            if (payment.getCustomer() != null && payment.getCustomer().getCustomerId() > 0) {
                statement.setInt(1, payment.getCustomer().getCustomerId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract staff ID from Staff object
            if (payment.getStaff() != null && payment.getStaff().getStaffId() > 0) {
                statement.setInt(2, payment.getStaff().getStaffId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Extract rental ID from Rental object
            if (payment.getRental() != null && payment.getRental().getRentalId() > 0) {
                statement.setInt(3, payment.getRental().getRentalId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            
            statement.setBigDecimal(4, payment.getAmount());
            
            // Convert LocalDateTime to Timestamp for payment date
            if (payment.getPaymentDate() != null) {
                statement.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate()));
            } else {
                statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            
            // Use provided LocalDateTime or current time for last update
            if (payment.getLastUpdate() != null) {
                statement.setTimestamp(6, Timestamp.valueOf(payment.getLastUpdate()));
            } else {
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int paymentId = generatedKeys.getInt(1);
                    payment.setPaymentId(paymentId);
                    return paymentId;
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        }
    }
    
    public Payment findById(Connection connection, int paymentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, paymentId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractPaymentFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Payment> findAll(Connection connection) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                payments.add(extractPaymentFromResultSet(resultSet));
            }
        }
        
        return payments;
    }
    
    public List<Payment> findByCustomerId(Connection connection, int customerId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CUSTOMER_ID_SQL)) {
            statement.setInt(1, customerId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(extractPaymentFromResultSet(resultSet));
                }
            }
        }
        
        return payments;
    }
    
    public List<Payment> findByRentalId(Connection connection, int rentalId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_RENTAL_ID_SQL)) {
            statement.setInt(1, rentalId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(extractPaymentFromResultSet(resultSet));
                }
            }
        }
        
        return payments;
    }
    
    public void update(Connection connection, Payment payment) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            
            // Extract customer ID from Customer object
            if (payment.getCustomer() != null && payment.getCustomer().getCustomerId() > 0) {
                statement.setInt(1, payment.getCustomer().getCustomerId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract staff ID from Staff object
            if (payment.getStaff() != null && payment.getStaff().getStaffId() > 0) {
                statement.setInt(2, payment.getStaff().getStaffId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Extract rental ID from Rental object
            if (payment.getRental() != null && payment.getRental().getRentalId() > 0) {
                statement.setInt(3, payment.getRental().getRentalId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
  
            statement.setBigDecimal(4, payment.getAmount());
            
            // Convert LocalDateTime to Timestamp for payment date
            if (payment.getPaymentDate() != null) {
                statement.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate()));
            } else {
                statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            
            // Use provided LocalDateTime or current time for last update
            if (payment.getLastUpdate() != null) {
                statement.setTimestamp(6, Timestamp.valueOf(payment.getLastUpdate()));
            } else {
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(7, payment.getPaymentId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating payment failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int paymentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, paymentId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting payment failed, no rows affected.");
            }
        }
    }
    
    private Payment extractPaymentFromResultSet(ResultSet resultSet) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(resultSet.getInt("payment_id"));
        payment.setAmount(resultSet.getBigDecimal("amount"));
        
        // Convert Timestamps to LocalDateTime
        Timestamp paymentDate = resultSet.getTimestamp("payment_date");
        if (paymentDate != null) {
            payment.setPaymentDate(paymentDate.toLocalDateTime());
        }
        
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            payment.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        // Create placeholder Customer object with ID for service layer to load full object
        int customerId = resultSet.getInt("customer_id");
        if (customerId > 0) {
            Customer tempCustomer = new Customer();
            tempCustomer.setCustomerId(customerId);
            payment.setCustomer(tempCustomer);
        }
        
        // Create placeholder Staff object with ID for service layer to load full object
        int staffId = resultSet.getInt("staff_id");
        if (staffId > 0) {
            Staff tempStaff = new Staff();
            tempStaff.setStaffId(staffId);
            payment.setStaff(tempStaff);
        }
        
        // Create placeholder Rental object with ID for service layer to load full object
        int rentalId = resultSet.getInt("rental_id");
        if (rentalId > 0) {
            Rental tempRental = new Rental();
            tempRental.setRentalId(rentalId);
            payment.setRental(tempRental);
        }
        
        return payment;
    }
}
