package dao;

import model.Actor;
import java.sql.*;

public class ActorDao {

    private static final String BASE_SELECT_SQL =
        "SELECT actor_id, first_name, last_name, last_update FROM actor";

    private static final String INSERT_SQL =
        "INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)";

    private static final String UPDATE_SQL =
        "UPDATE actor SET first_name = ?, last_name = ?, last_update = ? WHERE actor_id = ?";

    private static final String DELETE_SQL =
        "DELETE FROM actor WHERE actor_id = ?";

    // Insert
    public int insert(Connection connection, Actor actor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, actor.getFirstName());
            statement.setString(2, actor.getLastName());

            if (actor.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Creating actor failed, no rows affected.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int actorId = keys.getInt(1);
                    actor.setActorId(actorId);
                    return actorId;
                } else {
                    throw new SQLException("Creating actor failed, no ID obtained.");
                }
            }
        }
    }

    // Find by ID
    public Actor findById(Connection connection, int actorId) throws SQLException {
        String sql = BASE_SELECT_SQL + " WHERE actor_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, actorId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Actor actor = new Actor();
                    actor.setActorId(rs.getInt("actor_id"));
                    actor.setFirstName(rs.getString("first_name"));
                    actor.setLastName(rs.getString("last_name"));

                    Timestamp lastUpdate = rs.getTimestamp("last_update");
                    if (lastUpdate != null) {
                        actor.setLastUpdate(lastUpdate.toLocalDateTime());
                    }

                    return actor;
                }
                return null;
            }
        }
    }

    // Update
    public void update(Connection connection, Actor actor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, actor.getFirstName());
            statement.setString(2, actor.getLastName());

            if (actor.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }

            statement.setInt(4, actor.getActorId());

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Updating actor failed, no rows affected.");
            }
        }
    }

    // Delete
    public void deleteById(Connection connection, int actorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, actorId);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Deleting actor failed, no rows affected.");
            }
        }
    }
}
