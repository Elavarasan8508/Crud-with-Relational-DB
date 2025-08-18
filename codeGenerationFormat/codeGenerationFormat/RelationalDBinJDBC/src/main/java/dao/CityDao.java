package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CityDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO city (city, country_id, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT city_id, city, country_id, last_update FROM city WHERE city_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT city_id, city, country_id, last_update FROM city ORDER BY city_id";
    
    private static final String FIND_BY_COUNTRY_ID_SQL = 
        "SELECT city_id, city, country_id, last_update FROM city WHERE country_id = ?";
    
    private static final String UPDATE_SQL = 
        "UPDATE city SET city = ?, country_id = ?, last_update = ? WHERE city_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM city WHERE city_id = ?";
    
    public int insert(Connection connection, City city) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, city.getCity());
            
            // Extract country ID from Country object
            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                statement.setInt(2, city.getCountry().getCountryId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided LocalDateTime or current time
            if (city.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(city.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating city failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int cityId = generatedKeys.getInt(1);
                    city.setCityId(cityId);
                    return cityId;
                } else {
                    throw new SQLException("Creating city failed, no ID obtained.");
                }
            }
        }
    }
    
    public City findById(Connection connection, int cityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, cityId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCityFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<City> findAll(Connection connection) throws SQLException {
        List<City> cities = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                cities.add(extractCityFromResultSet(resultSet));
            }
        }
        
        return cities;
    }
    
    public List<City> findByCountryId(Connection connection, int countryId) throws SQLException {
        List<City> cities = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_COUNTRY_ID_SQL)) {
            statement.setInt(1, countryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cities.add(extractCityFromResultSet(resultSet));
                }
            }
        }
        
        return cities;
    }
    
    public void update(Connection connection, City city) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, city.getCity());
            
            // Extract country ID from Country object
            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                statement.setInt(2, city.getCountry().getCountryId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            // Use provided LocalDateTime or current time
            if (city.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(city.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(4, city.getCityId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating city failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int cityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, cityId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting city failed, no rows affected.");
            }
        }
    }
    
    private City extractCityFromResultSet(ResultSet resultSet) throws SQLException {
        City city = new City();
        city.setCityId(resultSet.getInt("city_id"));
        city.setCity(resultSet.getString("city"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            city.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        // Create placeholder Country object with ID for service layer to load full object
        int countryId = resultSet.getInt("country_id");
        if (countryId > 0) {
            Country tempCountry = new Country();
            tempCountry.setCountryId(countryId);
            city.setCountry(tempCountry);
        }
        
        return city;
    }
}
