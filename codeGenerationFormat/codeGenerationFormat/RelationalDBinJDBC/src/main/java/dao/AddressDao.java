package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class AddressDao {

    private static final String INSERT_SQL = "INSERT INTO address (address, address2, district, city_id, postal_code, phone, last_update) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM address WHERE address_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM address ORDER BY address_id";

    private static final String SELECT_BY_CITY_ID_SQL = "SELECT * FROM address WHERE city_id = ?";

    private static final String UPDATE_SQL = "UPDATE address SET address = ?, address2 = ?, district = ?, city_id = ?, postal_code = ?, phone = ?, last_update = ? WHERE address_id = ?";

    private static final String DELETE_SQL = "DELETE FROM address WHERE address_id = ?";

    public int insert(Connection conn, Address address) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, address.getAddress());
            ps.setString(2, address.getAddress2());
            ps.setString(3, address.getDistrict());
            if (address.getCity() != null && address.getCity().getCityId() > 0) {
                ps.setInt(4, address.getCity().getCityId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, address.getPostalCode());
            ps.setString(6, address.getPhone());
            ps.setTimestamp(7, Timestamp.valueOf(address.getLastUpdate() != null ? address.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    address.setAddressId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Address findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Address> findAll(Connection conn) throws SQLException {
        List<Address> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Address address) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, address.getAddress());
            ps.setString(2, address.getAddress2());
            ps.setString(3, address.getDistrict());
            if (address.getCity() != null && address.getCity().getCityId() > 0) {
                ps.setInt(4, address.getCity().getCityId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, address.getPostalCode());
            ps.setString(6, address.getPhone());
            ps.setTimestamp(7, Timestamp.valueOf(address.getLastUpdate() != null ? address.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(8, address.getAddressId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Address> findByCityId(Connection conn, int cityID) throws SQLException {
        List<Address> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CITY_ID_SQL)) {
            ps.setInt(1, cityID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Address extract(ResultSet rs) throws SQLException {
        Address address = new Address();
        Integer address_id = rs.getObject("address_id", Integer.class);
        address.setAddressId(address_id);
        address.setAddress(rs.getString("address"));
        address.setAddress2(rs.getString("address2"));
        address.setDistrict(rs.getString("district"));
        Integer city_id = rs.getObject("city_id", Integer.class);
        address.setCityId(city_id);
        if (city_id != null && city_id > 0) {
            City city = new City();
            city.setCityId(city_id);
            address.setCity(city);
        }
        address.setPostalCode(rs.getString("postal_code"));
        address.setPhone(rs.getString("phone"));
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            address.setLastUpdate(last_update.toLocalDateTime());
        return address;
    }
}
