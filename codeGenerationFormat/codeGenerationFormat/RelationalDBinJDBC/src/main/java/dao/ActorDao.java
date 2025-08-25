package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.*;


public class ActorDao {

    private static final String INSERT_SQL = "INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM actor WHERE actor_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM actor ORDER BY actor_id";

    private static final String UPDATE_SQL = "UPDATE actor SET first_name = ?, last_name = ?, last_update = ? WHERE actor_id = ?";

    private static final String DELETE_SQL = "DELETE FROM actor WHERE actor_id = ?";

    public int insert(Connection conn, Actor actor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, actor.getFirstName());
            ps.setString(2, actor.getLastName());
            ps.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate() != null ? actor.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    actor.setActorId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Actor findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Actor> findAll(Connection conn) throws SQLException {
        List<Actor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Actor actor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, actor.getFirstName());
            ps.setString(2, actor.getLastName());
            ps.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate() != null ? actor.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(4, actor.getActorId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Actor extract(ResultSet rs) throws SQLException {
        Actor actor = new Actor();
        Integer actor_id = rs.getObject("actor_id", Integer.class);
        actor.setActorId(actor_id);
        actor.setFirstName(rs.getString("first_name"));
        actor.setLastName(rs.getString("last_name"));
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            actor.setLastUpdate(last_update.toLocalDateTime());
        return actor;
    }
}
