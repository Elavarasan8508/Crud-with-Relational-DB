package model;

import java.util.List;
import java.time.LocalDateTime;

public class City {

	private int cityId;
    private Country country;
    private String city;

    private LocalDateTime lastUpdate;

    private List<Address> addressList;

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public List<Address> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<Address> addressList) {
		this.addressList = addressList;
	}

	public static class Builder {

        private City instance = new City();;

        public Builder country(Country country) {
            instance.setCountry(country);
            return this;
        }

        public Builder cityId(int cityId) {
            instance.setCityId(cityId);
            return this;
        }

        public Builder city(String city) {
            instance.setCity(city);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder addressList(List<Address> addressList) {
            instance.setAddressList(addressList);
            return this;
        }

        public City build() {
            return instance;
        }
    }
}
