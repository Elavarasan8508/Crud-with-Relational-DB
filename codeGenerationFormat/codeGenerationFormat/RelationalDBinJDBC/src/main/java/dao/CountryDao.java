package dao;

import model.Country;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CountryDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO country (country, last_update) VALUES (?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT country_id, country, last_update FROM country WHERE country_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT country_id, country, last_update FROM country ORDER BY country_id";
    
    private static final String UPDATE_SQL = 
        "UPDATE country SET country = ?, last_update = ? WHERE country_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM country WHERE country_id = ?";
    
    public int insert(Connection connection, Country country) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, country.getCountry());
            
            // Use provided LocalDateTime or current time
            if (country.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(country.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating country failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int countryId = generatedKeys.getInt(1);
                    country.setCountryId(countryId);
                    return countryId;
                } else {
                    throw new SQLException("Creating country failed, no ID obtained.");
                }
            }
        }
    }
    
    public Country findById(Connection connection, int countryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, countryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCountryFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Country> findAll(Connection connection) throws SQLException {
        List<Country> countries = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                countries.add(extractCountryFromResultSet(resultSet));
            }
        }
        
        return countries;
    }
    
    public void update(Connection connection, Country country) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, country.getCountry());
            
            // Use provided LocalDateTime or current time
            if (country.getLastUpdate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(country.getLastUpdate()));
            } else {
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(3, country.getCountryId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating country failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int countryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, countryId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting country failed, no rows affected.");
            }
        }
    }
    
    private Country extractCountryFromResultSet(ResultSet resultSet) throws SQLException {
        Country country = new Country();
        country.setCountryId(resultSet.getInt("country_id"));
        country.setCountry(resultSet.getString("country"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            country.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return country;
    }
}
