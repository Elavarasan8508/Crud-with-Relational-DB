package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Actor;
import model.Film;
import model.FilmActor;

public class FilmActorDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO film_actor (actor_id, film_id, last_update) VALUES (?, ?, ?)";
    
    private static final String FIND_BY_FILM_ID_SQL = 
        "SELECT actor_id, film_id, last_update FROM film_actor WHERE film_id = ?";
    
    private static final String FIND_BY_ACTOR_ID_SQL = 
        "SELECT actor_id, film_id, last_update FROM film_actor WHERE actor_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM film_actor WHERE actor_id = ? AND film_id = ?";
    
    // Insert with IDs (for backward compatibility)
    public void insert(Connection connection, int actorId, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, actorId);
            statement.setInt(2, filmId);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating film-actor relationship failed, no rows affected.");
            }
        }
    }
    
 // Insert with FilmActor object - FIXED to return and set ID
    public int insert(Connection connection, FilmActor filmActor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Extract IDs from objects
            int actorId = filmActor.getActor() != null ? filmActor.getActor().getActorId() : filmActor.getActor().getActorId();
            int filmId = filmActor.getFilm() != null ? filmActor.getFilm().getFilmId() : filmActor.getFilm().getFilmId();
            
            statement.setInt(1, actorId);
            statement.setInt(2, filmId);
            
            // Use provided LocalDateTime or current time
            if (filmActor.getLastUpdate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(filmActor.getLastUpdate()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating film-actor relationship failed, no rows affected.");
            }
            
            //  FIXED: Get and set the generated ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int filmActorId = generatedKeys.getInt(1);
                    filmActor.setFilmActorId(filmActorId);
                    return filmActorId;
                } else {
                    throw new SQLException("Creating film-actor relationship failed, no ID obtained.");
                }
            }
        }
    }

    
    public List<FilmActor> findByFilmId(Connection connection, int filmId) throws SQLException {
        List<FilmActor> filmActors = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_FILM_ID_SQL)) {
            statement.setInt(1, filmId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    filmActors.add(extractFilmActorFromResultSet(resultSet));
                }
            }
        }
        
        return filmActors;
    }
    
    public List<FilmActor> findByActorId(Connection connection, int actorId) throws SQLException {
        List<FilmActor> filmActors = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ACTOR_ID_SQL)) {
            statement.setInt(1, actorId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    filmActors.add(extractFilmActorFromResultSet(resultSet));
                }
            }
        }
        
        return filmActors;
    }
    
    public void delete(Connection connection, int actorId, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, actorId);
            statement.setInt(2, filmId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting film-actor relationship failed, no rows affected.");
            }
        }
    }
    
    // Delete with FilmActor object
    public void delete(Connection connection, FilmActor filmActor) throws SQLException {
        int actorId = filmActor.getActor() != null ? filmActor.getActor().getActorId() : filmActor.getActor().getActorId();
        int filmId = filmActor.getFilm() != null ? filmActor.getFilm().getFilmId() : filmActor.getFilm().getFilmId();
        
        delete(connection, actorId, filmId);
    }
    
    private FilmActor extractFilmActorFromResultSet(ResultSet resultSet) throws SQLException {
        FilmActor filmActor = new FilmActor();
        
        // Set IDs directly
        int actorId = resultSet.getInt("actor_id");
        int filmId = resultSet.getInt("film_id");
        
        // Create placeholder objects with IDs for service layer to load full objects
        if (actorId > 0) {
            Actor tempActor = new Actor();
            tempActor.setActorId(actorId);
            filmActor.setActor(tempActor);
        }
        
        if (filmId > 0) {
            Film tempFilm = new Film();
            tempFilm.setFilmId(filmId);
            filmActor.setFilm(tempFilm);
        }
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            filmActor.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return filmActor;
    }
}
