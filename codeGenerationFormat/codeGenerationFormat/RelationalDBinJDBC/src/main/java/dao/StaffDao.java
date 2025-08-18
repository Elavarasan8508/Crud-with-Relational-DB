package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StaffDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO staff (first_name, last_name, address_id, email, store_id, active, username, password, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT staff_id, first_name, last_name, address_id, email, store_id, active, username, password, last_update " +
        "FROM staff WHERE staff_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT staff_id, first_name, last_name, address_id, email, store_id, active, username, password, last_update " +
        "FROM staff ORDER BY staff_id";
    
    private static final String FIND_BY_STORE_ID_SQL = 
        "SELECT staff_id, first_name, last_name, address_id, email, store_id, active, username, password, last_update " +
        "FROM staff WHERE store_id = ? ORDER BY last_name, first_name";
    
    private static final String UPDATE_SQL = 
        "UPDATE staff SET first_name = ?, last_name = ?, address_id = ?, email = ?, store_id = ?, " +
        "active = ?, username = ?, password = ?, last_update = ? WHERE staff_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM staff WHERE staff_id = ?";
    
    public int insert(Connection connection, Staff staff) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, staff.getFirstName());
            statement.setString(2, staff.getLastName());
            
            // Extract address ID from Address object
            if (staff.getAddress() != null && staff.getAddress().getAddressId() > 0) {
                statement.setInt(3, staff.getAddress().getAddressId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            
            statement.setString(4, staff.getEmail());
            
            // Extract store ID from Store object
            if (staff.getStore() != null && staff.getStore().getStoreId() > 0) {
                statement.setInt(5, staff.getStore().getStoreId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            
            statement.setBoolean(6, staff.getActive());
            statement.setString(7, staff.getUsername());
            statement.setString(8, staff.getPassword());
            
            // Use provided LocalDateTime or current time
            if (staff.getLastUpdate() != null) {
                statement.setTimestamp(9, Timestamp.valueOf(staff.getLastUpdate()));
            } else {
                statement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating staff failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int staffId = generatedKeys.getInt(1);
                    staff.setStaffId(staffId);
                    return staffId;
                } else {
                    throw new SQLException("Creating staff failed, no ID obtained.");
                }
            }
        }
    }
    
    public Staff findById(Connection connection, int staffId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, staffId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractStaffFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Staff> findAll(Connection connection) throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                staffList.add(extractStaffFromResultSet(resultSet));
            }
        }
        
        return staffList;
    }
    
    public List<Staff> findByStoreId(Connection connection, int storeId) throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_STORE_ID_SQL)) {
            statement.setInt(1, storeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    staffList.add(extractStaffFromResultSet(resultSet));
                }
            }
        }
        
        return staffList;
    }
    
    public void update(Connection connection, Staff staff) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, staff.getFirstName());
            statement.setString(2, staff.getLastName());
            
            // Extract address ID from Address object
            if (staff.getAddress() != null && staff.getAddress().getAddressId() > 0) {
                statement.setInt(3, staff.getAddress().getAddressId());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            
            statement.setString(4, staff.getEmail());
            
            // Extract store ID from Store object
            if (staff.getStore() != null && staff.getStore().getStoreId() > 0) {
                statement.setInt(5, staff.getStore().getStoreId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            
            statement.setBoolean(6, staff.getActive());
            statement.setString(7, staff.getUsername());
            statement.setString(8, staff.getPassword());
            
            // Use provided LocalDateTime or current time
            if (staff.getLastUpdate() != null) {
                statement.setTimestamp(9, Timestamp.valueOf(staff.getLastUpdate()));
            } else {
                statement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(10, staff.getStaffId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating staff failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int staffId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, staffId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting staff failed, no rows affected.");
            }
        }
    }
    
    private Staff extractStaffFromResultSet(ResultSet resultSet) throws SQLException {
        Staff staff = new Staff();
        staff.setStaffId(resultSet.getInt("staff_id"));
        staff.setFirstName(resultSet.getString("first_name"));
        staff.setLastName(resultSet.getString("last_name"));
        staff.setEmail(resultSet.getString("email"));
        staff.setActive(resultSet.getBoolean("active"));
        staff.setUsername(resultSet.getString("username"));
        staff.setPassword(resultSet.getString("password"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            staff.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        // Create placeholder Address object with ID for service layer to load full object
        int addressId = resultSet.getInt("address_id");
        if (addressId > 0) {
            Address tempAddress = new Address();
            tempAddress.setAddressId(addressId);
            staff.setAddress(tempAddress);
        }
        
        // Create placeholder Store object with ID for service layer to load full object
        int storeId = resultSet.getInt("store_id");
        if (storeId > 0) {
            Store tempStore = new Store();
            tempStore.setStoreId(storeId);
            staff.setStore(tempStore);
        }
        
        return staff;
    }
}
