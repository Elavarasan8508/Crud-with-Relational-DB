package model;

import java.util.List;
import java.time.LocalDateTime;

public class Rental {

	private int rentalId;
    private Customer customer;

    private Inventory inventory;

    private Staff staff;

    private LocalDateTime rentalDate;

    private LocalDateTime returnDate;

    private LocalDateTime lastUpdate;

    private List<Payment> paymentList;

    public int getRentalId() {
		return rentalId;
	}

	public void setRentalId(int rentalId) {
		this.rentalId = rentalId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public LocalDateTime getRentalDate() {
		return rentalDate;
	}

	public void setRentalDate(LocalDateTime rentalDate) {
		this.rentalDate = rentalDate;
	}

	public LocalDateTime getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(LocalDateTime returnDate) {
		this.returnDate = returnDate;
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

	public static class Builder {

        private Rental instance = new Rental();;

        public Builder customer(Customer customer) {
            instance.setCustomer(customer);
            return this;
        }

        public Builder inventory(Inventory inventory) {
            instance.setInventory(inventory);
            return this;
        }

        public Builder staff(Staff staff) {
            instance.setStaff(staff);
            return this;
        }

        public Builder rentalDate(java.time.LocalDateTime rentalDate) {
            instance.setRentalDate(rentalDate);
            return this;
        }

        public Builder returnDate(java.time.LocalDateTime returnDate) {
            instance.setReturnDate(returnDate);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Builder paymentList(List<Payment> paymentList) {
            instance.setPaymentList(paymentList);
            return this;
        }

        public Rental build() {
            return instance;
        }
    }
}
