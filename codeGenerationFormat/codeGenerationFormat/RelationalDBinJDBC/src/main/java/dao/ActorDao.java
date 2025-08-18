package dao;

import model.Actor;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActorDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT actor_id, first_name, last_name, last_update FROM actor WHERE actor_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT actor_id, first_name, last_name, last_update FROM actor ORDER BY actor_id";
    
    private static final String FIND_BY_NAME_SQL = 
        "SELECT actor_id, first_name, last_name, last_update FROM actor " +
        "WHERE first_name ILIKE ? AND last_name ILIKE ?";
    
    private static final String UPDATE_SQL = 
        "UPDATE actor SET first_name = ?, last_name = ?, last_update = ? WHERE actor_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM actor WHERE actor_id = ?";
    
    public int insert(Connection connection, Actor actor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, actor.getFirstName());
            statement.setString(2, actor.getLastName());
            
            // Use provided LocalDateTime or current time
            if (actor.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating actor failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int actorId = generatedKeys.getInt(1);
                    actor.setActorId(actorId);
                    return actorId;
                } else {
                    throw new SQLException("Creating actor failed, no ID obtained.");
                }
            }
        }
    }
    
    public Actor findById(Connection connection, int actorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, actorId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractActorFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Actor> findAll(Connection connection) throws SQLException {
        List<Actor> actors = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                actors.add(extractActorFromResultSet(resultSet));
            }
        }
        
        return actors;
    }
    
    public List<Actor> findByName(Connection connection, String firstName, String lastName) throws SQLException {
        List<Actor> actors = new ArrayList<>();
        String sql = "SELECT actor_id, first_name, last_name, last_update FROM actor WHERE first_name ILIKE ? AND last_name ILIKE ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    actors.add(extractActorFromResultSet(resultSet));
                }
            }
        }
        
        return actors;
    }

    
    public void update(Connection connection, Actor actor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, actor.getFirstName());
            statement.setString(2, actor.getLastName());
            
            // Use provided LocalDateTime or current time
            if (actor.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(actor.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(4, actor.getActorId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating actor failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int actorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, actorId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting actor failed, no rows affected.");
            }
        }
    }
    
    private Actor extractActorFromResultSet(ResultSet resultSet) throws SQLException {
        Actor actor = new Actor();
        actor.setActorId(resultSet.getInt("actor_id"));
        actor.setFirstName(resultSet.getString("first_name"));
        actor.setLastName(resultSet.getString("last_name"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            actor.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return actor;
    }
}
