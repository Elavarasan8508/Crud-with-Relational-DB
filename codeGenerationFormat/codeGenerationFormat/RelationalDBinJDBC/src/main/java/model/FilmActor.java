package model;

import java.time.LocalDateTime;

public class FilmActor {

	private int filmActorId;
    private Actor actor;

    private Film film;

    private LocalDateTime lastUpdate;

 
    public int getFilmActorId() {
		return filmActorId;
	}
	public void setFilmActorId(int filmActorId) {
		this.filmActorId = filmActorId;
	}
	public Actor getActor() {
		return actor;
	}
	public void setActor(Actor actor) {
		this.actor = actor;
	}
	public Film getFilm() {
		return film;
	}
	public void setFilm(Film film) {
		this.film = film;
	}
	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public static class Builder {

        private FilmActor instance = new FilmActor();;

        public Builder actor(Actor actor) {
            instance.setActor(actor);
            return this;
        }

        public Builder film(Film film) {
            instance.setFilm(film);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public FilmActor build() {
            return instance;
        }
    }
}
