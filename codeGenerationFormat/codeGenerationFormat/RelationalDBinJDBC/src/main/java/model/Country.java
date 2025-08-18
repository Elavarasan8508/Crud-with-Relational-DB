package model;

import java.util.List;
import java.time.LocalDateTime;

public class Country {

	private int countryId;
    private String country;
    private LocalDateTime lastUpdate;
    private List<City> cityList;

public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<City> getCityList() {
		return cityList;
	}

	public void setCityList(List<City> cityList) {
		this.cityList = cityList;
	}

	public static class Builder {

        private Country instance = new Country();;

        public Builder countryId(int countryID) {
            instance.setCountryId(countryID);
            return this;
        }

        public Builder country(String country) {
            instance.setCountry(country);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder cityList(List<City> cityList) {
            instance.setCityList(cityList);
            return this;
        }

        public Country build() {
            return instance;
        }
    }
}
