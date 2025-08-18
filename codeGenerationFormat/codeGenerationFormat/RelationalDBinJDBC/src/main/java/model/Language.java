package model;

import java.util.List;
import java.time.LocalDateTime;

public class Language {

    private int languageId;

    private String name;

    private LocalDateTime lastUpdate;

    private List<Film> filmList;

  
    public int getLanguageId() {
		return languageId;
	}
	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public java.time.LocalDateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public List<Film> getFilmList() {
		return filmList;
	}
	public void setFilmList(List<Film> filmList) {
		this.filmList = filmList;
	}



	public static class Builder {

        private Language instance = new Language();;

        public Builder languageId(int languageId) {
            instance.setLanguageId(languageId);
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

        public Builder filmList(List<Film> filmList) {
            instance.setFilmList(filmList);
            return this;
        }

        public Language build() {
            return instance;
        }
    }
}
