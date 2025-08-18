package controller;

import service.CustomerServiceManagement;
import model.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CustomerController extends HttpServlet {
    
    private final CustomerServiceManagement customerService;
    private final ObjectMapper objectMapper;
    
    public CustomerController() {
        this.customerService = new CustomerServiceManagement();
        this.objectMapper = new ObjectMapper();
        
        //  Configure Jackson for LocalDateTime (same as FilmController)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        //  Configure to ignore null values
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        //  PREVENT INFINITE RECURSION - Add mix-ins to ignore back references
        this.objectMapper.addMixIn(Customer.class, CustomerMixin.class);
        this.objectMapper.addMixIn(Address.class, AddressMixin.class);
        this.objectMapper.addMixIn(City.class, CityMixin.class);
        this.objectMapper.addMixIn(Country.class, CountryMixin.class);
        this.objectMapper.addMixIn(Store.class, StoreMixin.class);
        this.objectMapper.addMixIn(Payment.class, PaymentMixin.class);
        this.objectMapper.addMixIn(Rental.class, RentalMixin.class);
        this.objectMapper.addMixIn(Staff.class, StaffMixin.class);
        this.objectMapper.addMixIn(Inventory.class, InventoryMixin.class);
        
        //  Configure Jackson to handle circular references
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }
    
    //  Mix-in classes to ignore circular references WITHOUT modifying POJOs
    abstract class CustomerMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();  // Simplify payment list
        @JsonIgnore abstract List<Rental> getRentalList();    // Simplify rental list
    }
    
    abstract class AddressMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Staff> getStaffList();
        @JsonIgnore abstract List<Store> getStoreList();
    }
    
    abstract class CityMixin {
        @JsonIgnore abstract List<Address> getAddressList();
    }
    
    abstract class CountryMixin {
        @JsonIgnore abstract List<City> getCityList();
    }
    
    abstract class StoreMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
        @JsonIgnore abstract List<Staff> getStaffList();
    }
    
    abstract class PaymentMixin {
        @JsonIgnore abstract Customer getCustomer();   // Ignore customer back reference
        @JsonIgnore abstract Staff getStaff();         // Ignore staff back reference
        @JsonIgnore abstract Rental getRental();       // Ignore rental back reference
    }
    
    abstract class RentalMixin {
        @JsonIgnore abstract Customer getCustomer();   // Ignore customer back reference
        @JsonIgnore abstract Staff getStaff();         // Ignore staff back reference
        @JsonIgnore abstract List<Payment> getPaymentList(); // Ignore payment list
    }
    
    abstract class StaffMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
        @JsonIgnore abstract List<Store> getStoreList();
    }
    
    abstract class InventoryMixin {
        @JsonIgnore abstract List<Rental> getRentalList();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            
            // Service returns Customer object
            Customer customer = customerService.createCustomer(requestData);
            
            //  SIMPLE RESPONSE FOR POST (no complex object)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("customerId", customer.getCustomerId());
            responseData.put("message", "Customer created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), responseData);
            
        } catch (SQLException | IllegalArgumentException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String customerIdParam = request.getParameter("customerId");
            String storeIdParam = request.getParameter("storeId");
            String activeParam = request.getParameter("active");
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            
            if (customerIdParam != null) {
                //  GET single customer with full relationships
                int customerId = Integer.parseInt(customerIdParam);
                Customer customer = customerService.getCustomerById(customerId);
                responseData.put("customer", customer);
                responseData.put("message", "Customer retrieved successfully");
                
            } else if (storeIdParam != null) {
                //  GET customers by store with full relationships
                int storeId = Integer.parseInt(storeIdParam);
                List<Customer> customers = customerService.getCustomersByStore(storeId);
                responseData.put("storeId", storeId);
                responseData.put("totalCustomers", customers.size());
                responseData.put("customers", customers);
                responseData.put("message", customers.size() + " customers found for store");
                
            } else if (activeParam != null) {
                //  GET customers by active status with full relationships
                boolean isActive = Boolean.parseBoolean(activeParam);
                List<Customer> customers = customerService.getCustomersByActiveStatus(isActive);
                responseData.put("activeFilter", isActive);
                responseData.put("totalCustomers", customers.size());
                responseData.put("customers", customers);
                responseData.put("message", customers.size() + " " + (isActive ? "active" : "inactive") + " customers found");
                
            } else {
                //  GET all customers with full relationships
                List<Customer> customers = customerService.getAllCustomers();
                responseData.put("totalCustomers", customers.size());
                responseData.put("customers", customers);
                responseData.put("message", "All customers retrieved successfully");
            }
            
            //  SAFE SERIALIZATION - Jackson will now ignore circular references
            objectMapper.writeValue(response.getOutputStream(), responseData);
            
        } catch (SQLException e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
        } catch (IllegalArgumentException e) {
            handleError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String customerIdParam = request.getParameter("customerId");
            
            if (customerIdParam != null) {
                int customerId = Integer.parseInt(customerIdParam);
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                Customer customer = customerService.updateCustomer(customerId, requestData);
                
                //  SIMPLE RESPONSE FOR PUT (no complex object)
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("customerId", customer.getCustomerId());
                responseData.put("message", "Customer updated successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Customer ID is required");
            }
            
        } catch (SQLException | IllegalArgumentException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String customerIdParam = request.getParameter("customerId");
            
            if (customerIdParam != null) {
                int customerId = Integer.parseInt(customerIdParam);
                Customer customer = customerService.deleteCustomer(customerId);
                
                //  SIMPLE RESPONSE FOR DELETE (no complex object)
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("customerId", customerId);
                responseData.put("message", "Customer deleted successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Customer ID is required");
            }
            
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    
    private void handleError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        try {
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            // Fallback if even error serialization fails
            response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
        }
    }
}
