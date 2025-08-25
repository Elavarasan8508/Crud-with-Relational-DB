package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class StaffDao {

    private static final String INSERT_SQL = "INSERT INTO staff (first_name, last_name, address_id, email, store_id, active, username, password, last_update, picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM staff WHERE staff_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM staff ORDER BY staff_id";

    private static final String SELECT_BY_ADDRESS_ID_SQL = "SELECT * FROM staff WHERE address_id = ?";

    private static final String SELECT_BY_STORE_ID_SQL = "SELECT * FROM staff WHERE store_id = ?";

    private static final String UPDATE_SQL = "UPDATE staff SET first_name = ?, last_name = ?, address_id = ?, email = ?, store_id = ?, active = ?, username = ?, password = ?, last_update = ?, picture = ? WHERE staff_id = ?";

    private static final String DELETE_SQL = "DELETE FROM staff WHERE staff_id = ?";

    public int insert(Connection conn, Staff staff) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getFirstName());
            ps.setString(2, staff.getLastName());
            if (staff.getAddress() != null && staff.getAddress().getAddressId() > 0) {
                ps.setInt(3, staff.getAddress().getAddressId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, staff.getEmail());
            if (staff.getStore() != null && staff.getStore().getStoreId() > 0) {
                ps.setInt(5, staff.getStore().getStoreId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setBoolean(6, staff.getActive());
            ps.setString(7, staff.getUsername());
            ps.setString(8, staff.getPassword());
            ps.setTimestamp(9, Timestamp.valueOf(staff.getLastUpdate() != null ? staff.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setString(10, staff.getPicture());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    staff.setStaffId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Staff findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Staff> findAll(Connection conn) throws SQLException {
        List<Staff> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Staff staff) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, staff.getFirstName());
            ps.setString(2, staff.getLastName());
            if (staff.getAddress() != null && staff.getAddress().getAddressId() > 0) {
                ps.setInt(3, staff.getAddress().getAddressId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, staff.getEmail());
            if (staff.getStore() != null && staff.getStore().getStoreId() > 0) {
                ps.setInt(5, staff.getStore().getStoreId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setBoolean(6, staff.getActive());
            ps.setString(7, staff.getUsername());
            ps.setString(8, staff.getPassword());
            ps.setTimestamp(9, Timestamp.valueOf(staff.getLastUpdate() != null ? staff.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setString(10, staff.getPicture());
            ps.setInt(11, staff.getStaffId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Staff> findByAddressId(Connection conn, int addressID) throws SQLException {
        List<Staff> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ADDRESS_ID_SQL)) {
            ps.setInt(1, addressID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Staff> findByStoreId(Connection conn, int storeID) throws SQLException {
        List<Staff> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STORE_ID_SQL)) {
            ps.setInt(1, storeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Staff extract(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        Integer staff_id = rs.getObject("staff_id", Integer.class);
        staff.setStaffId(staff_id);
        staff.setFirstName(rs.getString("first_name"));
        staff.setLastName(rs.getString("last_name"));
        Integer address_id = rs.getObject("address_id", Integer.class);
        staff.setAddressId(address_id);
        if (address_id != null && address_id > 0) {
            Address address = new Address();
            address.setAddressId(address_id);
            staff.setAddress(address);
        }
        staff.setEmail(rs.getString("email"));
        Integer store_id = rs.getObject("store_id", Integer.class);
        staff.setStoreId(store_id);
        if (store_id != null && store_id > 0) {
            Store store = new Store();
            store.setStoreId(store_id);
            staff.setStore(store);
        }
        staff.setActive(rs.getBoolean("active"));
        staff.setUsername(rs.getString("username"));
        staff.setPassword(rs.getString("password"));
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            staff.setLastUpdate(last_update.toLocalDateTime());
        staff.setPicture(rs.getString("picture"));
        return staff;
    }
}
