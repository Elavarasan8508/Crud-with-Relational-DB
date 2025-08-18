package model;

import java.util.List;
import java.time.LocalDateTime;

public class Inventory {

	private int inventoryId;
    private Film film;

    private Store store;

    private Rental inventory;

    private LocalDateTime lastUpdate;

    private List<Rental> rentalList;

    public int getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(int inventoryId) {
		this.inventoryId = inventoryId;
	}

	public Film getFilm() {
		return film;
	}

	public void setFilm(Film film) {
		this.film = film;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public Rental getInventory() {
		return inventory;
	}

	public void setInventory(Rental inventory) {
		this.inventory = inventory;
	}

	public java.time.LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<Rental> getRentalList() {
		return rentalList;
	}

	public void setRentalList(List<Rental> rentalList) {
		this.rentalList = rentalList;
	}

	public static class Builder {

        private Inventory instance = new Inventory();;

        public Builder film(Film film) {
            instance.setFilm(film);
            return this;
        }

        public Builder store(Store store) {
            instance.setStore(store);
            return this;
        }

        public Builder inventory(Rental inventory) {
            instance.setInventory(inventory);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder rentalList(List<Rental> rentalList) {
            instance.setRentalList(rentalList);
            return this;
        }

        public Inventory build() {
            return instance;
        }
    }
}
