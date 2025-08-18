package model;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

//Checked
public class Film {
    private int filmId;
    private Language language;
    private Language originalLanguage;
    private Category category;
    private String title;
    private String description;
    private Integer releaseYear; 
    private Integer rentalDuration; 
    private BigDecimal rentalRate; 
    private Integer length; 
    private BigDecimal replacementCost;
    private String rating;
    private String specialFeatures;
    private LocalDateTime lastUpdate;
    
    // Relationship lists
    private List<FilmActor> filmActorList;
    private List<FilmCategory> filmCategoryList;
    private List<Inventory> inventoryList;

    // Constructors
    public Film() {}

    public Film(int filmId) {
        this.filmId = filmId;
    }

    // Getters and Setters
    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(Language originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Integer getRentalDuration() {
        return rentalDuration;
    }

    public void setRentalDuration(Integer rentalDuration) {
        this.rentalDuration = rentalDuration;
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        this.rentalRate = rentalRate;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public BigDecimal getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSpecialFeatures() {
        return specialFeatures;
    }

    public void setSpecialFeatures(String specialFeatures) {
        this.specialFeatures = specialFeatures;
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

    public List<FilmCategory> getFilmCategoryList() {
        return filmCategoryList;
    }

    public void setFilmCategoryList(List<FilmCategory> filmCategoryList) {
        this.filmCategoryList = filmCategoryList;
    }

    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    // Builder Pattern
    public static class Builder {
        private Film instance = new Film();

        public Builder filmId(int filmId) {
            instance.setFilmId(filmId);
            return this;
        }

        public Builder language(Language language) {
            instance.setLanguage(language);
            return this;
        }

        public Builder originalLanguage(Language originalLanguage) {
            instance.setOriginalLanguage(originalLanguage);
            return this;
        }

        public Builder category(Category category) {
            instance.setCategory(category);
            return this;
        }

        public Builder title(String title) {
            instance.setTitle(title);
            return this;
        }

        public Builder description(String description) {
            instance.setDescription(description);
            return this;
        }

        public Builder releaseYear(Integer releaseYear) {
            instance.setReleaseYear(releaseYear);
            return this;
        }

        public Builder rentalDuration(Integer rentalDuration) {
            instance.setRentalDuration(rentalDuration);
            return this;
        }

        public Builder rentalRate(BigDecimal rentalRate) {
            instance.setRentalRate(rentalRate);
            return this;
        }

        public Builder length(Integer length) {
            instance.setLength(length);
            return this;
        }

        public Builder replacementCost(BigDecimal replacementCost) {
            instance.setReplacementCost(replacementCost);
            return this;
        }

        public Builder rating(String rating) {
            instance.setRating(rating);
            return this;
        }

        public Builder specialFeatures(String specialFeatures) {
            instance.setSpecialFeatures(specialFeatures);
            return this;
        }

        public Builder lastUpdate(LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder filmActorList(List<FilmActor> filmActorList) {
            instance.setFilmActorList(filmActorList);
            return this;
        }

        public Builder filmCategoryList(List<FilmCategory> filmCategoryList) {
            instance.setFilmCategoryList(filmCategoryList);
            return this;
        }

        public Builder inventoryList(List<Inventory> inventoryList) {
            instance.setInventoryList(inventoryList);
            return this;
        }

        public Film build() {
            return instance;
        }
    }
}
