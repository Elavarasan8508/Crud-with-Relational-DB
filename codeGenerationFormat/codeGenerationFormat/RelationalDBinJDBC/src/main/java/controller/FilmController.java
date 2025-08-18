package controller;

import service.FilmManagementService;
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

public class FilmController extends HttpServlet {
    
    private final FilmManagementService filmService;
    private final ObjectMapper objectMapper;
    private final VideoRentalService rentalService;
    
    public FilmController() {
        this.filmService = new FilmManagementService();
        this.objectMapper = new ObjectMapper();
        this.rentalService = new VideoRentalService();
        
        //  Configure Jackson for LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        //  Configure to ignore null values
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        //  PREVENT INFINITE RECURSION - Add mix-ins to ignore back references
        this.objectMapper.addMixIn(FilmActor.class, FilmActorMixin.class);
        this.objectMapper.addMixIn(FilmCategory.class, FilmCategoryMixin.class);
        this.objectMapper.addMixIn(Inventory.class, InventoryMixin.class);
        
        //  Configure Jackson to handle circular references
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }
    
    //  Mix-in classes to ignore circular references WITHOUT modifying POJOs
    abstract class FilmActorMixin {
  
          @JsonIgnore abstract Film getFilm();
        @JsonIgnore abstract void setFilm(Film film);
    }
    abstract class FilmCategoryMixin {
        @JsonIgnore abstract Film getFilm();
        @JsonIgnore abstract void setFilm(Film film);
    }
    
    abstract class InventoryMixin {
        @JsonIgnore abstract List<Rental> getRentalList(); // Ignore rental list if it exists
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo(); // Gets /1/inventory/3 from URL
            
            if (pathInfo != null && pathInfo.contains("/inventory/")) {
                // Handle inventory creation: POST /films/1/inventory/3
                handleInventoryCreation(pathInfo, request, response);
            } else {
                // Handle regular film creation: POST /films
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                Map<String, Object> result = filmService.handleFilmCreation(requestData);
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getOutputStream(), result);
            }
            
        } catch (SQLException | IllegalArgumentException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    //  NEW: Handle inventory creation
    private void handleInventoryCreation(String pathInfo, HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        // Parse URL: /1/inventory/3 -> filmId=1, storeId=3
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 4) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format. Use /films/{filmId}/inventory/{storeId}");
            return;
        }
        
        try {
            int filmId = Integer.parseInt(pathParts[1]);        // /films/1/inventory/3
            int storeId = Integer.parseInt(pathParts[1]);       // /films/1/inventory/3
            
            // Read quantity from JSON body
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            Integer quantity = (Integer) requestData.get("quantity");
            
            if (quantity == null || quantity <= 0) {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Quantity must be a positive number");
                return;
            }
            
            // Delegate to service
            Map<String, Object> result = filmService.handleInventoryCreation(filmId, storeId, quantity);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (NumberFormatException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid film ID or store ID");
        }
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Extract all possible query parameters
            String customerIdParam = request.getParameter("customerId");
            String activeRentals = request.getParameter("activeRentals");
            String rentalIdParam = request.getParameter("rentalId");
            
            //  Film query parameters
            String filmIdParam = request.getParameter("filmId");
            String titleParam = request.getParameter("title");
            String languageIdParam = request.getParameter("languageId");
            String actorIdParam = request.getParameter("actorId");        
            String categoryIdParam = request.getParameter("categoryId");  
            
            //  Check for inventory in URL path
            String pathInfo = request.getPathInfo();
            
            Map<String, Object> result;
            
            if (pathInfo != null && pathInfo.contains("/inventory")) {
                //  Handle inventory queries: GET /films/1/inventory or GET /films/1/inventory/3
                result = handleInventoryQuery(pathInfo);
                
            } else if (customerIdParam != null) {
                // Delegate customer rental history query to service
                int customerId = Integer.parseInt(customerIdParam);
                result = rentalService.handleCustomerHistoryQuery(customerId);
                
            } else if ("true".equals(activeRentals)) {
                // Delegate active rentals query to service
                result = rentalService.handleActiveRentalsQuery();
                
            } else if (rentalIdParam != null) {
                // Delegate specific rental query to service
                int rentalId = Integer.parseInt(rentalIdParam);
                result = rentalService.handleRentalQuery(rentalId);
                
            } else if (filmIdParam != null || titleParam != null || languageIdParam != null || 
                       actorIdParam != null || categoryIdParam != null) {
                //  Pass all 5 parameters to film service
                result = filmService.handleFilmQuery(filmIdParam, titleParam, languageIdParam, actorIdParam, categoryIdParam);
                
            } else {
                //  Default case - get all films
                result = filmService.handleFilmQuery(null, null, null, null, null);
            }
            
            //  SAFE SERIALIZATION - Jackson will now ignore circular references
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

    //  Handle inventory queries
    private Map<String, Object> handleInventoryQuery(String pathInfo) throws SQLException {
        String[] pathParts = pathInfo.split("/");
        
        if (pathParts.length >= 3) {
            int filmId = Integer.parseInt(pathParts[1]);
            
            if (pathParts.length == 3) {
                // GET /films/1/inventory - get all inventory for this film
                return filmService.handleFilmInventoryQuery(filmId);
            } else if (pathParts.length >= 4) {
                // GET /films/1/inventory/3 - get inventory for this film at specific store
                int storeId = Integer.parseInt(pathParts[3]);
                return filmService.handleFilmStoreInventoryQuery(filmId, storeId);
            }
        }
        
        throw new IllegalArgumentException("Invalid inventory query URL");
    }



    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String filmIdParam = request.getParameter("filmId");
            
            if (filmIdParam != null) {
                int filmId = Integer.parseInt(filmIdParam);
                
                // Delegate to service
                Map<String, Object> result = filmService.handleFilmDeletion(filmId);
                objectMapper.writeValue(response.getOutputStream(), result);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Film ID is required");
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
