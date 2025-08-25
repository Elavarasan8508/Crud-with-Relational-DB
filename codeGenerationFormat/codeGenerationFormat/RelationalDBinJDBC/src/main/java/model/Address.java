package model;

import java.util.List;
import java.time.LocalDateTime;

public class Address {
    
	private int addressId;
	
    private City city;

    private String address;

    private Staff managerStaff;

    private String address2;

    private String district;

    private String postalCode;
    
    private Integer cityId;
    
    private String phone;

    private LocalDateTime lastUpdate;

    private List<Customer> customerList;

    private List<Staff> staffList;

    public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	private List<Store> storeList;

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Staff getManagerStaff() {
        return managerStaff;
    }

    public void setManagerStaff(Staff managerStaff) {
        this.managerStaff = managerStaff;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public java.time.LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public List<Staff> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<Staff> staffList) {
        this.staffList = staffList;
    }

    public List<Store> getStoreList() {
        return storeList;
    }

    public void setStoreList(List<Store> storeList) {
        this.storeList = storeList;
    }

    public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public static class Builder {

        private Address instance = new Address();;

        public Builder city(City city) {
            instance.setCity(city);
            return this;
        }

        public Builder address(String address) {
            instance.setAddress(address);
            return this;
        }

        public Builder managerStaff(Staff managerStaff) {
            instance.setManagerStaff(managerStaff);
            return this;
        }

        public Builder address2(String address2) {
            instance.setAddress2(address2);
            return this;
        }

        public Builder district(String district) {
            instance.setDistrict(district);
            return this;
        }

        public Builder postalCode(String postalCode) {
            instance.setPostalCode(postalCode);
            return this;
        }

        public Builder phone(String phone) {
            instance.setPhone(phone);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder customerList(List<Customer> customerList) {
            instance.setCustomerList(customerList);
            return this;
        }

        public Builder staffList(List<Staff> staffList) {
            instance.setStaffList(staffList);
            return this;
        }

        public Builder storeList(List<Store> storeList) {
            instance.setStoreList(storeList);
            return this;
        }

        public Address build() {
            return instance;
        }
    }
}
