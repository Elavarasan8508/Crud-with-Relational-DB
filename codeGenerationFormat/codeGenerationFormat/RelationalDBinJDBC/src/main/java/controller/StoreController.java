package controller;

import service.StoreManagementService;
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
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class StoreController extends HttpServlet {
    
    private final StoreManagementService storeService;
    private final ObjectMapper objectMapper;
    
    public StoreController() {
        this.storeService = new StoreManagementService();
        this.objectMapper = new ObjectMapper();
        
        //  Configure Jackson for LocalDateTime (same as other controllers)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        //  Configure to ignore null values
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        //  PREVENT INFINITE RECURSION - Add mix-ins to ignore back references
        this.objectMapper.addMixIn(Store.class, StoreMixin.class);
        this.objectMapper.addMixIn(Address.class, AddressMixin.class);
        this.objectMapper.addMixIn(Staff.class, StaffMixin.class);
        this.objectMapper.addMixIn(Customer.class, CustomerMixin.class);
        this.objectMapper.addMixIn(Inventory.class, InventoryMixin.class);
        this.objectMapper.addMixIn(Film.class, FilmMixin.class);
        this.objectMapper.addMixIn(City.class, CityMixin.class);
        this.objectMapper.addMixIn(Country.class, CountryMixin.class);
        this.objectMapper.addMixIn(Rental.class, RentalMixin.class);
        this.objectMapper.addMixIn(Payment.class, PaymentMixin.class);
        
        //  Configure Jackson to handle circular references
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }
    
    //  Mix-in classes to ignore circular references WITHOUT modifying POJOs
    abstract class StoreMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
        @JsonIgnore abstract List<Staff> getStaffList();
    }
    
    abstract class AddressMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Staff> getStaffList();
        @JsonIgnore abstract List<Store> getStoreList();
    }
    
    abstract class StaffMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
        @JsonIgnore abstract List<Store> getStoreList();
    }
    
    abstract class CustomerMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
    }
    
    abstract class InventoryMixin {
        @JsonIgnore abstract List<Rental> getRentalList();
    }
    
    abstract class FilmMixin {
        @JsonIgnore abstract List<FilmActor> getFilmActorList();
        @JsonIgnore abstract List<FilmCategory> getFilmCategoryList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
    }
    
    abstract class CityMixin {
        @JsonIgnore abstract List<Address> getAddressList();
    }
    
    abstract class CountryMixin {
        @JsonIgnore abstract List<City> getCityList();
    }
    
    abstract class RentalMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
    }
    
    abstract class PaymentMixin {
        @JsonIgnore abstract Customer getCustomer();
        @JsonIgnore abstract Staff getStaff();
        @JsonIgnore abstract Rental getRental();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            
            //  SIMPLE RESPONSE FOR POST (no complex store object)
            Map<String, Object> result = storeService.handleStoreCreation(requestData);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), result);
            
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
            String storeIdParam = request.getParameter("storeId");
            String inventory = request.getParameter("inventory");
            String customers = request.getParameter("customers");
            String staff = request.getParameter("staff");
            String rentals = request.getParameter("rentals");
            String city = request.getParameter("city");
            
            Map<String, Object> result;
            
            if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);
                
                if ("true".equals(inventory)) {
                    //  GET store inventory with full relationships
                    result = storeService.handleInventoryQuery(storeId);
                    
                } else if ("true".equals(customers)) {
                    //  GET store customers with full relationships
                    result = storeService.handleStoreCustomersQuery(storeId);
                    
                } else if ("true".equals(staff)) {
                    //  GET store staff with full relationships
                    result = storeService.handleStoreStaffQuery(storeId);
                    
                } else if ("true".equals(rentals)) {
                    //  GET store rentals with full relationships
                    result = storeService.handleStoreRentalsQuery(storeId);
                    
                } else {
                    //  GET single store with full relationships
                    result = storeService.handleStoreQuery(storeId);
                }
                
            } else if (city != null) {
                //  GET stores by city with full relationships
                result = storeService.handleStoresByCityQuery(city);
                
            } else {
                //  GET all stores with full relationships
                result = storeService.handleAllStoresQuery();
            }
            
            //  SAFE SERIALIZATION - Jackson will now ignore circular references
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (SQLException e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid store ID");
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
            String storeIdParam = request.getParameter("storeId");
            
            if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                //  SIMPLE RESPONSE FOR PUT (no complex store object)
                Map<String, Object> result = storeService.handleStoreUpdate(storeId, requestData);
                objectMapper.writeValue(response.getOutputStream(), result);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required");
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
            String storeIdParam = request.getParameter("storeId");
            
            if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);
                
                //  SIMPLE RESPONSE FOR DELETE (no complex store object)
                Map<String, Object> result = storeService.handleStoreDeletion(storeId);
                objectMapper.writeValue(response.getOutputStream(), result);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Store ID is required");
            }
            
        } catch (SQLException | IllegalArgumentException e) {
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
