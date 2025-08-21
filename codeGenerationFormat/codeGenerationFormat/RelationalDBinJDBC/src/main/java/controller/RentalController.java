package controller;

import service.VideoRentalService;
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

public class RentalController extends HttpServlet {
    
    private final VideoRentalService rentalService;
    private final ObjectMapper objectMapper;
    
    public RentalController() {
        this.rentalService = new VideoRentalService();
        this.objectMapper = new ObjectMapper();
        
        //  Configure Jackson for LocalDateTime (same as other controllers)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        //  Configure to ignore null values
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        //  PREVENT INFINITE RECURSION - Add mix-ins to ignore back references
        this.objectMapper.addMixIn(Rental.class, RentalMixin.class);
        this.objectMapper.addMixIn(Customer.class, CustomerMixin.class);
        this.objectMapper.addMixIn(Staff.class, StaffMixin.class);
        this.objectMapper.addMixIn(Inventory.class, InventoryMixin.class);
        this.objectMapper.addMixIn(Film.class, FilmMixin.class);
        this.objectMapper.addMixIn(Store.class, StoreMixin.class);
        this.objectMapper.addMixIn(Payment.class, PaymentMixin.class);
        
        //  Configure Jackson to handle circular references
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }
    
    //  Mix-in classes to ignore circular references WITHOUT modifying POJOs
    abstract class RentalMixin {
        @JsonIgnore abstract List<Payment> getPaymentList(); // Ignore payment list to avoid cycles
    }
    
    abstract class CustomerMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
    }
    
    abstract class StaffMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
        @JsonIgnore abstract List<Store> getStoreList();
    }
    
    abstract class InventoryMixin {
        @JsonIgnore abstract List<Rental> getRentalList();
    }
    
    abstract class FilmMixin {
        @JsonIgnore abstract List<FilmActor> getFilmActorList();
        @JsonIgnore abstract List<FilmCategory> getFilmCategoryList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
    }
    
    abstract class StoreMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
        @JsonIgnore abstract List<Staff> getStaffList();
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
            
            // Service returns Rental object
            Rental rental = rentalService.createRental(requestData);
            
            //  SIMPLE RESPONSE FOR POST (no complex rental object)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("rentalId", rental.getRentalId());
            responseData.put("message", "Rental created successfully");
            
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
            String activeRentals = request.getParameter("activeRentals");
            String rentalIdParam = request.getParameter("rentalId");
            String storeIdParam = request.getParameter("storeId");
            String overdueParam = request.getParameter("overdue");
            String apiInfo = request.getParameter("info");
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            
            if (customerIdParam != null) {
                // GET customer rental history with full relationships
                int customerId = Integer.parseInt(customerIdParam);
                List<Rental> rentals = rentalService.getCustomerRentals(customerId);
                responseData.put("customerId", customerId);
                responseData.put("totalRentals", rentals.size());
                responseData.put("rentals", rentals);
                responseData.put("message", rentals.size() + " rentals found for customer");
                
            } else if ("true".equals(activeRentals)) {
                // GET active rentals with full relationships
                List<Rental> rentals = rentalService.getAllActiveRentals();
                responseData.put("totalActiveRentals", rentals.size());
                responseData.put("rentals", rentals);
                responseData.put("message", rentals.size() + " active rentals found");
                
            } else if (rentalIdParam != null) {
                // GET single rental by ID with full relationships
                int rentalId = Integer.parseInt(rentalIdParam);
                Rental rental = rentalService.getRentalById(rentalId);
                responseData.put("rental", rental);
                responseData.put("message", "Rental retrieved successfully");
                
            } else if (storeIdParam != null) {
                // GET rentals by store with full relationships
                int storeId = Integer.parseInt(storeIdParam);
                List<Rental> rentals = rentalService.getRentalsByStore(storeId);
                responseData.put("storeId", storeId);
                responseData.put("totalRentals", rentals.size());
                responseData.put("rentals", rentals);
                responseData.put("message", rentals.size() + " rentals found for store");
                
            } else if ("true".equals(overdueParam)) {
                // GET overdue rentals with full relationships
                List<Rental> rentals = rentalService.getOverdueRentals();
                responseData.put("totalOverdueRentals", rentals.size());
                responseData.put("rentals", rentals);
                responseData.put("message", rentals.size() + " overdue rentals found");
                
            } else if ("true".equals(apiInfo)) {
                // ONLY show API info when explicitly requested
                Map<String, Object> result = rentalService.handleApiInformationQuery();
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
                
            } else {
                // DEFAULT: Get all rentals with full relationships (like CustomerController)
                List<Rental> rentals = rentalService.getAllRentals();
                responseData.put("totalRentals", rentals.size());
                responseData.put("rentals", rentals);
                responseData.put("message", "All rentals retrieved successfully");
            }
            
            //  SAFE SERIALIZATION - Jackson will now ignore circular references
            objectMapper.writeValue(response.getOutputStream(), responseData);
            
        } catch (SQLException e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
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
            String action = request.getParameter("action");
            String rentalIdParam = request.getParameter("rentalId");
            
            if ("return".equals(action) || rentalIdParam != null) {
                int rentalId = Integer.parseInt(rentalIdParam);
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                // Service returns Rental object
                Rental rental = rentalService.returnFilm(rentalId, requestData);
                
                //  SIMPLE RESPONSE FOR PUT (film return)
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("rentalId", rental.getRentalId());
                responseData.put("message", "Film returned successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "For film return, use ?action=return&rentalId={id} or ?rentalId={id}");
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
            String rentalIdParam = request.getParameter("rentalId");
            
            if (rentalIdParam != null) {
                int rentalId = Integer.parseInt(rentalIdParam);
                
                // Service returns Rental object
                Rental rental = rentalService.deleteRental(rentalId);
                
                //  SIMPLE RESPONSE FOR DELETE
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("rentalId", rentalId);
                responseData.put("message", "Rental deleted successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Rental ID is required");
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
        errorResponse.put("timestamp", java.time.Instant.now().toString());
        
        try {
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            // Fallback if even error serialization fails
            response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
        }
    }
}
