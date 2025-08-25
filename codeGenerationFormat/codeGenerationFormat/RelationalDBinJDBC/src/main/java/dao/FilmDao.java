package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class FilmDao {

    private static final String INSERT_SQL = "INSERT INTO film (title, description, release_year, language_id, original_language_id, rental_duration, rental_rate, length, replacement_cost, rating, special_features, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM film WHERE film_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM film ORDER BY film_id";

    private static final String SELECT_BY_LANGUAGE_ID_SQL = "SELECT * FROM film WHERE language_id = ?";

    private static final String SELECT_BY_ORIGINAL_LANGUAGE_ID_SQL = "SELECT * FROM film WHERE original_language_id = ?";

    private static final String UPDATE_SQL = "UPDATE film SET title = ?, description = ?, release_year = ?, language_id = ?, original_language_id = ?, rental_duration = ?, rental_rate = ?, length = ?, replacement_cost = ?, rating = ?, special_features = ?, last_update = ? WHERE film_id = ?";

    private static final String DELETE_SQL = "DELETE FROM film WHERE film_id = ?";

    public int insert(Connection conn, Film film) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, film.getTitle());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getReleaseYear());
            if (film.getLanguage() != null && film.getLanguage().getLanguageId() > 0) {
                ps.setInt(4, film.getLanguage().getLanguageId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (film.getOriginalLanguage() != null && film.getOriginalLanguage().getLanguageId() > 0) {
                ps.setInt(5, film.getOriginalLanguage().getLanguageId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, film.getRentalDuration());
            BigDecimal val7 = film.getRentalRate();
            if (val7 != null) {
                ps.setBigDecimal(7, val7);
            } else {
                ps.setNull(7, Types.FLOAT);
            }
            ps.setInt(8, film.getLength());
            BigDecimal val9 = film.getReplacementCost();
            if (val9 != null) {
                ps.setBigDecimal(9, val9);
            } else {
                ps.setNull(9, Types.FLOAT);
            }
            ps.setString(10, film.getRating());
            ps.setString(11, film.getSpecialFeatures());
            ps.setTimestamp(12, Timestamp.valueOf(film.getLastUpdate() != null ? film.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    film.setFilmId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Film findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<Film> findAll(Connection conn) throws SQLException {
        List<Film> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, Film film) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, film.getTitle());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getReleaseYear());
            if (film.getLanguage() != null && film.getLanguage().getLanguageId() > 0) {
                ps.setInt(4, film.getLanguage().getLanguageId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (film.getOriginalLanguage() != null && film.getOriginalLanguage().getLanguageId() > 0) {
                ps.setInt(5, film.getOriginalLanguage().getLanguageId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, film.getRentalDuration());
            BigDecimal val7 = film.getRentalRate();
            if (val7 != null) {
                ps.setBigDecimal(7, val7);
            } else {
                ps.setNull(7, Types.FLOAT);
            }
            ps.setInt(8, film.getLength());
            BigDecimal val9 = film.getReplacementCost();
            if (val9 != null) {
                ps.setBigDecimal(9, val9);
            } else {
                ps.setNull(9, Types.FLOAT);
            }
            ps.setString(10, film.getRating());
            ps.setString(11, film.getSpecialFeatures());
            ps.setTimestamp(12, Timestamp.valueOf(film.getLastUpdate() != null ? film.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(13, film.getFilmId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Film> findByLanguageId(Connection conn, int languageID) throws SQLException {
        List<Film> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_LANGUAGE_ID_SQL)) {
            ps.setInt(1, languageID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<Film> findByOriginalLanguageId(Connection conn, int originalLanguageID) throws SQLException {
        List<Film> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ORIGINAL_LANGUAGE_ID_SQL)) {
            ps.setInt(1, originalLanguageID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private Film extract(ResultSet rs) throws SQLException {
        Film film = new Film();
        Integer film_id = rs.getObject("film_id", Integer.class);
        film.setFilmId(film_id);
        film.setTitle(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseYear(rs.getInt("release_year"));
        Integer language_id = rs.getObject("language_id", Integer.class);
        film.setLanguageId(language_id);
        if (language_id != null && language_id > 0) {
            Language language = new Language();
            language.setLanguageId(language_id);
            film.setLanguage(language);
        }
        Integer original_language_id = rs.getObject("original_language_id", Integer.class);
        film.setOriginalLanguageId(original_language_id);
        if (original_language_id != null && original_language_id > 0) {
            Language originalLanguage = new Language();
            originalLanguage.setLanguageId(original_language_id);
            film.setOriginalLanguage(originalLanguage);
        }
        film.setRentalDuration(rs.getInt("rental_duration"));
        BigDecimal rental_rate = rs.getObject("rental_rate", BigDecimal.class);
        film.setRentalRate(rental_rate);
        film.setLength(rs.getInt("length"));
        BigDecimal replacement_cost = rs.getObject("replacement_cost", BigDecimal.class);
        film.setReplacementCost(replacement_cost);
        film.setRating(rs.getString("rating"));
        film.setSpecialFeatures(rs.getString("special_features"));
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            film.setLastUpdate(last_update.toLocalDateTime());
        return film;
    }
}
