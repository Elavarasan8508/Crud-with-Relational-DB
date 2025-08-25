package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class InventoryDao {

    private static final String INSERT_SQL = "INSERT INTO inventory (film_id, store_id, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM inventory WHERE inventory_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM inventory ORDER BY inventory_id";

    private static final String SELECT_BY_FILM_ID_SQL = "SELECT * FROM inventory WHERE film_id = ?";

    private static final String SELECT_BY_STORE_ID_SQL = "SELECT * FROM inventory WHERE store_id = ?";

    private static final String UPDATE_SQL = "UPDATE inventory SET film_id = ?, store_id = ?, last_update = ? WHERE inventory_id = ?";

    private static final String DELETE_SQL = "DELETE FROM inventory WHERE inventory_id = ?";

    public int insert(Connection conn, Inventory inventory) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (inventory.getFilm() != null && inventory.getFilm().getFilmId() > 0) {
                ps.setInt(1, inventory.getFilm().getFilmId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (inventory.getStore() != null && inventory.getStore().getStoreId() > 0) {
                ps.setInt(2, inventory.getStore().getStoreId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(inventory.getLastUpdate() != null ? inventory.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    inventory.setInventoryId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Inventory findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Inventory> findAll(Connection conn) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Inventory inventory) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (inventory.getFilm() != null && inventory.getFilm().getFilmId() > 0) {
                ps.setInt(1, inventory.getFilm().getFilmId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (inventory.getStore() != null && inventory.getStore().getStoreId() > 0) {
                ps.setInt(2, inventory.getStore().getStoreId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(inventory.getLastUpdate() != null ? inventory.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(4, inventory.getInventoryId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Inventory> findByFilmId(Connection conn, int filmID) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_FILM_ID_SQL)) {
            ps.setInt(1, filmID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Inventory> findByStoreId(Connection conn, int storeID) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_STORE_ID_SQL)) {
            ps.setInt(1, storeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Inventory extract(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        Integer inventory_id = rs.getObject("inventory_id", Integer.class);
        inventory.setInventoryId(inventory_id);
        Integer film_id = rs.getObject("film_id", Integer.class);
        inventory.setFilmId(film_id);
        if (film_id != null && film_id > 0) {
            Film film = new Film();
            film.setFilmId(film_id);
            inventory.setFilm(film);
        }
        Integer store_id = rs.getObject("store_id", Integer.class);
        inventory.setStoreId(store_id);
        if (store_id != null && store_id > 0) {
            Store store = new Store();
            store.setStoreId(store_id);
            inventory.setStore(store);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            inventory.setLastUpdate(last_update.toLocalDateTime());
        return inventory;
    }
}
