package dao;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO film (title, description, release_year, language_id, original_language_id, " +
        "rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT film_id, title, description, release_year, language_id, original_language_id, " +
        "rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update " +
        "FROM film WHERE film_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT film_id, title, description, release_year, language_id, original_language_id, " +
        "rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update " +
        "FROM film ORDER BY film_id";
    
    private static final String FIND_BY_TITLE_SQL = 
        "SELECT film_id, title, description, release_year, language_id, original_language_id, " +
        "rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update " +
        "FROM film WHERE title ILIKE ?";
    
    private static final String FIND_BY_LANGUAGE_ID_SQL = 
        "SELECT film_id, title, description, release_year, language_id, original_language_id, " +
        "rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update " +
        "FROM film WHERE language_id = ?";
    
    private static final String UPDATE_SQL = 
        "UPDATE film SET title = ?, description = ?, release_year = ?, language_id = ?, " +
        "original_language_id = ?, rental_duration = ?, rental_rate = ?, length = ?, " +
        "replacement_cost = ?, rating = ?, special_features = ?, last_update = ? WHERE film_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM film WHERE film_id = ?";
    
    public int insert(Connection connection, Film film) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, film.getTitle());
            statement.setString(2, film.getDescription());
            statement.setInt(3, film.getReleaseYear());
            
            // Get language ID from Language object (or 0 if null)
            int languageId = (film.getLanguage() != null) ? film.getLanguage().getLanguageId() : 0;
            statement.setInt(4, languageId);
            
            // Get original language ID from Language object (or null if not set)
            if (film.getOriginalLanguage() != null && film.getOriginalLanguage().getLanguageId() > 0) {
                statement.setInt(5, film.getOriginalLanguage().getLanguageId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }
            
            statement.setInt(6, film.getRentalDuration());
            statement.setBigDecimal(7, film.getRentalRate());
            statement.setInt(8, film.getLength());
            statement.setBigDecimal(9, film.getReplacementCost());
            statement.setString(10, film.getRating());
            statement.setString(11, film.getSpecialFeatures());
            statement.setTimestamp(12, Timestamp.valueOf(film.getLastUpdate()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating film failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int filmId = generatedKeys.getInt(1);
                    film.setFilmId(filmId);
                    return filmId;
                } else {
                    throw new SQLException("Creating film failed, no ID obtained.");
                }
            }
        }
    }
    
    public Film findById(Connection connection, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, filmId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractFilmFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Film> findAll(Connection connection) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                films.add(extractFilmFromResultSet(resultSet));
            }
        }
        
        return films;
    }
    
    public List<Film> findByTitle(Connection connection, String title) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_TITLE_SQL)) {
            statement.setString(1, "%" + title + "%");
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    films.add(extractFilmFromResultSet(resultSet));
                }
            }
        }
        
        return films;
    }
    
    public List<Film> findByLanguageId(Connection connection, int languageId) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_LANGUAGE_ID_SQL)) {
            statement.setInt(1, languageId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    films.add(extractFilmFromResultSet(resultSet));
                }
            }
        }
        
        return films;
    }
    
    public void update(Connection connection, Film film) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, film.getTitle());
            statement.setString(2, film.getDescription());
            statement.setInt(3, film.getReleaseYear());
            
            // Get language ID from Language object (or 0 if null)
            int languageId = (film.getLanguage() != null) ? film.getLanguage().getLanguageId() : 0;
            statement.setInt(4, languageId);
            
            // Get original language ID from Language object (or null if not set)
            if (film.getOriginalLanguage() != null && film.getOriginalLanguage().getLanguageId() > 0) {
                statement.setInt(5, film.getOriginalLanguage().getLanguageId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }
            
            statement.setInt(6, film.getRentalDuration());
            statement.setBigDecimal(7, film.getRentalRate());
            statement.setInt(8, film.getLength());
            statement.setBigDecimal(9, film.getReplacementCost());
            statement.setString(10, film.getRating());
            statement.setString(11, film.getSpecialFeatures());
            statement.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
            statement.setInt(13, film.getFilmId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating film failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, filmId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting film failed, no rows affected.");
            }
        }
    }
    
    private Film extractFilmFromResultSet(ResultSet resultSet) throws SQLException {
        Film film = new Film();
        film.setFilmId(resultSet.getInt("film_id"));
        film.setTitle(resultSet.getString("title"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseYear(resultSet.getInt("release_year"));
        film.setRentalDuration(resultSet.getInt("rental_duration"));
        film.setRentalRate(resultSet.getBigDecimal("rental_rate"));
        film.setLength(resultSet.getInt("length"));
        film.setReplacementCost(resultSet.getBigDecimal("replacement_cost"));
        film.setRating(resultSet.getString("rating"));
        film.setSpecialFeatures(resultSet.getString("special_features"));
        film.setLastUpdate(resultSet.getTimestamp("last_update").toLocalDateTime());
        
        // Create placeholder Language objects with IDs for service layer to load full objects
        int languageId = resultSet.getInt("language_id");
        if (languageId > 0) {
            Language tempLanguage = new Language();
            tempLanguage.setLanguageId(languageId);
            film.setLanguage(tempLanguage);
        }
        
        Integer originalLanguageId = resultSet.getObject("original_language_id", Integer.class);
        if (originalLanguageId != null && originalLanguageId > 0) {
            Language tempOriginalLanguage = new Language();
            tempOriginalLanguage.setLanguageId(originalLanguageId);
            film.setOriginalLanguage(tempOriginalLanguage);
        }
        
        return film;
    }
}
