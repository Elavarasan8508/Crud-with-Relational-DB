package model;

import java.util.List;
import java.time.LocalDateTime;

public class Actor {
	
	
    private int actorId;
    private FilmActor actor;
    private Film film;
    private String firstName;
    private String lastName;
    private LocalDateTime lastUpdate;
    private List<FilmActor> filmActorList;

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

	public FilmActor getActor() {
		return actor;
	}

	public void setActor(FilmActor actor) {
		this.actor = actor;
	}

	public Film getFilm() {
		return film;
	}

	public void setFilm(Film film) {
		this.film = film;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<FilmActor> getFilmActorList() {
		return filmActorList;
	}

	public void setFilmActorList(List<FilmActor> filmActorList) {
		this.filmActorList = filmActorList;
	}

	public static class Builder {

        private Actor instance = new Actor();

        public Builder actor(FilmActor actor) {
            instance.setActor(actor);
            return this;
        }

        public Builder film(Film film) {
            instance.setFilm(film);
            return this;
        }

        public Builder firstName(String firstName) {
            instance.setFirstName(firstName);
            return this;
        }

        public Builder lastName(String lastName) {
            instance.setLastName(lastName);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder filmActorList(List<FilmActor> filmActorList) {
            instance.setFilmActorList(filmActorList);
            return this;
        }

        public Actor build() {
            return instance;
        }
    }
}
