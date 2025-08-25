package model;

import java.util.List;
import java.time.LocalDateTime;

public class Staff {

	private int staffId;
    private Address address;
    private Store store;
    private Store managerStaff;
    private String firstName;
    private Integer storeId;
    private Integer addressId;
    private String lastName;
    private String email;
    private Boolean active;
    private String username;
    private String password;
    private LocalDateTime lastUpdate;
    private String picture;
    private List<Payment> paymentList;
    private List<Rental> rentalList;
    private List<Store> storeList;

    
    public int getStaffId() {
		return staffId;
	}

	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public Store getManagerStaff() {
		return managerStaff;
	}

	public void setManagerStaff(Store managerStaff) {
		this.managerStaff = managerStaff;
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
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(boolean b) {
		this.active = b;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public List<Payment> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<Payment> paymentList) {
		this.paymentList = paymentList;
	}

	public List<Rental> getRentalList() {
		return rentalList;
	}

	public void setRentalList(List<Rental> rentalList) {
		this.rentalList = rentalList;
	}
	public List<Store> getStoreList() {
		return storeList;
	}
	public void setStoreList(List<Store> storeList) {
		this.storeList = storeList;
	}
	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}
	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}
	public static class Builder {

        private Staff instance = new Staff();;

        public Builder address(Address address) {
            instance.setAddress(address);
            return this;
        }

        public Builder store(Store store) {
            instance.setStore(store);
            return this;
        }

        public Builder managerStaff(Store managerStaff) {
            instance.setManagerStaff(managerStaff);
            return this;
        }

        public Builder staffId(int staffId) {
            instance.setStaffId(staffId);
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

        public Builder email(String email) {
            instance.setEmail(email);
            return this;
        }

        public Builder active(boolean active) {
            instance.setActive(active);
            return this;
        }

        public Builder username(String username) {
            instance.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            instance.setPassword(password);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder picture(String picture) {
            instance.setPicture(picture);
            return this;
        }

        public Builder paymentList(List<Payment> paymentList) {
            instance.setPaymentList(paymentList);
            return this;
        }

        public Builder rentalList(List<Rental> rentalList) {
            instance.setRentalList(rentalList);
            return this;
        }

        public Builder storeList(List<Store> storeList) {
            instance.setStoreList(storeList);
            return this;
        }

        public Staff build() {
            return instance;
        }
    }
}
