package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

	private int paymentId;
    private Customer customer;

    private Rental rental;

    private Staff staff;
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    private LocalDateTime lastUpdate;

    public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Rental getRental() {
		return rental;
	}

	public void setRental(Rental rental) {
		this.rental = rental;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public java.time.LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(java.time.LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public java.time.LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public static class Builder {

        private Payment instance = new Payment();;

        public Builder customer(Customer customer) {
            instance.setCustomer(customer);
            return this;
        }

        public Builder rental(Rental rental) {
            instance.setRental(rental);
            return this;
        }

        public Builder staff(Staff staff) {
            instance.setStaff(staff);
            return this;
        }

        public Builder paymentId(int paymentId) {
            instance.setPaymentId(paymentId);
            return this;
        }

        public Builder amount(BigDecimal amount) {
            instance.setAmount(amount);
            return this;
        }

        public Builder paymentDate(java.time.LocalDateTime paymentDate) {
            instance.setPaymentDate(paymentDate);
            return this;
        }

        public Builder lastUpdate(java.time.LocalDateTime lastUpdate) {
            instance.setLastUpdate(lastUpdate);
            return this;
        }

        public Payment build() {
            return instance;
        }
    }
}
