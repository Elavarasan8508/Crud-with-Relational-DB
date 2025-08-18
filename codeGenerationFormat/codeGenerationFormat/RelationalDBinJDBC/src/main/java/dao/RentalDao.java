package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO rental (rental_date, inventory_id, customer_id, return_date, staff_id, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT rental_id, rental_date, inventory_id, customer_id, return_date, staff_id, last_update " +
        "FROM rental WHERE rental_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT rental_id, rental_date, inventory_id, customer_id, return_date, staff_id, last_update " +
        "FROM rental ORDER BY rental_id";
    
    private static final String FIND_BY_CUSTOMER_ID_SQL = 
        "SELECT rental_id, rental_date, inventory_id, customer_id, return_date, staff_id, last_update " +
        "FROM rental WHERE customer_id = ? ORDER BY rental_date DESC";
    
    private static final String FIND_BY_INVENTORY_ID_SQL = 
        "SELECT rental_id, rental_date, inventory_id, customer_id, return_date, staff_id, last_update " +
        "FROM rental WHERE inventory_id = ? ORDER BY rental_date DESC";
    
    private static final String UPDATE_SQL = 
        "UPDATE rental SET rental_date = ?, inventory_id = ?, customer_id = ?, return_date = ?, " +
        "staff_id = ?, last_update = ? WHERE rental_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM rental WHERE rental_id = ?";
    
    public int insert(Connection connection, Rental rental) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Convert LocalDateTime to Timestamp
            statement.setTimestamp(1, rental.getRentalDate() != null ? 
                Timestamp.valueOf(rental.getRentalDate()) : new Timestamp(System.currentTimeMillis()));
            
            // Extract IDs from objects
            statement.setInt(2, rental.getInventory() != null ? rental.getInventory().getInventoryId() : 0);
            statement.setInt(3, rental.getCustomer() != null ? rental.getCustomer().getCustomerId() : 0);
            
            // Handle nullable return date
            if (rental.getReturnDate() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(rental.getReturnDate()));
            } else {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            }
            
            statement.setInt(5, rental.getStaff() != null ? rental.getStaff().getStaffId() : 0);
            statement.setTimestamp(6, rental.getLastUpdate() != null ? 
                Timestamp.valueOf(rental.getLastUpdate()) : new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating rental failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int rentalId = generatedKeys.getInt(1);
                    rental.setRentalId(rentalId);
                    return rentalId;
                } else {
                    throw new SQLException("Creating rental failed, no ID obtained.");
                }
            }
        }
    }
    
    public Rental findById(Connection connection, int rentalId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, rentalId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractRentalFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Rental> findAll(Connection connection) throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                rentals.add(extractRentalFromResultSet(resultSet));
            }
        }
        
        return rentals;
    }
    
    public List<Rental> findByCustomerId(Connection connection, int customerId) throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CUSTOMER_ID_SQL)) {
            statement.setInt(1, customerId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rentals.add(extractRentalFromResultSet(resultSet));
                }
            }
        }
        
        return rentals;
    }
    
    public List<Rental> findByInventoryId(Connection connection, int inventoryId) throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_INVENTORY_ID_SQL)) {
            statement.setInt(1, inventoryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rentals.add(extractRentalFromResultSet(resultSet));
                }
            }
        }
        
        return rentals;
    }
    
    public void update(Connection connection, Rental rental) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            
            // Convert LocalDateTime to Timestamp
            statement.setTimestamp(1, rental.getRentalDate() != null ? 
                Timestamp.valueOf(rental.getRentalDate()) : new Timestamp(System.currentTimeMillis()));
            
            // Extract IDs from objects
            statement.setInt(2, rental.getInventory() != null ? rental.getInventory().getInventoryId() : 0);
            statement.setInt(3, rental.getCustomer() != null ? rental.getCustomer().getCustomerId() : 0);
            
            // Handle nullable return date in update
            if (rental.getReturnDate() != null) {
                statement.setTimestamp(4, Timestamp.valueOf(rental.getReturnDate()));
            } else {
                statement.setNull(4, java.sql.Types.TIMESTAMP);
            }
            
            statement.setInt(5, rental.getStaff() != null ? rental.getStaff().getStaffId() : 0);
            statement.setTimestamp(6, rental.getLastUpdate() != null ? 
                Timestamp.valueOf(rental.getLastUpdate()) : new Timestamp(System.currentTimeMillis()));
            statement.setInt(7, rental.getRentalId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating rental failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int rentalId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, rentalId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting rental failed, no rows affected.");
            }
        }
    }
    
    private Rental extractRentalFromResultSet(ResultSet resultSet) throws SQLException {
        Rental rental = new Rental();
        rental.setRentalId(resultSet.getInt("rental_id"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp rentalDateTs = resultSet.getTimestamp("rental_date");
        if (rentalDateTs != null) {
            rental.setRentalDate(rentalDateTs.toLocalDateTime());
        }
        
        // Handle nullable return date
        Timestamp returnDateTs = resultSet.getTimestamp("return_date");
        if (returnDateTs != null) {
            rental.setReturnDate(returnDateTs.toLocalDateTime());
        }
        
        Timestamp lastUpdateTs = resultSet.getTimestamp("last_update");
        if (lastUpdateTs != null) {
            rental.setLastUpdate(lastUpdateTs.toLocalDateTime());
        }
        
        // Create placeholder objects with IDs for service layer to load full objects
        int customerId = resultSet.getInt("customer_id");
        if (customerId > 0) {
            Customer tempCustomer = new Customer();
            tempCustomer.setCustomerId(customerId);
            rental.setCustomer(tempCustomer);
        }
        
        int inventoryId = resultSet.getInt("inventory_id");
        if (inventoryId > 0) {
            Inventory tempInventory = new Inventory();
            tempInventory.setInventoryId(inventoryId);
            rental.setInventory(tempInventory);
        }
        
        int staffId = resultSet.getInt("staff_id");
        if (staffId > 0) {
            Staff tempStaff = new Staff();
            tempStaff.setStaffId(staffId);
            rental.setStaff(tempStaff);
        }
        
        return rental;
    }
}
