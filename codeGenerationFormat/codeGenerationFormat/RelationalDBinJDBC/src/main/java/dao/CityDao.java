package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class CityDao {

    private static final String INSERT_SQL = "INSERT INTO city (city, country_id, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM city WHERE city_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM city ORDER BY city_id";

    private static final String SELECT_BY_COUNTRY_ID_SQL = "SELECT * FROM city WHERE country_id = ?";

    private static final String UPDATE_SQL = "UPDATE city SET city = ?, country_id = ?, last_update = ? WHERE city_id = ?";

    private static final String DELETE_SQL = "DELETE FROM city WHERE city_id = ?";

    public int insert(Connection conn, City city) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, city.getCity());
            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                ps.setInt(2, city.getCountry().getCountryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(city.getLastUpdate() != null ? city.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    city.setCityId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public City findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<City> findAll(Connection conn) throws SQLException {
        List<City> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, City city) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, city.getCity());
            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                ps.setInt(2, city.getCountry().getCountryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(city.getLastUpdate() != null ? city.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(4, city.getCityId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<City> findByCountryId(Connection conn, int countryID) throws SQLException {
        List<City> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_COUNTRY_ID_SQL)) {
            ps.setInt(1, countryID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private City extract(ResultSet rs) throws SQLException {
        City city = new City();
        Integer city_id = rs.getObject("city_id", Integer.class);
        city.setCityId(city_id);
        city.setCity(rs.getString("city"));
        Integer country_id = rs.getObject("country_id", Integer.class);
        city.setCountryId(country_id);
        if (country_id != null && country_id > 0) {
            Country country = new Country();
            country.setCountryId(country_id);
            city.setCountry(country);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            city.setLastUpdate(last_update.toLocalDateTime());
        return city;
    }
}
