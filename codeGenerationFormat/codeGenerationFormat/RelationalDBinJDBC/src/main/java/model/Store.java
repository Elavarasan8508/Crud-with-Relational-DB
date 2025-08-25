package model;

import java.util.List;
import java.time.LocalDateTime;

public class Store {

    private int storeId;
    private Address address;
    private Staff managerStaff;
    private LocalDateTime lastUpdate;
    private Integer managerStaffId;
    private Integer addressId;

    private List<Customer> customerList;
    private List<Inventory> inventoryList;
    private List<Staff> staffList;

    // Constructors
    public Store() {}

    // Getters and Setters
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Staff getManagerStaff() {
        return managerStaff;
    }

    public void setManagerStaff(Staff managerStaff) {
        this.managerStaff = managerStaff;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public List<Staff> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<Staff> staffList) {
        this.staffList = staffList;
    }


    public Integer getManagerStaffId() {
		return managerStaffId;
	}

	public void setManagerStaffId(Integer managerStaffId) {
		this.managerStaffId = managerStaffId;
	}


	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}


	// Builder Pattern
    public static class Builder {

        private Store instance = new Store();

        public Builder storeId(int storeId) {
            instance.setStoreId(storeId);
            return this;
        }

        public Builder address(Address address) {
            instance.setAddress(address);
            return this;
        }

        public Builder managerStaff(Staff managerStaff) {
            instance.setManagerStaff(managerStaff);
            return this;
        }

        public Builder lastUpdate(LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder customerList(List<Customer> customerList) {
            instance.setCustomerList(customerList);
            return this;
        }

        public Builder inventoryList(List<Inventory> inventoryList) {
            instance.setInventoryList(inventoryList);
            return this;
        }

        public Builder staffList(List<Staff> staffList) {
            instance.setStaffList(staffList);
            return this;
        }

        public Store build() {
            return instance;
        }
    }
}
