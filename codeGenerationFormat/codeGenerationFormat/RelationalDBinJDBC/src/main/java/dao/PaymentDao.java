package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class PaymentDao {

    private static final String INSERT_SQL = "INSERT INTO payment (customer_id, staff_id, rental_id, amount, payment_date, last_update) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM payment WHERE payment_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM payment ORDER BY payment_id";

    private static final String SELECT_BY_CUSTOMER_ID_SQL = "SELECT * FROM payment WHERE customer_id = ?";

    private static final String SELECT_BY_RENTAL_ID_SQL = "SELECT * FROM payment WHERE rental_id = ?";

    private static final String SELECT_BY_STAFF_ID_SQL = "SELECT * FROM payment WHERE staff_id = ?";

    private static final String UPDATE_SQL = "UPDATE payment SET customer_id = ?, staff_id = ?, rental_id = ?, amount = ?, payment_date = ?, last_update = ? WHERE payment_id = ?";

    private static final String DELETE_SQL = "DELETE FROM payment WHERE payment_id = ?";

    public int insert(Connection conn, Payment payment) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (payment.getCustomer() != null && payment.getCustomer().getCustomerId() > 0) {
                ps.setInt(1, payment.getCustomer().getCustomerId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (payment.getStaff() != null && payment.getStaff().getStaffId() > 0) {
                ps.setInt(2, payment.getStaff().getStaffId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (payment.getRental() != null && payment.getRental().getRentalId() > 0) {
                ps.setInt(3, payment.getRental().getRentalId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            BigDecimal val4 = payment.getAmount();
            if (val4 != null) {
                ps.setBigDecimal(4, val4);
            } else {
                ps.setNull(4, Types.FLOAT);
            }
            ps.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate() != null ? payment.getPaymentDate() : java.time.LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(payment.getLastUpdate() != null ? payment.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    payment.setPaymentId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Payment findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Payment> findAll(Connection conn) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Payment payment) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (payment.getCustomer() != null && payment.getCustomer().getCustomerId() > 0) {
                ps.setInt(1, payment.getCustomer().getCustomerId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (payment.getStaff() != null && payment.getStaff().getStaffId() > 0) {
                ps.setInt(2, payment.getStaff().getStaffId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (payment.getRental() != null && payment.getRental().getRentalId() > 0) {
                ps.setInt(3, payment.getRental().getRentalId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            BigDecimal val4 = payment.getAmount();
            if (val4 != null) {
                ps.setBigDecimal(4, val4);
            } else {
                ps.setNull(4, Types.FLOAT);
            }
            ps.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate() != null ? payment.getPaymentDate() : java.time.LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(payment.getLastUpdate() != null ? payment.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(7, payment.getPaymentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Payment> findByCustomerId(Connection conn, int customerID) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER_ID_SQL)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Payment> findByRentalId(Connection conn, int rentalID) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_RENTAL_ID_SQL)) {
            ps.setInt(1, rentalID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Payment> findByStaffId(Connection conn, int staffID) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STAFF_ID_SQL)) {
            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Payment extract(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        Integer payment_id = rs.getObject("payment_id", Integer.class);
        payment.setPaymentId(payment_id);
        Integer customer_id = rs.getObject("customer_id", Integer.class);
        payment.setCustomerId(customer_id);
        if (customer_id != null && customer_id > 0) {
            Customer customer = new Customer();
            customer.setCustomerId(customer_id);
            payment.setCustomer(customer);
        }
        Integer staff_id = rs.getObject("staff_id", Integer.class);
        payment.setStaffId(staff_id);
        if (staff_id != null && staff_id > 0) {
            Staff staff = new Staff();
            staff.setStaffId(staff_id);
            payment.setStaff(staff);
        }
        Integer rental_id = rs.getObject("rental_id", Integer.class);
        payment.setRentalId(rental_id);
        if (rental_id != null && rental_id > 0) {
            Rental rental = new Rental();
            rental.setRentalId(rental_id);
            payment.setRental(rental);
        }
        BigDecimal amount = rs.getObject("amount", BigDecimal.class);
        payment.setAmount(amount);
        Timestamp payment_date = rs.getTimestamp("payment_date");
        if (payment_date != null)
            payment.setPaymentDate(payment_date.toLocalDateTime());
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            payment.setLastUpdate(last_update.toLocalDateTime());
        return payment;
    }
}
