package model;

import java.util.List;
import java.time.LocalDateTime;

public class Category {
    
	private int categoryId;
    private FilmCategory category;

    private Film film;

    private String name;

    private LocalDateTime lastUpdate;

    private List<FilmCategory> filmCategoryList;



    public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public FilmCategory getCategory() {
		return category;
	}

	public void setCategory(FilmCategory category) {
		this.category = category;
	}

	public Film getFilm() {
		return film;
	}

	public void setFilm(Film film) {
		this.film = film;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<FilmCategory> getFilmCategoryList() {
		return filmCategoryList;
	}

	public void setFilmCategoryList(List<FilmCategory> filmCategoryList) {
		this.filmCategoryList = filmCategoryList;
	}

	public static class Builder {

        private Category instance = new Category();;

        public Builder category(FilmCategory category) {
            instance.setCategory(category);
            return this;
        }

        public Builder film(Film film) {
            instance.setFilm(film);
            return this;
        }

        public Builder name(String name) {
            instance.setName(name);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder filmCategoryList(List<FilmCategory> filmCategoryList) {
            instance.setFilmCategoryList(filmCategoryList);
            return this;
        }

        public Category build() {
            return instance;
        }
    }
}
