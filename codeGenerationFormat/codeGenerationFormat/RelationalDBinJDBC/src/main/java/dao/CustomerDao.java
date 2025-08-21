package dao;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO customer (first_name, last_name, email, address_id, active, create_date, store_id, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT customer_id, first_name, last_name, email, address_id, active, create_date, store_id, last_update " +
        "FROM customer WHERE customer_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT customer_id, first_name, last_name, email, address_id, active, create_date, store_id, last_update " +
        "FROM customer ORDER BY customer_id";
    
    private static final String FIND_BY_STORE_ID_SQL = 
        "SELECT customer_id, first_name, last_name, email, address_id, active, create_date, store_id, last_update " +
        "FROM customer WHERE store_id = ? ORDER BY customer_id";
    
    private static final String UPDATE_SQL = 
        "UPDATE customer SET first_name = ?, last_name = ?, email = ?, address_id = ?, active = ?, " +
        "store_id = ?, last_update = ? WHERE customer_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM customer WHERE customer_id = ?";
    
    public int insert(Connection connection, Customer customer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getEmail());
            
            // Get address ID from Address object (or 0 if null)
            int addressId = (customer.getAddress() != null) ? customer.getAddress().getAddressId() : 0;
            statement.setInt(4, addressId);
            
            statement.setBoolean(5, customer.isActive());
            statement.setTimestamp(6, Timestamp.valueOf(customer.getCreateDate()));
            
            // Get store ID from Store object (or 0 if null)
            int storeId = (customer.getStore() != null) ? customer.getStore().getStoreId() : 0;
            statement.setInt(7, storeId);
            
            statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int customerID = generatedKeys.getInt(1);
                    customer.setCustomerId(customerID);
                    return customerID;
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }
    
    // Primary method with ID naming
    public Customer findByID(Connection connection, int customerID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, customerID);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCustomerFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    //  ADD THIS - Alias for camelCase
    public Customer findById(Connection connection, int customerId) throws SQLException {
        return findByID(connection, customerId);
    }
    
    public List<Customer> findAll(Connection connection) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                customers.add(extractCustomerFromResultSet(resultSet));
            }
        }
        
        return customers;
    }
    
    // Primary method with ID naming
    public List<Customer> findByStoreID(Connection connection, int storeID) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_STORE_ID_SQL)) {
            statement.setInt(1, storeID);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(extractCustomerFromResultSet(resultSet));
                }
            }
        }
        
        return customers;
    }
    
    //  ADD THIS - Alias for camelCase
    public List<Customer> findByStoreId(Connection connection, int storeId) throws SQLException {
        return findByStoreID(connection, storeId);
    }
    
    public void update(Connection connection, Customer customer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getEmail());
            
            // Get address ID from Address object (or 0 if null)
            int addressId = (customer.getAddress() != null) ? customer.getAddress().getAddressId() : 0;
            statement.setInt(4, addressId);
            
            statement.setBoolean(5, customer.isActive());
            
            // Get store ID from Store object (or 0 if null)
            int storeId = (customer.getStore() != null) ? customer.getStore().getStoreId() : 0;
            statement.setInt(6, storeId);
            
            statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            statement.setInt(8, customer.getCustomerId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating customer failed, no rows affected.");
            }
        }
    }
    
    // Primary method with ID naming
    public void deleteByID(Connection connection, int customerID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, customerID);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting customer failed, no rows affected.");
            }
        }
    }
    
    //  ADD THIS - Alias for camelCase
    public void deleteById(Connection connection, int customerId) throws SQLException {
        deleteByID(connection, customerId);
    }
    
    private Customer extractCustomerFromResultSet(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(resultSet.getInt("customer_id"));
        customer.setFirstName(resultSet.getString("first_name"));
        customer.setLastName(resultSet.getString("last_name"));
        customer.setEmail(resultSet.getString("email"));
        customer.setActive(resultSet.getBoolean("active"));
        customer.setCreateDate(resultSet.getTimestamp("create_date").toLocalDateTime());
        customer.setLastUpdate(resultSet.getTimestamp("last_update").toLocalDateTime());
        
        // Don't set Address and Store here - they will be loaded by the service layer
        // Store the IDs temporarily for service layer to use
        int addressId = resultSet.getInt("address_id");
        int storeId = resultSet.getInt("store_id");
        
        // Create placeholder objects with just IDs for service layer to use
        if (addressId > 0) {
            Address tempAddress = new Address();
            tempAddress.setAddressId(addressId);
            customer.setAddress(tempAddress);
        }
        
        if (storeId > 0) {
            Store tempStore = new Store();
            tempStore.setStoreId(storeId);
            customer.setStore(tempStore);
        }
        
        return customer;
    }
}
