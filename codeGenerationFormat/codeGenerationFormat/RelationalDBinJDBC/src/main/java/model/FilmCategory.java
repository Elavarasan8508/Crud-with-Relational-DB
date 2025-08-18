package model;

import java.time.LocalDateTime;

public class FilmCategory {

	private int categoryId;
    private Category category;

    private Film film;

    private LocalDateTime lastUpdate;

    
    public int getCategoryId() {
		return categoryId;
	}


	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}


	public Category getCategory() {
		return category;
	}


	public void setCategory(Category category) {
		this.category = category;
	}


	public Film getFilm() {
		return film;
	}


	public void setFilm(Film film) {
		this.film = film;
	}


	public java.time.LocalDateTime getLastUpdate() {
		return lastUpdate;
	}


	public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public static class Builder {

        private FilmCategory instance = new FilmCategory();;

        public Builder category(Category category) {
            instance.setCategory(category);
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

        public FilmCategory build() {
            return instance;
        }
    }
}
