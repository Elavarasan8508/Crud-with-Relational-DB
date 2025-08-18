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
            
            //  SIMPLE RESPONSE FOR POST (no complex rental object)
            Map<String, Object> result = rentalService.handleRentalCreation(requestData);
            
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
            String customerIdParam = request.getParameter("customerId");
            String activeRentals = request.getParameter("activeRentals");
            String rentalIdParam = request.getParameter("rentalId");
            String storeIdParam = request.getParameter("storeId");
            String overdueParam = request.getParameter("overdue");
            String apiInfo = request.getParameter("info"); //  NEW: Add this parameter
            
            Map<String, Object> result;
            
            if (customerIdParam != null) {
                int customerId = Integer.parseInt(customerIdParam);
                result = rentalService.handleCustomerHistoryQuery(customerId);
                
            } else if ("true".equals(activeRentals)) {
                result = rentalService.handleActiveRentalsQuery();
                
            } else if (rentalIdParam != null) {
                int rentalId = Integer.parseInt(rentalIdParam);
                result = rentalService.handleRentalQuery(rentalId);
                
            } else if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);
                result = rentalService.handleStoreRentalsQuery(storeId);
                
            } else if ("true".equals(overdueParam)) {
                result = rentalService.handleOverdueRentalsQuery();
                
            } else if ("true".equals(apiInfo)) {
                //  ONLY show API info when explicitly requested
                result = rentalService.handleApiInformationQuery();
                
            } else {
                //  DEFAULT: Show all rentals instead of API info
                result = rentalService.handleAllRentalsQuery();
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
            
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
                //  SIMPLE RESPONSE FOR PUT (film return)
                Map<String, Object> result = rentalService.handleFilmReturn(rentalIdParam, request);
                objectMapper.writeValue(response.getOutputStream(), result);
                
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
                
                //  SIMPLE RESPONSE FOR DELETE
                Map<String, Object> result = rentalService.handleRentalDeletion(rentalId);
                objectMapper.writeValue(response.getOutputStream(), result);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Rental ID is required");
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
        errorResponse.put("timestamp", java.time.Instant.now().toString());
        
        try {
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            // Fallback if even error serialization fails
            response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
        }
    }
}
