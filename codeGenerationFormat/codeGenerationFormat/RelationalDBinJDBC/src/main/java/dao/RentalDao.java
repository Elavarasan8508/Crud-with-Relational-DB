package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class RentalDao {

    private static final String INSERT_SQL = "INSERT INTO rental (rental_date, inventory_id, customer_id, return_date, staff_id, last_update) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM rental WHERE rental_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM rental ORDER BY rental_id";

    private static final String SELECT_BY_CUSTOMER_ID_SQL = "SELECT * FROM rental WHERE customer_id = ?";

    private static final String SELECT_BY_INVENTORY_ID_SQL = "SELECT * FROM rental WHERE inventory_id = ?";

    private static final String SELECT_BY_STAFF_ID_SQL = "SELECT * FROM rental WHERE staff_id = ?";

    private static final String UPDATE_SQL = "UPDATE rental SET rental_date = ?, inventory_id = ?, customer_id = ?, return_date = ?, staff_id = ?, last_update = ? WHERE rental_id = ?";

    private static final String DELETE_SQL = "DELETE FROM rental WHERE rental_id = ?";

    public int insert(Connection conn, Rental rental) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(rental.getRentalDate() != null ? rental.getRentalDate() : java.time.LocalDateTime.now()));
            if (rental.getInventory() != null && rental.getInventory().getInventoryId() > 0) {
                ps.setInt(2, rental.getInventory().getInventoryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (rental.getCustomer() != null && rental.getCustomer().getCustomerId() > 0) {
                ps.setInt(3, rental.getCustomer().getCustomerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setTimestamp(4, Timestamp.valueOf(rental.getReturnDate() != null ? rental.getReturnDate() : java.time.LocalDateTime.now()));
            if (rental.getStaff() != null && rental.getStaff().getStaffId() > 0) {
                ps.setInt(5, rental.getStaff().getStaffId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setTimestamp(6, Timestamp.valueOf(rental.getLastUpdate() != null ? rental.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    rental.setRentalId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Rental findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Rental> findAll(Connection conn) throws SQLException {
        List<Rental> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Rental rental) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setTimestamp(1, Timestamp.valueOf(rental.getRentalDate() != null ? rental.getRentalDate() : java.time.LocalDateTime.now()));
            if (rental.getInventory() != null && rental.getInventory().getInventoryId() > 0) {
                ps.setInt(2, rental.getInventory().getInventoryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (rental.getCustomer() != null && rental.getCustomer().getCustomerId() > 0) {
                ps.setInt(3, rental.getCustomer().getCustomerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setTimestamp(4, Timestamp.valueOf(rental.getReturnDate() != null ? rental.getReturnDate() : java.time.LocalDateTime.now()));
            if (rental.getStaff() != null && rental.getStaff().getStaffId() > 0) {
                ps.setInt(5, rental.getStaff().getStaffId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setTimestamp(6, Timestamp.valueOf(rental.getLastUpdate() != null ? rental.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(7, rental.getRentalId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Rental> findByCustomerId(Connection conn, int customerID) throws SQLException {
        List<Rental> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_ID_SQL)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Rental> findByInventoryId(Connection conn, int inventoryID) throws SQLException {
        List<Rental> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_INVENTORY_ID_SQL)) {
            ps.setInt(1, inventoryID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Rental> findByStaffId(Connection conn, int staffID) throws SQLException {
        List<Rental> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STAFF_ID_SQL)) {
            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Rental extract(ResultSet rs) throws SQLException {
        Rental rental = new Rental();
        Integer rental_id = rs.getObject("rental_id", Integer.class);
        rental.setRentalId(rental_id);
        Timestamp rental_date = rs.getTimestamp("rental_date");
        if (rental_date != null)
            rental.setRentalDate(rental_date.toLocalDateTime());
        Integer inventory_id = rs.getObject("inventory_id", Integer.class);
        rental.setInventoryId(inventory_id);
        if (inventory_id != null && inventory_id > 0) {
            Inventory inventory = new Inventory();
            inventory.setInventoryId(inventory_id);
            rental.setInventory(inventory);
        }
        Integer customer_id = rs.getObject("customer_id", Integer.class);
        rental.setCustomerId(customer_id);
        if (customer_id != null && customer_id > 0) {
            Customer customer = new Customer();
            customer.setCustomerId(customer_id);
            rental.setCustomer(customer);
        }
        Timestamp return_date = rs.getTimestamp("return_date");
        if (return_date != null)
            rental.setReturnDate(return_date.toLocalDateTime());
        Integer staff_id = rs.getObject("staff_id", Integer.class);
        rental.setStaffId(staff_id);
        if (staff_id != null && staff_id > 0) {
            Staff staff = new Staff();
            staff.setStaffId(staff_id);
            rental.setStaff(staff);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            rental.setLastUpdate(last_update.toLocalDateTime());
        return rental;
    }
}
