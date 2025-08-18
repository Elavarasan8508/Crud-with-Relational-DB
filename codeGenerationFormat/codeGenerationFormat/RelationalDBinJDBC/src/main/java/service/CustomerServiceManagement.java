package service;

import dao.*;
import model.*;
import DataBaseConnection.TransactionManager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

public class CustomerServiceManagement {
    
    private final CustomerDao customerDao;
    private final AddressDao addressDao;
    private final CityDao cityDao;
    private final CountryDao countryDao;
    private final StoreDao storeDao;
    private final RentalDao rentalDao;
    private final PaymentDao paymentDao;
    
    public CustomerServiceManagement() {
        this.customerDao = new CustomerDao();
        this.addressDao = new AddressDao();
        this.cityDao = new CityDao();
        this.countryDao = new CountryDao();
        this.storeDao = new StoreDao();
        this.rentalDao = new RentalDao();
        this.paymentDao = new PaymentDao();
    }
    
    // Business Logic: Create Customer - Returns Customer object
    public Customer createCustomer(Map<String, Object> requestData) throws SQLException {
        try {
            return TransactionManager.executeInTransaction(connection -> {
                System.out.println("👤 Starting customer creation transaction...");
                
                try {
                    // Extract and validate data
                    Map<String, Object> customerData = (Map<String, Object>) requestData.get("customer");
                    Map<String, Object> addressData = (Map<String, Object>) requestData.get("address");
                    Map<String, Object> cityData = (Map<String, Object>) requestData.get("city");
                    Map<String, Object> countryData = (Map<String, Object>) requestData.get("country");
                    Integer storeId = (Integer) requestData.get("storeId");
                    
                    // Validate required data
                    if (customerData == null || storeId == null) {
                        throw new IllegalArgumentException("Customer data and store ID are required");
                    }
                    
                    // Create objects
                    Customer customer = mapToCustomer(customerData);
                    Address address = addressData != null ? mapToAddress(addressData) : null;
                    City city = cityData != null ? mapToCity(cityData) : null;
                    Country country = countryData != null ? mapToCountry(countryData) : null;
                    
                    // Verify store exists
                    Store store = storeDao.findById(connection, storeId);
                    if (store == null) {
                        throw new IllegalArgumentException("Store not found with ID: " + storeId);
                    }
                    
                 // Create relationships
                    if (country != null) {
                        // Set LocalDateTime instead of Timestamp
                        country.setLastUpdate(LocalDateTime.now());
                        int countryId = countryDao.insert(connection, country);
                        
                        if (city != null) {
                            // Set the complete country object with ID
                            country.setCountryId(countryId); // Set ID on country object
                            city.setCountry(country);        // Set complete country object on city
                            city.setLastUpdate(LocalDateTime.now()); // Use LocalDateTime
                            int cityId = cityDao.insert(connection, city);
                            
                            if (address != null) {
                                // Set the complete city object with ID
                                city.setCityId(cityId);      // Set ID on city object
                                address.setCity(city);       // Set complete city object on address
                                address.setLastUpdate(LocalDateTime.now()); // Use LocalDateTime
                                int addressId = addressDao.insert(connection, address);
                                
                                // Set address with ID on customer
                                address.setAddressId(addressId);
                                customer.setAddress(address);
                            }
                        }
                    }

                    
                    // Set store on customer
                    customer.setStore(store);
                    customer.setCreateDate(LocalDateTime.now());
                    customer.setLastUpdate(LocalDateTime.now());
                    
                    int customerId = customerDao.insert(connection, customer);
                    Customer createdCustomer = buildCustomerWithRelationships(connection, customerId);
                    
                    return createdCustomer;
                    
                } catch (Exception e) {
                    throw new RuntimeException("Customer creation failed", e);
                }
            });
        } catch (Exception e) {
            throw new SQLException("Failed to create customer: " + e.getMessage(), e);
        }
    }
    
    // Business Logic: Get Customer by ID - Returns Customer object
    public Customer getCustomerById(int customerId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Customer customer = buildCustomerWithRelationships(connection, customerId);
            
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found with ID: " + customerId);
            }
            
            return customer;
        });
    }
    
    // Business Logic: Get All Customers - Returns List<Customer>
    public List<Customer> getAllCustomers() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Customer> customers = customerDao.findAll(connection);
            List<Customer> customersWithDetails = new ArrayList<>();
            
            for (Customer customer : customers) {
                try {
                    Customer customerWithDetails = buildCustomerWithRelationships(connection, customer.getCustomerId());
                    if (customerWithDetails != null) {
                        customersWithDetails.add(customerWithDetails);
                    }
                } catch (Exception e) {
                    customer.setRentalList(new ArrayList<>());
                    customer.setPaymentList(new ArrayList<>());
                    customersWithDetails.add(customer);
                }
            }
            
            return customersWithDetails;
        });
    }
    
    // Business Logic: Get Customers by Store - Returns List<Customer>
    public List<Customer> getCustomersByStore(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Customer> customers = customerDao.findByStoreId(connection, storeId);
            List<Customer> customersWithDetails = new ArrayList<>();
            
            for (Customer customer : customers) {
                try {
                    Customer customerWithDetails = buildCustomerWithRelationships(connection, customer.getCustomerId());
                    if (customerWithDetails != null) {
                        customersWithDetails.add(customerWithDetails);
                    }
                } catch (Exception e) {
                    customer.setRentalList(new ArrayList<>());
                    customer.setPaymentList(new ArrayList<>());
                    customersWithDetails.add(customer);
                }
            }
            
            return customersWithDetails;
        });
    }
    
    // Business Logic: Get Customers by Active Status - Returns List<Customer>
    public List<Customer> getCustomersByActiveStatus(boolean isActive) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Customer> allCustomers = customerDao.findAll(connection);
            List<Customer> filteredCustomers = new ArrayList<>();
            
            for (Customer customer : allCustomers) {
                if (customer.isActive() == isActive) {
                    try {
                        Customer customerWithDetails = buildCustomerWithRelationships(connection, customer.getCustomerId());
                        if (customerWithDetails != null) {
                            filteredCustomers.add(customerWithDetails);
                        }
                    } catch (Exception e) {
                        customer.setRentalList(new ArrayList<>());
                        customer.setPaymentList(new ArrayList<>());
                        filteredCustomers.add(customer);
                    }
                }
            }
            
            return filteredCustomers;
        });
    }
    
    // Business Logic: Update Customer - Returns updated Customer object
    public Customer updateCustomer(int customerId, Map<String, Object> requestData) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Customer existingCustomer = customerDao.findById(connection, customerId);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("Customer not found with ID: " + customerId);
            }
            
            Map<String, Object> customerData = (Map<String, Object>) requestData.get("customer");
            Customer updatedCustomer = mapToCustomer(customerData);
            updatedCustomer.setCustomerId(customerId);
            updatedCustomer.setLastUpdate(LocalDateTime.now());
            
            // Preserve existing address and store if not being updated
            if (updatedCustomer.getAddress() == null) {
                updatedCustomer.setAddress(existingCustomer.getAddress());
            }
            if (updatedCustomer.getStore() == null) {
                updatedCustomer.setStore(existingCustomer.getStore());
            }
            
            customerDao.update(connection, updatedCustomer);
            Customer customer = buildCustomerWithRelationships(connection, customerId);
            
            return customer;
        });
    }
    
    // Business Logic: Delete Customer - Returns deleted Customer object
    public Customer deleteCustomer(int customerId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Customer customer = buildCustomerWithRelationships(connection, customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found with ID: " + customerId);
            }
            
            // Business rule: Check for active rentals
            boolean hasActiveRentals = hasActiveRentals(customer);
            if (hasActiveRentals) {
                throw new IllegalStateException("Cannot delete customer with active rentals");
            }
            
            customerDao.deleteById(connection, customerId);
            
            return customer;
        });
    }
    
    // Business Logic Methods
    private boolean hasActiveRentals(Customer customer) {
        if (customer.getRentalList() == null || customer.getRentalList().isEmpty()) {
            return false;
        }
        return customer.getRentalList().stream()
            .anyMatch(rental -> rental.getReturnDate() == null);
    }
    
    private boolean isNewCustomer(Customer customer) {
        if (customer.getCreateDate() == null) return false;
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return customer.getCreateDate().isAfter(oneMonthAgo);
    }
    
    private BigDecimal getTotalPayments(Customer customer) {
        if (customer.getPaymentList() == null || customer.getPaymentList().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return customer.getPaymentList().stream()
            .map(Payment::getAmount)  
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    
    private Customer buildCustomerWithRelationships(java.sql.Connection connection, int customerId) throws SQLException {
        Customer customer = customerDao.findById(connection, customerId);
        if (customer == null) return null;
        
        // Load relationships
        try {
            // Load address with city and country
            if (customer.getAddress() != null && customer.getAddress().getAddressId() > 0) {
                Address address = addressDao.findById(connection, customer.getAddress().getAddressId());
                if (address != null) {
                    // Load city
                    if (address.getCity().getCityId() > 0) {
                        City city = cityDao.findById(connection, address.getCity().getCityId());
                        if (city != null) {
                            address.setCity(city);
                            
                            // Load country
                            if (city.getCountry().getCountryId() > 0) {
                                Country country = countryDao.findById(connection, city.getCountry().getCountryId());
                                if (country != null) {
                                    city.setCountry(country);
                                }
                            }
                        }
                    }
                    customer.setAddress(address);
                }
            }
            
            // Load store
            if (customer.getStore() != null && customer.getStore().getStoreId() > 0) {
                Store store = storeDao.findById(connection, customer.getStore().getStoreId());
                if (store != null) {
                    customer.setStore(store);
                }
            }
            
            // Load rentals and payments
            List<Rental> rentals = rentalDao.findByCustomerId(connection, customerId);
            customer.setRentalList(rentals != null ? rentals : new ArrayList<>());
            
            List<Payment> payments = paymentDao.findByCustomerId(connection, customerId);
            customer.setPaymentList(payments != null ? payments : new ArrayList<>());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error loading relationships for customer " + customerId + ": " + e.getMessage());
            customer.setRentalList(new ArrayList<>());
            customer.setPaymentList(new ArrayList<>());
        }
        
        return customer;
    }
    
    // Helper mapping methods
    private Customer mapToCustomer(Map<String, Object> data) {
        Customer customer = new Customer();
        customer.setFirstName((String) data.get("firstName"));
        customer.setLastName((String) data.get("lastName"));
        customer.setEmail((String) data.get("email"));
        
        if (data.get("active") != null) {
            if (data.get("active") instanceof Boolean) {
                customer.setActive((Boolean) data.get("active"));
            } else {
                customer.setActive(Boolean.parseBoolean(data.get("active").toString()));
            }
        } else {
            customer.setActive(true);
        }
        
        return customer;
    }
    
    private Address mapToAddress(Map<String, Object> data) {
        Address address = new Address();
        address.setAddress((String) data.get("address"));
        address.setAddress2((String) data.get("address2"));
        address.setDistrict((String) data.get("district"));
        address.setPostalCode((String) data.get("postalCode"));
        address.setPhone((String) data.get("phone"));
        return address;
    }
    
    private City mapToCity(Map<String, Object> data) {
        City city = new City();
        city.setCity((String) data.get("city"));
        return city;
    }
    
    private Country mapToCountry(Map<String, Object> data) {
        Country country = new Country();
        country.setCountry((String) data.get("country"));
        return country;
    }
    
    // Helper methods for getting IDs from objects
    private int getAddressIdFromCustomer(Customer customer) {
        if (customer.getAddress() != null && customer.getAddress().getAddressId() > 0) {
            return customer.getAddress().getAddressId();
        }
        return 0;
    }

    private int getStoreIdFromCustomer(Customer customer) {
        if (customer.getStore() != null && customer.getStore().getStoreId() > 0) {
            return customer.getStore().getStoreId();
        }
        return 0;
    }

    private void setAddressIdOnCustomer(Customer customer, int addressId) {
        if (addressId > 0) {
            if (customer.getAddress() == null) {
                customer.setAddress(new Address());
            }
            customer.getAddress().setAddressId(addressId);
        }
    }

    private void setStoreIdOnCustomer(Customer customer, int storeId) {
        if (storeId > 0) {
            if (customer.getStore() == null) {
                customer.setStore(new Store());
            }
            customer.getStore().setStoreId(storeId);
        }
    }
}
