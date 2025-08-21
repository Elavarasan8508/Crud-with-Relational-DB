package dao;

import model.Category;
import java.sql.*;
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

    // Insert
    public int insert(Connection connection, Category category) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setTimestamp(2, Timestamp.valueOf(category.getLastUpdate() != null
                    ? category.getLastUpdate()
                    : java.time.LocalDateTime.now()));

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

    // Find by ID
    public Category findById(Connection connection, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, categoryId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setName(rs.getString("name"));
                    Timestamp lastUpdate = rs.getTimestamp("last_update");
                    if (lastUpdate != null) {
                        category.setLastUpdate(lastUpdate.toLocalDateTime());
                    }
                    return category;
                }
            }
        }
        return null;
    }

    // Find all
    public List<Category> findAll(Connection connection) throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                Timestamp lastUpdate = rs.getTimestamp("last_update");
                if (lastUpdate != null) {
                    category.setLastUpdate(lastUpdate.toLocalDateTime());
                }
                categories.add(category);
            }
        }
        return categories;
    }

    // Update
    public void update(Connection connection, Category category) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, category.getName());
            statement.setTimestamp(2, Timestamp.valueOf(category.getLastUpdate() != null
                    ? category.getLastUpdate()
                    : java.time.LocalDateTime.now()));
            statement.setInt(3, category.getCategoryId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
    }

    // Delete
    public void deleteById(Connection connection, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, categoryId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting category failed, no rows affected.");
            }
        }
    }

    // Find by Name
    public List<Category> findByName(Connection connection, String name) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, last_update FROM category WHERE name LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setName(rs.getString("name"));
                    Timestamp lastUpdate = rs.getTimestamp("last_update");
                    if (lastUpdate != null) {
                        category.setLastUpdate(lastUpdate.toLocalDateTime());
                    }
                    categories.add(category);
                }
            }
        }
        return categories;
    }
}
