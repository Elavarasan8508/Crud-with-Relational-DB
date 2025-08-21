package dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {

    private static final String INSERT_SQL =
        "INSERT INTO address (address, address2, district, city_id, postal_code, phone, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
        "SELECT * FROM address WHERE address_id = ?";

    private static final String SELECT_ALL_SQL =
        "SELECT * FROM address ORDER BY address_id";

    private static final String SELECT_BY_CITY_ID_SQL =
        "SELECT * FROM address WHERE city_id = ?";

    private static final String UPDATE_SQL =
        "UPDATE address SET address = ?, address2 = ?, district = ?, city_id = ?, postal_code = ?, " +
        "phone = ?, last_update = ? WHERE address_id = ?";

    private static final String DELETE_SQL =
        "DELETE FROM address WHERE address_id = ?";

    // ✅ Insert
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

            ps.setTimestamp(7, Timestamp.valueOf(
                address.getLastUpdate() != null ? address.getLastUpdate() : java.time.LocalDateTime.now()
            ));

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

    // ✅ Find by ID
    public Address findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    // ✅ Find all
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

    // ✅ Find by city
    public List<Address> findByCityId(Connection conn, int cityId) throws SQLException {
        List<Address> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CITY_ID_SQL)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    // ✅ Update
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

            ps.setTimestamp(7, Timestamp.valueOf(
                address.getLastUpdate() != null ? address.getLastUpdate() : java.time.LocalDateTime.now()
            ));

            ps.setInt(8, address.getAddressId());

            return ps.executeUpdate() > 0;
        }
    }

    // ✅ Delete
    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🔹 Extract object from ResultSet
    private Address extract(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setAddressId(rs.getInt("address_id"));
        a.setAddress(rs.getString("address"));
        a.setAddress2(rs.getString("address2"));
        a.setDistrict(rs.getString("district"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setPhone(rs.getString("phone"));

        Timestamp ts = rs.getTimestamp("last_update");
        if (ts != null) a.setLastUpdate(ts.toLocalDateTime());

        int cityId = rs.getInt("city_id");
        if (cityId > 0) {
            City c = new City();
            c.setCityId(cityId);
            a.setCity(c);
        }
        return a;
    }
}
