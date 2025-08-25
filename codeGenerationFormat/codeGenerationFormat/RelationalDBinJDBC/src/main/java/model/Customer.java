package model;

import java.util.List;
import java.time.LocalDateTime;

//checked
public class Customer {
    private int customerId;
    private Address address;
    private Store store;
    private Integer storeId;
    private Integer addressId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean active; 
    private LocalDateTime createDate;
    private LocalDateTime lastUpdate;
    
    // Relationship lists
    private List<Payment> paymentList;
    private List<Rental> rentalList;

    // Constructors
    public Customer() {}

    public Customer(int customerId) {
        this.customerId = customerId;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerID) {
        this.customerId = customerID;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
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


    public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}


	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}


	// Builder Pattern
    public static class Builder {
        private Customer instance = new Customer();

        public Builder customerId(int customerId) {
            instance.setCustomerId(customerId);
            return this;
        }

        public Builder address(Address address) {
            instance.setAddress(address);
            return this;
        }

        public Builder store(Store store) {
            instance.setStore(store);
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

        public Builder createDate(LocalDateTime createDate) {
            instance.setCreateDate(createDate);
            return this;
        }

        public Builder lastUpdate(LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
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

        public Customer build() {
            return instance;
        }
    }
}
