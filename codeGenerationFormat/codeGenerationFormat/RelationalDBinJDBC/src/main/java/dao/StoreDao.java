package dao;

import model.Store;
import model.Address;
import model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO store (manager_staff_id, address_id, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT store_id, manager_staff_id, address_id, last_update FROM store WHERE store_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT store_id, manager_staff_id, address_id, last_update FROM store ORDER BY store_id";
    
    private static final String UPDATE_SQL = 
        "UPDATE store SET manager_staff_id = ?, address_id = ?, last_update = ? WHERE store_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM store WHERE store_id = ?";
    
    public int insert(Connection connection, Store store) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Extract manager staff ID from Staff object
            if (store.getManagerStaff() != null && store.getManagerStaff().getStaffId() > 0) {
                statement.setInt(1, store.getManagerStaff().getStaffId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract address ID from Address object
            if (store.getAddress() != null && store.getAddress().getAddressId() > 0) {
                statement.setInt(2, store.getAddress().getAddressId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided timestamp or current time
            if (store.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(store.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating store failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int storeId = generatedKeys.getInt(1);
                    store.setStoreId(storeId);
                    return storeId;
                } else {
                    throw new SQLException("Creating store failed, no ID obtained.");
                }
            }
        }
    }
    
    public Store findById(Connection connection, int storeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, storeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractStoreFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Store> findAll(Connection connection) throws SQLException {
        List<Store> stores = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                stores.add(extractStoreFromResultSet(resultSet));
            }
        }
        
        return stores;
    }
    
    public void update(Connection connection, Store store) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            
            // Extract manager staff ID from Staff object
            if (store.getManagerStaff() != null && store.getManagerStaff().getStaffId() > 0) {
                statement.setInt(1, store.getManagerStaff().getStaffId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract address ID from Address object
            if (store.getAddress() != null && store.getAddress().getAddressId() > 0) {
                statement.setInt(2, store.getAddress().getAddressId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided timestamp or current time
            if (store.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(store.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(4, store.getStoreId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating store failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int storeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, storeId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting store failed, no rows affected.");
            }
        }
    }
    
    // Create Store object with placeholder Address and Staff objects containing just IDs
    private Store extractStoreFromResultSet(ResultSet resultSet) throws SQLException {
        Store store = new Store();
        store.setStoreId(resultSet.getInt("store_id"));
        
        // Create placeholder Address object with ID for service layer to load full object
        Integer addressId = resultSet.getObject("address_id", Integer.class);
        if (addressId != null && addressId > 0) {
            Address tempAddress = new Address();
            tempAddress.setAddressId(addressId);
            store.setAddress(tempAddress);
        }
        
        // Create placeholder Staff object with ID for service layer to load full object
        Integer managerStaffId = resultSet.getObject("manager_staff_id", Integer.class);
        if (managerStaffId != null && managerStaffId > 0) {
            Staff tempStaff = new Staff();
            tempStaff.setStaffId(managerStaffId);
            store.setManagerStaff(tempStaff);
        }
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            store.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return store;
    }
}
