package dao;

import model.Country;
import java.sql.*;
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
    

    // Create
    public int insert(Connection connection, Country country) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, country.getCountry());
            statement.setTimestamp(2, country.getLastUpdate() != null
                    ? Timestamp.valueOf(country.getLastUpdate())
                    : new Timestamp(System.currentTimeMillis()));

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Inserting country failed, no rows affected.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    country.setCountryId(id);
                    return id;
                } else {
                    throw new SQLException("Inserting country failed, no ID obtained.");
                }
            }
        }
    }

    // Read (by ID)
    public Country findById(Connection connection, int countryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, countryId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Country country = new Country();
                    country.setCountryId(rs.getInt("country_id"));
                    country.setCountry(rs.getString("country"));
                    Timestamp ts = rs.getTimestamp("last_update");
                    if (ts != null) country.setLastUpdate(ts.toLocalDateTime());
                    return country;
                }
                return null;
            }
        }
    }

    // Read (all)
    public List<Country> findAll(Connection connection) throws SQLException {
        List<Country> countries = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            
            while (rs.next()) {
                Country country = new Country();
                country.setCountryId(rs.getInt("country_id"));
                country.setCountry(rs.getString("country"));
                Timestamp ts = rs.getTimestamp("last_update");
                if (ts != null) country.setLastUpdate(ts.toLocalDateTime());
                countries.add(country);
            }
        }
        return countries;
    }

    // Update
    public void update(Connection connection, Country country) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, country.getCountry());
            statement.setTimestamp(2, country.getLastUpdate() != null
                    ? Timestamp.valueOf(country.getLastUpdate())
                    : new Timestamp(System.currentTimeMillis()));
            statement.setInt(3, country.getCountryId());

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Updating country failed, no rows affected.");
            }
        }
    }

    // Delete
    public void deleteById(Connection connection, int countryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, countryId);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Deleting country failed, no rows affected.");
            }
        }
    }
}
