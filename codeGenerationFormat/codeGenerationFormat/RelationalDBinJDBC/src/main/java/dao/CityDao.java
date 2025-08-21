package dao;

import model.City;
import model.Country;

import java.sql.*;
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

    // Insert
    public int insert(Connection connection, City city) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, city.getCity());

            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                statement.setInt(2, city.getCountry().getCountryId());
            } else {
                statement.setNull(2, Types.INTEGER);
            }

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

    // Find by ID
    public City findById(Connection connection, int cityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, cityId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    City city = new City();
                    city.setCityId(rs.getInt("city_id"));
                    city.setCity(rs.getString("city"));
                    Timestamp lastUpdate = rs.getTimestamp("last_update");
                    if (lastUpdate != null) {
                        city.setLastUpdate(lastUpdate.toLocalDateTime());
                    }
                    int countryId = rs.getInt("country_id");
                    if (countryId > 0) {
                        Country country = new Country();
                        country.setCountryId(countryId);
                        city.setCountry(country);
                    }
                    return city;
                }
            }
        }
        return null;
    }

    // Find all
    public List<City> findAll(Connection connection) throws SQLException {
        List<City> cities = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                City city = new City();
                city.setCityId(rs.getInt("city_id"));
                city.setCity(rs.getString("city"));
                Timestamp lastUpdate = rs.getTimestamp("last_update");
                if (lastUpdate != null) {
                    city.setLastUpdate(lastUpdate.toLocalDateTime());
                }
                int countryId = rs.getInt("country_id");
                if (countryId > 0) {
                    Country country = new Country();
                    country.setCountryId(countryId);
                    city.setCountry(country);
                }
                cities.add(city);
            }
        }
        return cities;
    }

    // Find by country_id
    public List<City> findByCountryId(Connection connection, int countryId) throws SQLException {
        List<City> cities = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_COUNTRY_ID_SQL)) {
            statement.setInt(1, countryId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    City city = new City();
                    city.setCityId(rs.getInt("city_id"));
                    city.setCity(rs.getString("city"));
                    Timestamp lastUpdate = rs.getTimestamp("last_update");
                    if (lastUpdate != null) {
                        city.setLastUpdate(lastUpdate.toLocalDateTime());
                    }
                    Country country = new Country();
                    country.setCountryId(rs.getInt("country_id"));
                    city.setCountry(country);
                    cities.add(city);
                }
            }
        }
        return cities;
    }

    // Update
    public void update(Connection connection, City city) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, city.getCity());
            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                statement.setInt(2, city.getCountry().getCountryId());
            } else {
                statement.setNull(2, Types.INTEGER);
            }
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

    // Delete
    public void deleteById(Connection connection, int cityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, cityId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting city failed, no rows affected.");
            }
        }
    }
}
