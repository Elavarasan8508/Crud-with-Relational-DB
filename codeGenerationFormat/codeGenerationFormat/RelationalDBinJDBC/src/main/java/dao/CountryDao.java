package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class CountryDao {

    private static final String INSERT_SQL = "INSERT INTO country (country, last_update) VALUES (?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM country WHERE country_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM country ORDER BY country_id";

    private static final String UPDATE_SQL = "UPDATE country SET country = ?, last_update = ? WHERE country_id = ?";

    private static final String DELETE_SQL = "DELETE FROM country WHERE country_id = ?";

    public int insert(Connection conn, Country country) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, country.getCountry());
            ps.setTimestamp(2, Timestamp.valueOf(country.getLastUpdate() != null ? country.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    country.setCountryId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Country findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Country> findAll(Connection conn) throws SQLException {
        List<Country> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Country country) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, country.getCountry());
            ps.setTimestamp(2, Timestamp.valueOf(country.getLastUpdate() != null ? country.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(3, country.getCountryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Country extract(ResultSet rs) throws SQLException {
        Country country = new Country();
        Integer country_id = rs.getObject("country_id", Integer.class);
        country.setCountryId(country_id);
        country.setCountry(rs.getString("country"));
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            country.setLastUpdate(last_update.toLocalDateTime());
        return country;
    }
}
