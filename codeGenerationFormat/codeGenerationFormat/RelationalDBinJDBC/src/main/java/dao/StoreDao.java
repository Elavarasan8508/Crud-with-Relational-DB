package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class StoreDao {

    private static final String INSERT_SQL = "INSERT INTO store (manager_staff_id, address_id, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM store WHERE store_id = ?";
    
    private static final String SELECT_ALL_SQL = "SELECT * FROM store ORDER BY store_id";

    private static final String SELECT_BY_ADDRESS_ID_SQL = "SELECT * FROM store WHERE address_id = ?";

    private static final String SELECT_BY_MANAGER_STAFF_ID_SQL = "SELECT * FROM store WHERE manager_staff_id = ?";

    private static final String UPDATE_SQL = "UPDATE store SET manager_staff_id = ?, address_id = ?, last_update = ? WHERE store_id = ?";

    private static final String DELETE_SQL = "DELETE FROM store WHERE store_id = ?";

    public int insert(Connection conn, Store store) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (store.getManagerStaff() != null && store.getManagerStaff().getStaffId() > 0) {
                ps.setInt(1, store.getManagerStaff().getStaffId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (store.getAddress() != null && store.getAddress().getAddressId() > 0) {
                ps.setInt(2, store.getAddress().getAddressId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(store.getLastUpdate() != null ? store.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    store.setStoreId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Store findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Store> findAll(Connection conn) throws SQLException {
        List<Store> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Store store) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (store.getManagerStaff() != null && store.getManagerStaff().getStaffId() > 0) {
                ps.setInt(1, store.getManagerStaff().getStaffId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (store.getAddress() != null && store.getAddress().getAddressId() > 0) {
                ps.setInt(2, store.getAddress().getAddressId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(store.getLastUpdate() != null ? store.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(4, store.getStoreId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Store> findByAddressId(Connection conn, int addressID) throws SQLException {
        List<Store> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ADDRESS_ID_SQL)) {
            ps.setInt(1, addressID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Store> findByManagerStaffId(Connection conn, int managerStaffID) throws SQLException {
        List<Store> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_MANAGER_STAFF_ID_SQL)) {
            ps.setInt(1, managerStaffID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Store extract(ResultSet rs) throws SQLException {
        Store store = new Store();
        Integer store_id = rs.getObject("store_id", Integer.class);
        store.setStoreId(store_id);
        Integer manager_staff_id = rs.getObject("manager_staff_id", Integer.class);
        store.setManagerStaffId(manager_staff_id);
        if (manager_staff_id != null && manager_staff_id > 0) {
            Staff managerStaff = new Staff();
            managerStaff.setStaffId(manager_staff_id);
            store.setManagerStaff(managerStaff);
        }
        Integer address_id = rs.getObject("address_id", Integer.class);
        store.setAddressId(address_id);
        if (address_id != null && address_id > 0) {
            Address address = new Address();
            address.setAddressId(address_id);
            store.setAddress(address);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            store.setLastUpdate(last_update.toLocalDateTime());
        return store;
    }
}
