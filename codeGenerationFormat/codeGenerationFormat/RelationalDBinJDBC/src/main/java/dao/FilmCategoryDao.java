package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Category;
import model.Film;
import model.FilmCategory;

public class FilmCategoryDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO film_category (film_id, category_id, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_FILM_ID_SQL = 
        "SELECT film_id, category_id, last_update FROM film_category WHERE film_id = ?";
    
    private static final String FIND_BY_CATEGORY_ID_SQL = 
        "SELECT film_id, category_id, last_update FROM film_category WHERE category_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM film_category WHERE film_id = ? AND category_id = ?";
    
    // Insert with IDs (for backward compatibility)
    public void insert(Connection connection, int filmId, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, filmId);
            statement.setInt(2, categoryId);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating film-category relationship failed, no rows affected.");
            }
        }
    }
    
 // Insert with FilmCategory object - FIXED
    public int insert(Connection connection, FilmCategory filmCategory) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Extract IDs from objects
            int filmId = filmCategory.getFilm() != null ? filmCategory.getFilm().getFilmId() : filmCategory.getFilm().getFilmId();
            int categoryId = filmCategory.getCategory() != null ? filmCategory.getCategory().getCategoryId() : filmCategory.getCategoryId();
            
            statement.setInt(1, filmId);
            statement.setInt(2, categoryId);
            
            // Use provided LocalDateTime or current time
            if (filmCategory.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(filmCategory.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating film-category relationship failed, no rows affected.");
            }
            
            //  FIXED: Just return the generated ID (don't set it on the object)
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int filmCategoryId = generatedKeys.getInt(1);
                    //  REMOVED the problematic line
                    return filmCategoryId;
                } else {
                    throw new SQLException("Creating film-category relationship failed, no ID obtained.");
                }
            }
        }
    }


    
    public List<FilmCategory> findByFilmId(Connection connection, int filmId) throws SQLException {
        List<FilmCategory> filmCategories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_FILM_ID_SQL)) {
            statement.setInt(1, filmId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    filmCategories.add(extractFilmCategoryFromResultSet(resultSet));
                }
            }
        }
        
        return filmCategories;
    }
    
    public List<FilmCategory> findByCategoryId(Connection connection, int categoryId) throws SQLException {
        List<FilmCategory> filmCategories = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CATEGORY_ID_SQL)) {
            statement.setInt(1, categoryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    filmCategories.add(extractFilmCategoryFromResultSet(resultSet));
                }
            }
        }
        
        return filmCategories;
    }
    
    public void delete(Connection connection, int filmId, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, filmId);
            statement.setInt(2, categoryId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting film-category relationship failed, no rows affected.");
            }
        }
    }
    
    // Delete with FilmCategory object
    public void delete(Connection connection, FilmCategory filmCategory) throws SQLException {
        int filmId = filmCategory.getFilm() != null ? filmCategory.getFilm().getFilmId() : filmCategory.getFilm().getFilmId();
        int categoryId = filmCategory.getCategory() != null ? filmCategory.getCategory().getCategoryId() : filmCategory.getCategoryId();
        
        delete(connection, filmId, categoryId);
    }
    
    private FilmCategory extractFilmCategoryFromResultSet(ResultSet resultSet) throws SQLException {
        FilmCategory filmCategory = new FilmCategory();
        
        // Set IDs directly
        int filmId = resultSet.getInt("film_id");
        int categoryId = resultSet.getInt("category_id");
        
        
        // Create placeholder objects with IDs for service layer to load full objects
        if (filmId > 0) {
            Film tempFilm = new Film();
            tempFilm.setFilmId(filmId);
            filmCategory.setFilm(tempFilm);
        }
        
        if (categoryId > 0) {
            Category tempCategory = new Category();
            tempCategory.setCategoryId(categoryId);
            filmCategory.setCategory(tempCategory);
        }
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            filmCategory.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return filmCategory;
    }
}
