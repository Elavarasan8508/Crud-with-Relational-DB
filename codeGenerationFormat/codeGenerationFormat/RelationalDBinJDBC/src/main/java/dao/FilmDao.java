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
    
    // Insert a new film
    public int insert(Connection connection, Film film) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, film.getTitle());
            statement.setString(2, film.getDescription());
            statement.setInt(3, film.getReleaseYear());
            
            int languageId = (film.getLanguage() != null) ? film.getLanguage().getLanguageId() : 0;
            statement.setInt(4, languageId);
            
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
    
    // Find film by ID
    public Film findById(Connection connection, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, filmId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Film film = new Film();
                    film.setFilmId(rs.getInt("film_id"));
                    film.setTitle(rs.getString("title"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseYear(rs.getInt("release_year"));
                    film.setRentalDuration(rs.getInt("rental_duration"));
                    film.setRentalRate(rs.getBigDecimal("rental_rate"));
                    film.setLength(rs.getInt("length"));
                    film.setReplacementCost(rs.getBigDecimal("replacement_cost"));
                    film.setRating(rs.getString("rating"));
                    film.setSpecialFeatures(rs.getString("special_features"));
                    film.setLastUpdate(rs.getTimestamp("last_update").toLocalDateTime());

                    int languageId = rs.getInt("language_id");
                    if (languageId > 0) {
                        Language lang = new Language();
                        lang.setLanguageId(languageId);
                        film.setLanguage(lang);
                    }

                    Integer originalLanguageId = rs.getObject("original_language_id", Integer.class);
                    if (originalLanguageId != null && originalLanguageId > 0) {
                        Language origLang = new Language();
                        origLang.setLanguageId(originalLanguageId);
                        film.setOriginalLanguage(origLang);
                    }

                    return film;
                }
                return null;
            }
        }
    }
    
    // Find all films
    public List<Film> findAll(Connection connection) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            
            while (rs.next()) {
                Film film = new Film();
                film.setFilmId(rs.getInt("film_id"));
                film.setTitle(rs.getString("title"));
                film.setDescription(rs.getString("description"));
                film.setReleaseYear(rs.getInt("release_year"));
                film.setRentalDuration(rs.getInt("rental_duration"));
                film.setRentalRate(rs.getBigDecimal("rental_rate"));
                film.setLength(rs.getInt("length"));
                film.setReplacementCost(rs.getBigDecimal("replacement_cost"));
                film.setRating(rs.getString("rating"));
                film.setSpecialFeatures(rs.getString("special_features"));
                film.setLastUpdate(rs.getTimestamp("last_update").toLocalDateTime());

                int languageId = rs.getInt("language_id");
                if (languageId > 0) {
                    Language lang = new Language();
                    lang.setLanguageId(languageId);
                    film.setLanguage(lang);
                }

                Integer originalLanguageId = rs.getObject("original_language_id", Integer.class);
                if (originalLanguageId != null && originalLanguageId > 0) {
                    Language origLang = new Language();
                    origLang.setLanguageId(originalLanguageId);
                    film.setOriginalLanguage(origLang);
                }

                films.add(film);
            }
        }
        
        return films;
    }
    
    // Find films by title
    public List<Film> findByTitle(Connection connection, String title) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_TITLE_SQL)) {
            statement.setString(1, "%" + title + "%");
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Film film = new Film();
                    film.setFilmId(rs.getInt("film_id"));
                    film.setTitle(rs.getString("title"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseYear(rs.getInt("release_year"));
                    film.setRentalDuration(rs.getInt("rental_duration"));
                    film.setRentalRate(rs.getBigDecimal("rental_rate"));
                    film.setLength(rs.getInt("length"));
                    film.setReplacementCost(rs.getBigDecimal("replacement_cost"));
                    film.setRating(rs.getString("rating"));
                    film.setSpecialFeatures(rs.getString("special_features"));
                    film.setLastUpdate(rs.getTimestamp("last_update").toLocalDateTime());

                    int languageId = rs.getInt("language_id");
                    if (languageId > 0) {
                        Language lang = new Language();
                        lang.setLanguageId(languageId);
                        film.setLanguage(lang);
                    }

                    Integer originalLanguageId = rs.getObject("original_language_id", Integer.class);
                    if (originalLanguageId != null && originalLanguageId > 0) {
                        Language origLang = new Language();
                        origLang.setLanguageId(originalLanguageId);
                        film.setOriginalLanguage(origLang);
                    }

                    films.add(film);
                }
            }
        }
        
        return films;
    }
    
    // Find films by language
    public List<Film> findByLanguageId(Connection connection, int languageId) throws SQLException {
        List<Film> films = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_LANGUAGE_ID_SQL)) {
            statement.setInt(1, languageId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Film film = new Film();
                    film.setFilmId(rs.getInt("film_id"));
                    film.setTitle(rs.getString("title"));
                    film.setDescription(rs.getString("description"));
                    film.setReleaseYear(rs.getInt("release_year"));
                    film.setRentalDuration(rs.getInt("rental_duration"));
                    film.setRentalRate(rs.getBigDecimal("rental_rate"));
                    film.setLength(rs.getInt("length"));
                    film.setReplacementCost(rs.getBigDecimal("replacement_cost"));
                    film.setRating(rs.getString("rating"));
                    film.setSpecialFeatures(rs.getString("special_features"));
                    film.setLastUpdate(rs.getTimestamp("last_update").toLocalDateTime());

                    int langId = rs.getInt("language_id");
                    if (langId > 0) {
                        Language lang = new Language();
                        lang.setLanguageId(langId);
                        film.setLanguage(lang);
                    }

                    Integer originalLanguageId = rs.getObject("original_language_id", Integer.class);
                    if (originalLanguageId != null && originalLanguageId > 0) {
                        Language origLang = new Language();
                        origLang.setLanguageId(originalLanguageId);
                        film.setOriginalLanguage(origLang);
                    }

                    films.add(film);
                }
            }
        }
        
        return films;
    }
    
    // Update film
    public void update(Connection connection, Film film) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, film.getTitle());
            statement.setString(2, film.getDescription());
            statement.setInt(3, film.getReleaseYear());
            
            int languageId = (film.getLanguage() != null) ? film.getLanguage().getLanguageId() : 0;
            statement.setInt(4, languageId);
            
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
    
    // Delete film
    public void deleteById(Connection connection, int filmId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, filmId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting film failed, no rows affected.");
            }
        }
    }
}
