package dao;

import model.Language;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LanguageDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO language (name, last_update) VALUES (?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT language_id, name, last_update FROM language WHERE language_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT language_id, name, last_update FROM language ORDER BY language_id";
    
    private static final String UPDATE_SQL = 
        "UPDATE language SET name = ?, last_update = ? WHERE language_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM language WHERE language_id = ?";
    
    public int insert(Connection connection, Language language) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, language.getName());
            
            // Use provided LocalDateTime or current time
            if (language.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(language.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating language failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int languageId = generatedKeys.getInt(1);
                    language.setLanguageId(languageId);
                    return languageId;
                } else {
                    throw new SQLException("Creating language failed, no ID obtained.");
                }
            }
        }
    }
    
    public Language findById(Connection connection, int languageId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, languageId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractLanguageFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Language> findAll(Connection connection) throws SQLException {
        List<Language> languages = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                languages.add(extractLanguageFromResultSet(resultSet));
            }
        }
        
        return languages;
    }
    
    public void update(Connection connection, Language language) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, language.getName());
            
            // Use provided LocalDateTime or current time
            if (language.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(language.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(3, language.getLanguageId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating language failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int languageId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, languageId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting language failed, no rows affected.");
            }
        }
    }
    
    private Language extractLanguageFromResultSet(ResultSet resultSet) throws SQLException {
        Language language = new Language();
        language.setLanguageId(resultSet.getInt("language_id"));
        language.setName(resultSet.getString("name"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            language.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return language;
    }
}
