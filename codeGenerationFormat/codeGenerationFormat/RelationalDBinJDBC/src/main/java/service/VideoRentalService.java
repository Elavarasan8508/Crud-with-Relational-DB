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
    
    //  FIXED: This method needs a Connection parameter
    private void validateInventoryAvailable(java.sql.Connection connection, int filmId, int storeId) throws SQLException {
        // Check if inventory exists and is available (not currently rented)
        boolean hasAvailableInventory = inventoryDao.findAvailableInventory(connection, filmId, storeId);
        if (!hasAvailableInventory) {
            throw new IllegalStateException("No available inventory for this film at this store");
        }
    }
    
    // Business Logic: Create Rental with Payment - Returns Rental object (like Store pattern)
    public Rental createRentalWithPayment(Map<String, Object> requestData) throws SQLException {
        try {
            return TransactionManager.executeInTransaction(connection -> {
                System.out.println("🎬 Starting rental creation transaction...");
                
                try {
                    // Extract and validate data
                    Map<String, Object> rentalData = (Map<String, Object>) requestData.get("rental");
                    Map<String, Object> paymentData = (Map<String, Object>) requestData.get("payment");
                    
                    // Validate required data
                    if (rentalData == null || paymentData == null) {
                        throw new IllegalArgumentException("Rental and payment data are required");
                    }
                    
                    // Create objects
                    Rental rental = mapToRental(rentalData);
                    Payment payment = mapToPayment(paymentData);
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
                    
                    //  Use the fixed validation method
                    if (returnDate == null) {
                        validateInventoryAvailable(connection, film.getFilmId(), inventory.getStore().getStoreId());
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
                    
                    // Return complete rental with relationships (like Store service)
                    Rental createdRental = buildRentalWithRelationships(connection, rentalId);
                    return createdRental;
                    
                } catch (Exception e) {
                    throw new RuntimeException("Rental creation failed", e);
                }
            });
        } catch (Exception e) {
            throw new SQLException("Failed to create rental: " + e.getMessage(), e);
        }
    }
    
    // Business Logic: Create Simple Rental - Returns Rental object
    public Rental createRental(int customerId, int inventoryId, int staffId, LocalDateTime rentalDate, BigDecimal paymentAmount) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            
            // Validate entities
            Customer customer = customerDao.findById(connection, customerId);
            if (customer == null || !customer.isActive()) {
                throw new IllegalArgumentException("Customer not found or inactive");
            }
            
            Inventory inventory = inventoryDao.findById(connection, inventoryId);
            if (inventory == null) {
                throw new IllegalArgumentException("Inventory not found");
            }
            
            Staff staff = staffDao.findById(connection, staffId);
            if (staff == null || !staff.getActive()) {
                throw new IllegalArgumentException("Staff not found or inactive");
            }
            
            Film film = filmDao.findById(connection, inventory.getFilm().getFilmId());
            if (film == null) {
                throw new IllegalArgumentException("Film not found");
            }
            
            //  Use the fixed validation method
            validateInventoryAvailable(connection, film.getFilmId(), inventory.getStore().getStoreId());
            
            // Create rental
            Rental rental = new Rental();
            rental.setCustomer(customer);
            rental.setInventory(inventory);
            rental.setStaff(staff);
            rental.setRentalDate(rentalDate != null ? rentalDate : LocalDateTime.now());
            rental.setReturnDate(null); // Active rental
            rental.setLastUpdate(LocalDateTime.now());
            
            int rentalId = rentalDao.insert(connection, rental);
            rental.setRentalId(rentalId);
            
            // Create payment
            BigDecimal amount = paymentAmount != null ? paymentAmount : film.getRentalRate();
            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setStaff(staff);
            payment.setRental(rental);
            payment.setAmount(amount);
            payment.setPaymentDate(rental.getRentalDate());
            payment.setLastUpdate(LocalDateTime.now());
            paymentDao.insert(connection, payment);
            
            Rental createdRental = buildRentalWithRelationships(connection, rentalId);
            return createdRental;
        });
    }
    
    // Business Logic: Return Film - Returns Rental object
    public Rental returnFilm(int rentalId) throws SQLException {
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
            
            Rental returnedRental = buildRentalWithRelationships(connection, rentalId);
            return returnedRental;
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
                    // Add basic rental if relationship loading fails
                    rental.setPaymentList(new ArrayList<>());
                    activeRentalsWithDetails.add(rental);
                }
            }
            
            return activeRentalsWithDetails;
        });
    }
    
    // Business Logic: Get Customer Rental History - Returns List<Rental>
    public List<Rental> getCustomerRentalHistory(int customerId) throws SQLException {
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
    
    //  Handle DELETE rental
    public Map<String, Object> handleRentalDeletion(int rentalId) throws SQLException {
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

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("rentalId", rentalId);
            responseData.put("message", "Rental deleted successfully");
            return responseData;
        });
    }

    //  Handle rentals by store
    public Map<String, Object> handleStoreRentalsQuery(int storeId) throws SQLException {
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

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("storeId", storeId);
            responseData.put("totalRentals", storeRentalsWithDetails.size());
            responseData.put("activeRentals", storeRentalsWithDetails.stream()
                .mapToLong(r -> r.getReturnDate() == null ? 1 : 0).sum());
            responseData.put("rentals", storeRentalsWithDetails);
            responseData.put("message", "Rentals retrieved successfully for store ID " + storeId);

            return responseData;
        });
    }

    //  Handle overdue rentals
    public Map<String, Object> handleOverdueRentalsQuery() throws SQLException {
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

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("totalOverdueRentals", overdueRentalsWithDetails.size());
            responseData.put("overdueRentals", overdueRentalsWithDetails);
            responseData.put("message", "Overdue rentals retrieved successfully");
            responseData.put("checkDate", LocalDateTime.now().toString());

            return responseData;
        });
    }
    
    // Controller Support Methods
    public Map<String, Object> handleRentalCreation(Map<String, Object> requestData) throws SQLException {
        try {
            // Check request structure and delegate to appropriate business method
            if (requestData.containsKey("rental") && requestData.containsKey("payment")) {
                // Handle nested structure
                Rental rental = createRentalWithPayment(requestData);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("rentalId", rental.getRentalId());
                responseData.put("message", "Rental and payment created successfully");
                
                return responseData;
                
            } else {
                // Handle flat structure
                Integer customerId = (Integer) requestData.get("customerId");
                Integer inventoryId = (Integer) requestData.get("inventoryId");
                Integer staffId = (Integer) requestData.get("staffId");
                String rentalDateStr = (String) requestData.get("rentalDate");
                Double paymentAmountDouble = (Double) requestData.get("paymentAmount");
                
                if (customerId == null || inventoryId == null || staffId == null) {
                    throw new IllegalArgumentException("Customer ID, Inventory ID, and Staff ID are required");
                }
                
                LocalDateTime rentalDate = parseLocalDateTime(rentalDateStr);
                BigDecimal paymentAmount = paymentAmountDouble != null ? BigDecimal.valueOf(paymentAmountDouble) : null;
                
                Rental rental = createRental(customerId, inventoryId, staffId, rentalDate, paymentAmount);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("rentalId", rental.getRentalId());
                responseData.put("message", "Rental created successfully");
                
                return responseData;
            }
            
        } catch (Exception e) {
            throw new SQLException("Rental creation failed: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Object> handleRentalQuery(int rentalId) throws SQLException {
        Rental rental = getRentalById(rentalId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("rental", rental);
        responseData.put("message", "Rental retrieved successfully");
        
        return responseData;
    }
    
    public Map<String, Object> handleActiveRentalsQuery() throws SQLException {
        List<Rental> activeRentals = getAllActiveRentals();
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("totalActiveRentals", activeRentals.size());
        responseData.put("rentals", activeRentals);
        responseData.put("message", "All active rentals retrieved successfully");
        
        return responseData;
    }
    
    public Map<String, Object> handleCustomerHistoryQuery(int customerId) throws SQLException {
        List<Rental> rentals = getCustomerRentalHistory(customerId);
        
        // Get customer info for response
        Customer customer = rentals.isEmpty() ? null : rentals.get(0).getCustomer();
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("customerId", customerId);
        if (customer != null) {
            responseData.put("customerName", customer.getFirstName() + " " + customer.getLastName());
        }
        responseData.put("totalRentals", rentals.size());
        responseData.put("activeRentals", rentals.stream().mapToLong(r -> r.getReturnDate() == null ? 1 : 0).sum());
        responseData.put("rentalHistory", rentals);
        responseData.put("message", "Customer rental history retrieved successfully");
        
        return responseData;
    }
    
    public Map<String, Object> handleFilmReturn(String rentalIdParam, jakarta.servlet.http.HttpServletRequest request) throws SQLException {
        try {
            if (rentalIdParam == null) {
                // Try to get from request body
                Map<String, Object> requestData = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(request.getInputStream(), Map.class);
                if (requestData.get("rentalId") != null) {
                    rentalIdParam = requestData.get("rentalId").toString();
                }
            }
            
            if (rentalIdParam == null) {
                throw new IllegalArgumentException("Rental ID is required");
            }
            
            int rentalId = Integer.parseInt(rentalIdParam);
            Rental returnedRental = returnFilm(rentalId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("rentalId", rentalId);
            responseData.put("message", "Film returned successfully");
            
            return responseData;
            
        } catch (Exception e) {
            throw new SQLException("Film return failed: " + e.getMessage(), e);
        }
    }
    
    //  Handle all rentals query
    public Map<String, Object> handleAllRentalsQuery() throws SQLException {
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

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("totalRentals", allRentalsWithDetails.size());
            responseData.put("activeRentals", allRentalsWithDetails.stream()
                .mapToLong(r -> r.getReturnDate() == null ? 1 : 0).sum());
            responseData.put("rentals", allRentalsWithDetails);
            responseData.put("message", "All rentals retrieved successfully");

            return responseData;
        });
    }
    
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
