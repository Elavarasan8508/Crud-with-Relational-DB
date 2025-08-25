package service;

import dao.*;
import model.*;
import DataBaseConnection.TransactionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class VideoRentalService {
    
    private final RentalDao rentalDao;
    private final CustomerDao customerDao;
    private final InventoryDao inventoryDao;
    private final FilmDao filmDao;
    private final StaffDao staffDao;
    private final PaymentDao paymentDao;
    
    public VideoRentalService() {
        this.rentalDao = new RentalDao();
        this.customerDao = new CustomerDao();
        this.inventoryDao = new InventoryDao();
        this.filmDao = new FilmDao();
        this.staffDao = new StaffDao();
        this.paymentDao = new PaymentDao();
    }
    
    
    // Business Logic: Create Rental - Returns Rental object
    public Rental createRental(Map<String, Object> requestData) throws SQLException {
        try {
            return TransactionManager.executeInTransaction(connection -> {
                System.out.println("🎬 Starting rental creation transaction...");
                
                try {
                    // Handle both nested and flat structure
                    Map<String, Object> rentalData;
                    Map<String, Object> paymentData = null;
                    
                    if (requestData.containsKey("rental") && requestData.containsKey("payment")) {
                        // Nested structure
                        rentalData = (Map<String, Object>) requestData.get("rental");
                        paymentData = (Map<String, Object>) requestData.get("payment");
                    } else {
                        // Flat structure
                        rentalData = requestData;
                    }
                    
                    // Create objects
                    Rental rental = mapToRental(rentalData);
                    LocalDateTime returnDate = parseLocalDateTime((String) rentalData.get("returnDate"));
                    
                    // Validate entities exist
                    Customer customer = customerDao.findById(connection, rental.getCustomer().getCustomerId());
                    if (customer == null || !customer.isActive()) {
                        throw new IllegalArgumentException("Customer not found or inactive");
                    }
                    
                    Inventory inventory = inventoryDao.findById(connection, rental.getInventory().getInventoryId());
                    if (inventory == null) {
                        throw new IllegalArgumentException("Inventory not found");
                    }
                    
                    Staff staff = staffDao.findById(connection, rental.getStaff().getStaffId());
                    if (staff == null || !staff.getActive()) {
                        throw new IllegalArgumentException("Staff not found or inactive");
                    }
                    
                    Film film = filmDao.findById(connection, inventory.getFilm().getFilmId());
                    if (film == null) {
                        throw new IllegalArgumentException("Film not found");
                    }
                    
                    
                    // Set complete objects and timestamps
                    rental.setCustomer(customer);
                    rental.setInventory(inventory);
                    rental.setStaff(staff);
                    rental.setReturnDate(returnDate);
                    rental.setLastUpdate(LocalDateTime.now());
                    
                    // Create rental
                    int rentalId = rentalDao.insert(connection, rental);
                    rental.setRentalId(rentalId);
                    
                    // Create payment
                    Payment payment;
                    if (paymentData != null) {
                        payment = mapToPayment(paymentData);
                    } else {
                        // Create default payment
                        payment = new Payment();
                        Double paymentAmountDouble = (Double) rentalData.get("paymentAmount");
                        BigDecimal amount = paymentAmountDouble != null ? 
                            BigDecimal.valueOf(paymentAmountDouble) : film.getRentalRate();
                        payment.setAmount(amount);
                        payment.setPaymentDate(rental.getRentalDate());
                    }
                    
                    payment.setCustomer(customer);
                    payment.setStaff(staff);
                    payment.setRental(rental);
                    payment.setLastUpdate(LocalDateTime.now());
                    int paymentId = paymentDao.insert(connection, payment);
                    payment.setPaymentId(paymentId);
                    
                    // Calculate and create late fee if applicable
                    if (returnDate != null) {
                        BigDecimal lateFee = calculateLateFee(rental.getRentalDate(), returnDate, film.getRentalDuration());
                        if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                            Payment lateFeePayment = new Payment();
                            lateFeePayment.setCustomer(customer);
                            lateFeePayment.setStaff(staff);
                            lateFeePayment.setRental(rental);
                            lateFeePayment.setAmount(lateFee);
                            lateFeePayment.setPaymentDate(returnDate);
                            lateFeePayment.setLastUpdate(LocalDateTime.now());
                            paymentDao.insert(connection, lateFeePayment);
                        }
                    }
                    
                    return buildRentalWithRelationships(connection, rentalId);
                    
                } catch (Exception e) {
                    throw new RuntimeException("Rental creation failed", e);
                }
            });
        } catch (Exception e) {
            throw new SQLException("Failed to create rental: " + e.getMessage(), e);
        }
    }
    
    // Business Logic: Return Film - Returns Rental object
    public Rental returnFilm(int rentalId, Map<String, Object> requestData) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            
            Rental rental = buildRentalWithRelationships(connection, rentalId);
            if (rental == null) {
                throw new IllegalArgumentException("Rental not found with ID: " + rentalId);
            }
            
            if (rental.getReturnDate() != null) {
                throw new IllegalStateException("Film already returned");
            }
            
            // Update rental with return date
            rental.setReturnDate(LocalDateTime.now());
            rental.setLastUpdate(LocalDateTime.now());
            rentalDao.update(connection, rental);
            
            // Calculate and create late fee if applicable
            Film film = rental.getInventory().getFilm();
            BigDecimal lateFee = calculateLateFee(rental.getRentalDate(), rental.getReturnDate(), film.getRentalDuration());
            
            if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                Payment lateFeePayment = new Payment();
                lateFeePayment.setCustomer(rental.getCustomer());
                lateFeePayment.setStaff(rental.getStaff());
                lateFeePayment.setRental(rental);
                lateFeePayment.setAmount(lateFee);
                lateFeePayment.setPaymentDate(rental.getReturnDate());
                lateFeePayment.setLastUpdate(LocalDateTime.now());
                paymentDao.insert(connection, lateFeePayment);
            }
            
            return buildRentalWithRelationships(connection, rentalId);
        });
    }
    
    // Business Logic: Delete Rental - Returns Rental object
    public Rental deleteRental(int rentalId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Rental rental = buildRentalWithRelationships(connection, rentalId);
            if (rental == null) {
                throw new IllegalArgumentException("Rental not found with ID: " + rentalId);
            }
            
            // Business rule: Check if film is returned before deletion
            if (rental.getReturnDate() == null) {
                throw new IllegalStateException("Cannot delete active rental - film must be returned first");
            }
            
            // Delete related payments first (if any)
            List<Payment> payments = paymentDao.findByRentalId(connection, rentalId);
            for (Payment payment : payments) {
                paymentDao.deleteById(connection, payment.getPaymentId());
            }
            
            // Delete the rental
            rentalDao.deleteById(connection, rentalId);
            
            return rental;
        });
    }
    
    // Business Logic: Get Rental by ID - Returns Rental object
    public Rental getRentalById(int rentalId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Rental rental = buildRentalWithRelationships(connection, rentalId);
            
            if (rental == null) {
                throw new IllegalArgumentException("Rental not found with ID: " + rentalId);
            }
            
            return rental;
        });
    }
    
    // Business Logic: Get All Rentals - Returns List<Rental>
    public List<Rental> getAllRentals() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Rental> allRentals = rentalDao.findAll(connection);
            List<Rental> allRentalsWithDetails = new ArrayList<>();
            
            for (Rental rental : allRentals) {
                try {
                    Rental rentalWithDetails = buildRentalWithRelationships(connection, rental.getRentalId());
                    if (rentalWithDetails != null) {
                        allRentalsWithDetails.add(rentalWithDetails);
                    }
                } catch (Exception e) {
                    rental.setPaymentList(new ArrayList<>());
                    allRentalsWithDetails.add(rental);
                }
            }
            
            return allRentalsWithDetails;
        });
    }
    
    // Business Logic: Get All Active Rentals - Returns List<Rental>
    public List<Rental> getAllActiveRentals() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Rental> allRentals = rentalDao.findAll(connection);
            List<Rental> activeRentals = allRentals.stream()
                .filter(r -> r.getReturnDate() == null)
                .collect(Collectors.toList());
            
            List<Rental> activeRentalsWithDetails = new ArrayList<>();
            for (Rental rental : activeRentals) {
                try {
                    Rental rentalWithDetails = buildRentalWithRelationships(connection, rental.getRentalId());
                    if (rentalWithDetails != null) {
                        activeRentalsWithDetails.add(rentalWithDetails);
                    }
                } catch (Exception e) {
                    rental.setPaymentList(new ArrayList<>());
                    activeRentalsWithDetails.add(rental);
                }
            }
            
            return activeRentalsWithDetails;
        });
    }
    
    // Business Logic: Get Customer Rentals - Returns List<Rental>
    public List<Rental> getCustomerRentals(int customerId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            
            Customer customer = customerDao.findById(connection, customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found with ID: " + customerId);
            }
            
            List<Rental> rentals = rentalDao.findByCustomerId(connection, customerId);
            List<Rental> rentalsWithDetails = new ArrayList<>();
            
            for (Rental rental : rentals) {
                try {
                    Rental rentalWithDetails = buildRentalWithRelationships(connection, rental.getRentalId());
                    if (rentalWithDetails != null) {
                        rentalsWithDetails.add(rentalWithDetails);
                    }
                } catch (Exception e) {
                    rental.setPaymentList(new ArrayList<>());
                    rentalsWithDetails.add(rental);
                }
            }
            
            return rentalsWithDetails;
        });
    }
    
    // Business Logic: Get Rentals by Store - Returns List<Rental>
    public List<Rental> getRentalsByStore(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            // Get all rentals where inventory belongs to the specified store
            List<Rental> allRentals = rentalDao.findAll(connection);
            List<Rental> storeRentals = allRentals.stream()
                .filter(rental -> {
                    try {
                        Inventory inventory = inventoryDao.findById(connection, rental.getInventory().getInventoryId());
                        return inventory != null && inventory.getStore().getStoreId() == storeId;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            List<Rental> storeRentalsWithDetails = new ArrayList<>();
            for (Rental rental : storeRentals) {
                try {
                    Rental rentalWithDetails = buildRentalWithRelationships(connection, rental.getRentalId());
                    if (rentalWithDetails != null) {
                        storeRentalsWithDetails.add(rentalWithDetails);
                    }
                } catch (Exception e) {
                    rental.setPaymentList(new ArrayList<>());
                    storeRentalsWithDetails.add(rental);
                }
            }
            
            return storeRentalsWithDetails;
        });
    }
    
    // Business Logic: Get Overdue Rentals - Returns List<Rental>
    public List<Rental> getOverdueRentals() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Rental> allRentals = rentalDao.findAll(connection);
            List<Rental> overdueRentals = new ArrayList<>();
            
            for (Rental rental : allRentals) {
                // Only check active rentals (not returned)
                if (rental.getReturnDate() == null) {
                    try {
                        // Get film info to check rental duration
                        Inventory inventory = inventoryDao.findById(connection, rental.getInventory().getInventoryId());
                        if (inventory != null) {
                            Film film = filmDao.findById(connection, inventory.getFilm().getFilmId());
                            if (film != null) {
                                LocalDateTime dueDate = rental.getRentalDate().plusDays(film.getRentalDuration());
                                if (LocalDateTime.now().isAfter(dueDate)) {
                                    overdueRentals.add(rental);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip this rental if we can't determine if it's overdue
                        continue;
                    }
                }
            }

            List<Rental> overdueRentalsWithDetails = new ArrayList<>();
            for (Rental rental : overdueRentals) {
                try {
                    Rental rentalWithDetails = buildRentalWithRelationships(connection, rental.getRentalId());
                    if (rentalWithDetails != null) {
                        overdueRentalsWithDetails.add(rentalWithDetails);
                    }
                } catch (Exception e) {
                    rental.setPaymentList(new ArrayList<>());
                    overdueRentalsWithDetails.add(rental);
                }
            }
            
            return overdueRentalsWithDetails;
        });
    }
    
    // Keep this method for API info endpoint
    public Map<String, Object> handleApiInformationQuery() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("service", "Video Rental API - Rental Management");
        apiInfo.put("status", "Active");
        apiInfo.put("message", "Rental service endpoints");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /rentals", "Get all rentals");
        endpoints.put("GET /rentals?info=true", "Show API information");
        endpoints.put("GET /rentals?customerId={id}", "Get rental history for customer");
        endpoints.put("GET /rentals?activeRentals=true", "Get all active rentals");
        endpoints.put("GET /rentals?rentalId={id}", "Get specific rental details");
        endpoints.put("GET /rentals?storeId={id}", "Get rentals by store");
        endpoints.put("GET /rentals?overdue=true", "Get overdue rentals");
        endpoints.put("POST /rentals", "Create a new rental");
        endpoints.put("PUT /rentals?rentalId={id}", "Return a rented film");
        endpoints.put("DELETE /rentals?rentalId={id}", "Delete a rental");
        apiInfo.put("endpoints", endpoints);
        
        return apiInfo;
    }
    
    // Business Logic: Build Rental with Relationships
    private Rental buildRentalWithRelationships(java.sql.Connection connection, int rentalId) throws SQLException {
        Rental rental = rentalDao.findById(connection, rentalId);
        if (rental == null) return null;
        
        try {
            // Load customer
            if (rental.getCustomer() != null && rental.getCustomer().getCustomerId() > 0) {
                Customer customer = customerDao.findById(connection, rental.getCustomer().getCustomerId());
                if (customer != null) {
                    rental.setCustomer(customer);
                }
            }
            
            // Load inventory and film
            if (rental.getInventory() != null && rental.getInventory().getInventoryId() > 0) {
                Inventory inventory = inventoryDao.findById(connection, rental.getInventory().getInventoryId());
                if (inventory != null) {
                    rental.setInventory(inventory);
                    
                    if (inventory.getFilm() != null && inventory.getFilm().getFilmId() > 0) {
                        Film film = filmDao.findById(connection, inventory.getFilm().getFilmId());
                        if (film != null) {
                            inventory.setFilm(film);
                        }
                    }
                }
            }
            
            // Load staff
            if (rental.getStaff() != null && rental.getStaff().getStaffId() > 0) {
                Staff staff = staffDao.findById(connection, rental.getStaff().getStaffId());
                if (staff != null) {
                    rental.setStaff(staff);
                }
            }
            
            // Load payments
            List<Payment> payments = paymentDao.findByRentalId(connection, rentalId);
            rental.setPaymentList(payments != null ? payments : new ArrayList<>());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error loading relationships for rental " + rentalId + ": " + e.getMessage());
            rental.setPaymentList(new ArrayList<>());
        }
        
        return rental;
    }
    
    // Helper mapping methods
    private Rental mapToRental(Map<String, Object> data) {
        Rental rental = new Rental();
        
        Customer customer = new Customer();
        customer.setCustomerId((Integer) data.get("customerId"));
        rental.setCustomer(customer);
        
        Inventory inventory = new Inventory();
        inventory.setInventoryId((Integer) data.get("inventoryId"));
        rental.setInventory(inventory);
        
        Staff staff = new Staff();
        staff.setStaffId((Integer) data.get("staffId"));
        rental.setStaff(staff);
        
        rental.setRentalDate(parseLocalDateTime((String) data.get("rentalDate")));
        
        return rental;
    }
    
    private Payment mapToPayment(Map<String, Object> data) {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf((Double) data.get("amount")));
        payment.setPaymentDate(parseLocalDateTime((String) data.get("paymentDate")));
        return payment;
    }
    
    // Utility Methods
    private LocalDateTime parseLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }
    
    private LocalDateTime calculateDueDate(LocalDateTime rentalDate, int rentalDuration) {
        return rentalDate.plusDays(rentalDuration);
    }
    
    private BigDecimal calculateLateFee(LocalDateTime rentalDate, LocalDateTime returnDate, int rentalDuration) {
        LocalDateTime dueDate = calculateDueDate(rentalDate, rentalDuration);
        
        if (returnDate.isAfter(dueDate)) {
            long overdueDays = java.time.Duration.between(dueDate, returnDate).toDays();
            return new BigDecimal("1.50").multiply(new BigDecimal(overdueDays));
        }
        
        return BigDecimal.ZERO;
    }
}
