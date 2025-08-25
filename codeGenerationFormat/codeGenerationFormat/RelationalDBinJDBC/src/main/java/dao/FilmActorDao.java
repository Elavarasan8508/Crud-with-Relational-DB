package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class FilmActorDao {

    private static final String INSERT_SQL = "INSERT INTO film_actor (actor_id, film_id, last_update) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM film_actor WHERE actor_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM film_actor ORDER BY actor_id";

    private static final String SELECT_BY_ACTOR_ID_SQL = "SELECT * FROM film_actor WHERE actor_id = ?";

    private static final String SELECT_BY_FILM_ID_SQL = "SELECT * FROM film_actor WHERE film_id = ?";

    private static final String UPDATE_SQL = "UPDATE film_actor SET film_id = ?, last_update = ? WHERE actor_id = ?";

    private static final String DELETE_SQL = "DELETE FROM film_actor WHERE actor_id = ?";

    public int insert(Connection conn, FilmActor filmactor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            if (filmactor.getActor() != null && filmactor.getActor().getActorId() > 0) {
                ps.setInt(1, filmactor.getActor().getActorId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (filmactor.getFilm() != null && filmactor.getFilm().getFilmId() > 0) {
                ps.setInt(2, filmactor.getFilm().getFilmId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(filmactor.getLastUpdate() != null ? filmactor.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.executeUpdate();
        }
        return filmactor.getActor().getActorId();
    }

    public FilmActor findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public List<FilmActor> findAll(Connection conn) throws SQLException {
        List<FilmActor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    public boolean update(Connection conn, FilmActor filmactor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            if (filmactor.getFilm() != null && filmactor.getFilm().getFilmId() > 0) {
                ps.setInt(1, filmactor.getFilm().getFilmId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setTimestamp(2, Timestamp.valueOf(filmactor.getLastUpdate() != null ? filmactor.getLastUpdate() : java.time.LocalDateTime.now()));
            ps.setInt(3, filmactor.getActorId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<FilmActor> findByActorId(Connection conn, int actorID) throws SQLException {
        List<FilmActor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ACTOR_ID_SQL)) {
            ps.setInt(1, actorID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<FilmActor> findByFilmId(Connection conn, int filmID) throws SQLException {
        List<FilmActor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_FILM_ID_SQL)) {
            ps.setInt(1, filmID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    private FilmActor extract(ResultSet rs) throws SQLException {
        FilmActor filmactor = new FilmActor();
        Integer actor_id = rs.getObject("actor_id", Integer.class);
        filmactor.setActorId(actor_id);
        if (actor_id != null && actor_id > 0) {
            Actor actor = new Actor();
            actor.setActorId(actor_id);
            filmactor.setActor(actor);
        }
        Integer film_id = rs.getObject("film_id", Integer.class);
        filmactor.setFilmId(film_id);
        if (film_id != null && film_id > 0) {
            Film film = new Film();
            film.setFilmId(film_id);
            filmactor.setFilm(film);
        }
        Timestamp last_update = rs.getTimestamp("last_update");
        if (last_update != null)
            filmactor.setLastUpdate(last_update.toLocalDateTime());
        return filmactor;
    }
}
