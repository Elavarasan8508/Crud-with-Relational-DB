package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class CustomerDao {

    private static final String INSERT_SQL = "INSERT INTO customer (store_id, first_name, last_name, email, address_id, active, create_date, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM customer WHERE customer_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM customer ORDER BY customer_id";

    private static final String SELECT_BY_ADDRESS_ID_SQL = "SELECT * FROM customer WHERE address_id = ?";

    private static final String SELECT_BY_STORE_ID_SQL = "SELECT * FROM customer WHERE store_id = ?";

    private static final String UPDATE_SQL = "UPDATE customer SET store_id = ?, first_name = ?, last_name = ?, email = ?, address_id = ?, active = ?, create_date = ?, last_update = ? WHERE customer_id = ?";

    private static final String DELETE_SQL = "DELETE FROM customer WHERE customer_id = ?";

    public int insert(Connection conn, Customer customer) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (customer.getStore() != null && customer.getStore().getStoreId() > 0) {
                ps.setInt(1, customer.getStore().getStoreId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getLastName());
            ps.setString(4, customer.getEmail());
            if (customer.getAddress() != null && customer.getAddress().getAddressId() > 0) {
                ps.setInt(5, customer.getAddress().getAddressId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setBoolean(6, customer.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(customer.getCreateDate() != null ? customer.getCreateDate() : java.time.LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(customer.getLastUpdate() != null ? customer.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setCustomerId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Customer findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Customer> findAll(Connection conn) throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Customer customer) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (customer.getStore() != null && customer.getStore().getStoreId() > 0) {
                ps.setInt(1, customer.getStore().getStoreId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getLastName());
            ps.setString(4, customer.getEmail());
            if (customer.getAddress() != null && customer.getAddress().getAddressId() > 0) {
                ps.setInt(5, customer.getAddress().getAddressId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setBoolean(6, customer.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(customer.getCreateDate() != null ? customer.getCreateDate() : java.time.LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(customer.getLastUpdate() != null ? customer.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(9, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Customer> findByAddressId(Connection conn, int addressID) throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ADDRESS_ID_SQL)) {
            ps.setInt(1, addressID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Customer> findByStoreId(Connection conn, int storeID) throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STORE_ID_SQL)) {
            ps.setInt(1, storeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Customer extract(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        Integer customer_id = rs.getObject("customer_id", Integer.class);
        customer.setCustomerId(customer_id);
        Integer store_id = rs.getObject("store_id", Integer.class);
        customer.setStoreId(store_id);
        if (store_id != null && store_id > 0) {
            Store store = new Store();
            store.setStoreId(store_id);
            customer.setStore(store);
        }
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        Integer address_id = rs.getObject("address_id", Integer.class);
        customer.setAddressId(address_id);
        if (address_id != null && address_id > 0) {
            Address address = new Address();
            address.setAddressId(address_id);
            customer.setAddress(address);
        }
        customer.setActive(rs.getBoolean("active"));
        Timestamp create_date = rs.getTimestamp("create_date");
        if (create_date != null)
            customer.setCreateDate(create_date.toLocalDateTime());
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            customer.setLastUpdate(last_update.toLocalDateTime());
        return customer;
    }
}
