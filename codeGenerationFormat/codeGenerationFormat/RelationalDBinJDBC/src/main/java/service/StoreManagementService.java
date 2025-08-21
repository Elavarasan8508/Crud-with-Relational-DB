package service;

import dao.*;
import model.*;
import DataBaseConnection.TransactionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StoreManagementService {
    
    private final StoreDao storeDao;
    private final StaffDao staffDao;
    private final AddressDao addressDao;
    private final CityDao cityDao;
    private final CountryDao countryDao;
    private final CustomerDao customerDao;
    private final InventoryDao inventoryDao;
    private final FilmDao filmDao;
    private final RentalDao rentalDao;
    
    public StoreManagementService() {
        this.storeDao = new StoreDao();
        this.staffDao = new StaffDao();
        this.addressDao = new AddressDao();
        this.cityDao = new CityDao();
        this.countryDao = new CountryDao();
        this.customerDao = new CustomerDao();
        this.inventoryDao = new InventoryDao();
        this.filmDao = new FilmDao();
        this.rentalDao = new RentalDao();
    }
    
    // Business Logic: Create Store - Returns Store object
    public Store createStore(Map<String, Object> requestData) throws SQLException {
        try {
            return TransactionManager.executeInTransaction(connection -> {
                System.out.println("🏪 Starting store creation transaction...");
                
                try {
                    // Extract and validate data
                    Map<String, Object> storeData = (Map<String, Object>) requestData.get("store");
                    Map<String, Object> managerData = (Map<String, Object>) requestData.get("manager");
                    Map<String, Object> addressData = (Map<String, Object>) requestData.get("address");
                    Map<String, Object> cityData = (Map<String, Object>) requestData.get("city");
                    Map<String, Object> countryData = (Map<String, Object>) requestData.get("country");
                    
                    // Validate required data
                    if (managerData == null || addressData == null) {
                        throw new IllegalArgumentException("Manager and address data are required");
                    }
                    
                    // Create objects
                    Store store = new Store();
                    Staff manager = mapToStaff(managerData);
                    Address address = mapToAddress(addressData);
                    City city = cityData != null ? mapToCity(cityData) : null;
                    Country country = countryData != null ? mapToCountry(countryData) : null;
                    
                    // Create location hierarchy
                    if (country != null) {
                        country.setLastUpdate(LocalDateTime.now());
                        int countryId = countryDao.insert(connection, country);
                        country.setCountryId(countryId);
                        
                        if (city != null) {
                            city.setCountry(country); // Set complete country object
                            city.setLastUpdate(LocalDateTime.now());
                            int cityId = cityDao.insert(connection, city);
                            city.setCityId(cityId);
                            
                            address.setCity(city); // Set complete city object
                        }
                    }
                    
                    address.setLastUpdate(LocalDateTime.now());
                    int addressId = addressDao.insert(connection, address);
                    address.setAddressId(addressId);
                    
                    // Step 1: Create store WITHOUT manager (set placeholder address)
                    Address storeAddress = new Address();
                    storeAddress.setAddressId(addressId);
                    store.setAddress(storeAddress);
                    store.setLastUpdate(LocalDateTime.now());
                    int storeId = storeDao.insert(connection, store);
                    
                    // Step 2: Create manager with store ID and address
                    manager.setAddress(address); // Set complete address object
                    Store managerStore = new Store();
                    managerStore.setStoreId(storeId);
                    manager.setStore(managerStore); // Set store object
                    manager.setLastUpdate(LocalDateTime.now());
                    int staffId = staffDao.insert(connection, manager);
                    manager.setStaffId(staffId);
                    
                    // Step 3: Update store with manager reference
                    store.setStoreId(storeId);
                    store.setManagerStaff(manager); // Set complete manager object
                    store.setAddress(address); // Set complete address object
                    storeDao.update(connection, store);
                    
                    Store createdStore = buildStoreWithRelationships(connection, storeId);
                    return createdStore;
                    
                } catch (Exception e) {
                    throw new RuntimeException("Store creation failed", e);
                }
            });
        } catch (Exception e) {
            throw new SQLException("Failed to create store: " + e.getMessage(), e);
        }
    }
    
    // Business Logic: Get Store by ID - Returns Store object
    public Store getStoreById(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = buildStoreWithRelationships(connection, storeId);
            
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            return store;
        });
    }
    
    // Business Logic: Get All Stores - Returns List<Store>
    public List<Store> getAllStores() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Store> stores = storeDao.findAll(connection);
            List<Store> storesWithDetails = new ArrayList<>();
            
            for (Store store : stores) {
                try {
                    Store storeWithDetails = buildStoreWithRelationships(connection, store.getStoreId());
                    if (storeWithDetails != null) {
                        storesWithDetails.add(storeWithDetails);
                    }
                } catch (Exception e) {
                    // Add basic store if relationship loading fails
                    store.setStaffList(new ArrayList<>());
                    store.setCustomerList(new ArrayList<>());
                    store.setInventoryList(new ArrayList<>());
                    storesWithDetails.add(store);
                }
            }
            
            return storesWithDetails;
        });
    }
    
    // Business Logic: Get Stores by City - Returns List<Store>
    public List<Store> getStoresByCity(String cityName) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Store> allStores = getAllStores();
            List<Store> cityStores = allStores.stream()
                .filter(store -> {
                    if (store.getAddress() != null && 
                        store.getAddress().getCity() != null && 
                        store.getAddress().getCity().getCity() != null) {
                        return store.getAddress().getCity().getCity().equalsIgnoreCase(cityName);
                    }
                    return false;
                })
                .collect(Collectors.toList());
            
            return cityStores;
        });
    }
    
    // Business Logic: Get Store Customers - Returns List<Customer>
    public List<Customer> getStoreCustomers(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = storeDao.findById(connection, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            List<Customer> customers = customerDao.findByStoreId(connection, storeId);
            return customers != null ? customers : new ArrayList<>();
        });
    }
    
    // Business Logic: Get Store Staff - Returns List<Staff>
    public List<Staff> getStoreStaff(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = storeDao.findById(connection, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            List<Staff> staff = staffDao.findByStoreId(connection, storeId);
            return staff != null ? staff : new ArrayList<>();
        });
    }
    
    // Business Logic: Get Store Rentals - Returns List<Rental>
    public List<Rental> getStoreRentals(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = storeDao.findById(connection, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            // Get all rentals where inventory belongs to this store
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
            
            return storeRentals;
        });
    }
    
    // Business Logic: Get Store Inventory - Returns List<Map<String, Object>>
    public List<Map<String, Object>> getStoreInventory(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = storeDao.findById(connection, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            List<Inventory> inventoryList = inventoryDao.findByStoreId(connection, storeId);
            
            // Load film details for each inventory item
            List<Map<String, Object>> inventoryWithFilms = new ArrayList<>();
            for (Inventory inventory : inventoryList) {
                Map<String, Object> inventoryData = new HashMap<>();
                inventoryData.put("inventoryId", inventory.getInventoryId());
                inventoryData.put("storeId", storeId);
                
                if (inventory.getFilm() != null) {
                    Film film = filmDao.findById(connection, inventory.getFilm().getFilmId());
                    inventoryData.put("film", film);
                }
                
                inventoryData.put("lastUpdate", inventory.getLastUpdate().toString());
                inventoryWithFilms.add(inventoryData);
            }
            
            return inventoryWithFilms;
        });
    }
    
    // Business Logic: Update Store - Returns updated Store object
    public Store updateStore(int storeId, Map<String, Object> requestData) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store existingStore = storeDao.findById(connection, storeId);
            if (existingStore == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            // Update only the fields that are provided
            Map<String, Object> storeData = (Map<String, Object>) requestData.get("store");
            if (storeData != null) {
                // Preserve existing values
                existingStore.setLastUpdate(LocalDateTime.now());
                storeDao.update(connection, existingStore);
            }
            
            Store store = buildStoreWithRelationships(connection, storeId);
            return store;
        });
    }
    
    // Business Logic: Delete Store - Returns deleted Store object
    public Store deleteStore(int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Store store = buildStoreWithRelationships(connection, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            // Business rule validations
            List<Customer> customers = customerDao.findByStoreId(connection, storeId);
            if (!customers.isEmpty()) {
                throw new IllegalStateException("Cannot delete store with existing customers");
            }
            
            List<Inventory> inventory = inventoryDao.findByStoreId(connection, storeId);
            if (!inventory.isEmpty()) {
                throw new IllegalStateException("Cannot delete store with existing inventory");
            }
            
            storeDao.deleteById(connection, storeId);
            return store;
        });
    }
    
    // Business Logic: Build Store with Relationships
    private Store buildStoreWithRelationships(java.sql.Connection connection, int storeId) throws SQLException {
        Store store = storeDao.findById(connection, storeId);
        if (store == null) return null;
        
        try {
            // Load manager (DAO already creates placeholder Staff with ID)
            if (store.getManagerStaff() != null && store.getManagerStaff().getStaffId() > 0) {
                Staff manager = staffDao.findById(connection, store.getManagerStaff().getStaffId());
                if (manager != null) {
                    store.setManagerStaff(manager);
                }
            }
            
            // Load address (DAO already creates placeholder Address with ID)
            if (store.getAddress() != null && store.getAddress().getAddressId() > 0) {
                Address address = addressDao.findById(connection, store.getAddress().getAddressId());
                if (address != null) {
                    // Load city and country
                    if (address.getCity() != null && address.getCity().getCityId() > 0) {
                        City city = cityDao.findById(connection, address.getCity().getCityId());
                        if (city != null) {
                            address.setCity(city);
                            
                            if (city.getCountry() != null && city.getCountry().getCountryId() > 0) {
                                Country country = countryDao.findById(connection, city.getCountry().getCountryId());
                                if (country != null) {
                                    city.setCountry(country);
                                }
                            }
                        }
                    }
                    store.setAddress(address);
                }
            }
            
            // Load staff
            List<Staff> staff = staffDao.findByStoreId(connection, storeId);
            store.setStaffList(staff != null ? staff : new ArrayList<>());
            
            // Load customers
            List<Customer> customers = customerDao.findByStoreId(connection, storeId);
            store.setCustomerList(customers != null ? customers : new ArrayList<>());
            
            // Load inventory
            List<Inventory> inventory = inventoryDao.findByStoreId(connection, storeId);
            store.setInventoryList(inventory != null ? inventory : new ArrayList<>());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error loading relationships for store " + storeId + ": " + e.getMessage());
            store.setStaffList(new ArrayList<>());
            store.setCustomerList(new ArrayList<>());
            store.setInventoryList(new ArrayList<>());
        }
        
        return store;
    }
    
    // Helper mapping methods
    private Staff mapToStaff(Map<String, Object> data) {
        Staff staff = new Staff();
        staff.setFirstName((String) data.get("firstName"));
        staff.setLastName((String) data.get("lastName"));
        staff.setEmail((String) data.get("email"));
        staff.setActive(data.get("active") != null ? (Boolean) data.get("active") : true);
        staff.setUsername((String) data.get("username"));
        staff.setPassword((String) data.get("password"));
        return staff;
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
}
