package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class FilmCategoryDao {

    private static final String INSERT_SQL = "INSERT INTO film_category (film_id, category_id, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM film_category WHERE film_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM film_category ORDER BY film_id";

    private static final String SELECT_BY_CATEGORY_ID_SQL = "SELECT * FROM film_category WHERE category_id = ?";

    private static final String SELECT_BY_FILM_ID_SQL = "SELECT * FROM film_category WHERE film_id = ?";

    private static final String UPDATE_SQL = "UPDATE film_category SET category_id = ?, last_update = ? WHERE film_id = ?";

    private static final String DELETE_SQL = "DELETE FROM film_category WHERE film_id = ?";

    public int insert(Connection conn, FilmCategory filmcategory) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            if (filmcategory.getFilm() != null && filmcategory.getFilm().getFilmId() > 0) {
                ps.setInt(1, filmcategory.getFilm().getFilmId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (filmcategory.getCategory() != null && filmcategory.getCategory().getCategoryId() > 0) {
                ps.setInt(2, filmcategory.getCategory().getCategoryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(filmcategory.getLastUpdate() != null ? filmcategory.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
        }
        return filmcategory.getFilm().getFilmId();
    }

    public FilmCategory findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<FilmCategory> findAll(Connection conn) throws SQLException {
        List<FilmCategory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, FilmCategory filmcategory) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (filmcategory.getCategory() != null && filmcategory.getCategory().getCategoryId() > 0) {
                ps.setInt(1, filmcategory.getCategory().getCategoryId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setTimestamp(2, Timestamp.valueOf(filmcategory.getLastUpdate() != null ? filmcategory.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(3, filmcategory.getFilmId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<FilmCategory> findByCategoryId(Connection conn, int categoryID) throws SQLException {
        List<FilmCategory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CATEGORY_ID_SQL)) {
            ps.setInt(1, categoryID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<FilmCategory> findByFilmId(Connection conn, int filmID) throws SQLException {
        List<FilmCategory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_FILM_ID_SQL)) {
            ps.setInt(1, filmID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private FilmCategory extract(ResultSet rs) throws SQLException {
        FilmCategory filmcategory = new FilmCategory();
        Integer film_id = rs.getObject("film_id", Integer.class);
        filmcategory.setFilmId(film_id);
        if (film_id != null && film_id > 0) {
            Film film = new Film();
            film.setFilmId(film_id);
            filmcategory.setFilm(film);
        }
        Integer category_id = rs.getObject("category_id", Integer.class);
        filmcategory.setCategoryId(category_id);
        if (category_id != null && category_id > 0) {
            Category category = new Category();
            category.setCategoryId(category_id);
            filmcategory.setCategory(category);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            filmcategory.setLastUpdate(last_update.toLocalDateTime());
        return filmcategory;
    }
}
