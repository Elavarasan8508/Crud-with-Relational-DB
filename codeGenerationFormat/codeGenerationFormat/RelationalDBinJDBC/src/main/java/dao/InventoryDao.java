package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO inventory (film_id, store_id, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT inventory_id, film_id, store_id, last_update FROM inventory WHERE inventory_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT inventory_id, film_id, store_id, last_update FROM inventory ORDER BY inventory_id";
    
    private static final String FIND_BY_FILM_ID_SQL = 
        "SELECT inventory_id, film_id, store_id, last_update FROM inventory WHERE film_id = ?";
    
    private static final String FIND_BY_STORE_ID_SQL = 
        "SELECT inventory_id, film_id, store_id, last_update FROM inventory WHERE store_id = ?";
    
    //  NEW: SQL for finding inventory by film and store
    private static final String FIND_BY_FILM_AND_STORE_SQL = 
        "SELECT inventory_id, film_id, store_id, last_update FROM inventory WHERE film_id = ? AND store_id = ?";
    
    //  NEW: SQL for checking available inventory (not currently rented)
    private static final String FIND_AVAILABLE_INVENTORY_SQL = 
        "SELECT COUNT(*) FROM inventory i " +
        "LEFT JOIN rental r ON i.inventory_id = r.inventory_id AND r.return_date IS NULL " +
        "WHERE i.film_id = ? AND i.store_id = ? AND r.rental_id IS NULL";
    
    private static final String UPDATE_SQL = 
        "UPDATE inventory SET film_id = ?, store_id = ?, last_update = ? WHERE inventory_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM inventory WHERE inventory_id = ?";
    
    public int insert(Connection connection, Inventory inventory) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Extract film ID from Film object
            if (inventory.getFilm() != null && inventory.getFilm().getFilmId() > 0) {
                statement.setInt(1, inventory.getFilm().getFilmId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract store ID from Store object
            if (inventory.getStore() != null && inventory.getStore().getStoreId() > 0) {
                statement.setInt(2, inventory.getStore().getStoreId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided LocalDateTime or current time
            if (inventory.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(inventory.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating inventory failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int inventoryId = generatedKeys.getInt(1);
                    inventory.setInventoryId(inventoryId);
                    return inventoryId;
                } else {
                    throw new SQLException("Creating inventory failed, no ID obtained.");
                }
            }
        }
    }
    
    public Inventory findById(Connection connection, int inventoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, inventoryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractInventoryFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Inventory> findAll(Connection connection) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                inventories.add(extractInventoryFromResultSet(resultSet));
            }
        }
        
        return inventories;
    }
    
    //  FIXED: Complete implementation for findByFilmId
    public List<Inventory> findByFilmId(Connection connection, int filmId) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_FILM_ID_SQL)) {
            statement.setInt(1, filmId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    inventories.add(extractInventoryFromResultSet(resultSet));
                }
            }
        }
        
        return inventories;
    }

    //  NEW: Complete implementation for findByFilmIdAndStoreId
    public List<Inventory> findByFilmIdAndStoreId(Connection connection, int filmId, int storeId) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_FILM_AND_STORE_SQL)) {
            statement.setInt(1, filmId);
            statement.setInt(2, storeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    inventories.add(extractInventoryFromResultSet(resultSet));
                }
            }
        }
        
        return inventories;
    }

    //  NEW: Check if there's available inventory (not currently rented)
    public boolean findAvailableInventory(Connection connection, int filmId, int storeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_AVAILABLE_INVENTORY_SQL)) {
            statement.setInt(1, filmId);
            statement.setInt(2, storeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int availableCount = resultSet.getInt(1);
                    return availableCount > 0;
                }
            }
        }
        return false;
    }
    
    public List<Inventory> findByStoreId(Connection connection, int storeId) throws SQLException {
        List<Inventory> inventories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_STORE_ID_SQL)) {
            statement.setInt(1, storeId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    inventories.add(extractInventoryFromResultSet(resultSet));
                }
            }
        }
        
        return inventories;
    }
    
    public void update(Connection connection, Inventory inventory) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            
            // Extract film ID from Film object
            if (inventory.getFilm() != null && inventory.getFilm().getFilmId() > 0) {
                statement.setInt(1, inventory.getFilm().getFilmId());
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Extract store ID from Store object
            if (inventory.getStore() != null && inventory.getStore().getStoreId() > 0) {
                statement.setInt(2, inventory.getStore().getStoreId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided LocalDateTime or current time
            if (inventory.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(inventory.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(4, inventory.getInventoryId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating inventory failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int inventoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, inventoryId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting inventory failed, no rows affected.");
            }
        }
    }
    
    private Inventory extractInventoryFromResultSet(ResultSet resultSet) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(resultSet.getInt("inventory_id"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            inventory.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        // Create placeholder Film object with ID for service layer to load full object
        int filmId = resultSet.getInt("film_id");
        if (filmId > 0) {
            Film tempFilm = new Film();
            tempFilm.setFilmId(filmId);
            inventory.setFilm(tempFilm);
        }
        
        // Create placeholder Store object with ID for service layer to load full object
        int storeId = resultSet.getInt("store_id");
        if (storeId > 0) {
            Store tempStore = new Store();
            tempStore.setStoreId(storeId);
            inventory.setStore(tempStore);
        }
        
        return inventory;
    }
}
