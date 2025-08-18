package dao;

import model.Category;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO category (name, last_update) VALUES (?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT category_id, name, last_update FROM category WHERE category_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT category_id, name, last_update FROM category ORDER BY category_id";
    
    private static final String UPDATE_SQL = 
        "UPDATE category SET name = ?, last_update = ? WHERE category_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM category WHERE category_id = ?";
    
    public int insert(Connection connection, Category category) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            
            // Use provided LocalDateTime or current time
            if (category.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(category.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    category.setCategoryId(categoryId);
                    return categoryId;
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }
    
    public Category findById(Connection connection, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, categoryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCategoryFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Category> findAll(Connection connection) throws SQLException {
        List<Category> categories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                categories.add(extractCategoryFromResultSet(resultSet));
            }
        }
        
        return categories;
    }
    
    public void update(Connection connection, Category category) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, category.getName());
            
            // Use provided LocalDateTime or current time
            if (category.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(category.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(3, category.getCategoryId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, categoryId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting category failed, no rows affected.");
            }
        }
    }
    
    private Category extractCategoryFromResultSet(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setCategoryId(resultSet.getInt("category_id"));
        category.setName(resultSet.getString("name"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            category.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return category;
    }
    public List<Category> findByName(Connection connection, String name) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, last_update FROM category WHERE name ILIKE ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(extractCategoryFromResultSet(resultSet));
                }
            }
        }
        
        return categories;
    }

}
