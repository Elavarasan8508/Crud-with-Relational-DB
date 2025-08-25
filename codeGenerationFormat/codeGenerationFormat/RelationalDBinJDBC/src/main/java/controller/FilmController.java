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
            String pathInfo = request.getPathInfo(); // Gets /10/inventory/2 from URL
            
            // Check for inventory creation: /filmId/inventory/storeId OR /filmId/inventory
            if (pathInfo != null) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 4 && "inventory".equals(parts[2])) {
                    // Case: /films/10/inventory/2 (storeId in URL)
                    handleInventoryCreationWithStoreInURL(pathInfo, request, response);
                    return;
                } else if (parts.length == 3 && "inventory".equals(parts[2])) {
                    // Case: /films/10/inventory (storeId in JSON body)
                    handleInventoryCreation(pathInfo, request, response);
                    return;
                }
            }
            
            // Handle regular film creation: POST /films
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            Film film = filmService.createFilm(requestData);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("filmId", film.getFilmId());
            responseData.put("message", "Film created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), responseData);
            
        } catch (SQLException | IllegalArgumentException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    // For URL like: POST /films/10/inventory/2 with body { "quantity": 5 }
    private void handleInventoryCreationWithStoreInURL(String pathInfo, HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 4 || !"inventory".equals(pathParts[2])) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format. Use /films/{filmId}/inventory/{storeId}");
            return;
        }
        
        try {
            int filmId = Integer.parseInt(pathParts[1]);
            int storeId = Integer.parseInt(pathParts[3]);
            
            // Read only quantity from JSON body
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
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Error processing request: " + e.getMessage());
        }
    }

    // For URL like: POST /films/10/inventory with body { "storeId": 2, "quantity": 5 }
    private void handleInventoryCreation(String pathInfo, HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format. Use /films/{filmId}/inventory");
            return;
        }
        
        try {
            int filmId = Integer.parseInt(pathParts[1]); // Get filmId from URL
            
            // Read storeId and quantity from JSON body
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            
            Integer storeId = (Integer) requestData.get("storeId");
            Integer quantity = (Integer) requestData.get("quantity");
            
            if (storeId == null || storeId <= 0) {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "StoreId must be a positive number");
                return;
            }
            
            if (quantity == null || quantity <= 0) {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Quantity must be a positive number");
                return;
            }
            
            // Delegate to service
            Map<String, Object> result = filmService.handleInventoryCreation(filmId, storeId, quantity);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (NumberFormatException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid film ID");
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Error processing request: " + e.getMessage());
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
            String storeIdParam = request.getParameter("storeId");
            String overdueParam = request.getParameter("overdue");

            //  Film query parameters
            String filmIdParam = request.getParameter("filmId");
            String titleParam = request.getParameter("title");
            String languageIdParam = request.getParameter("languageId");
            String actorIdParam = request.getParameter("actorId");
            String categoryIdParam = request.getParameter("categoryId");

            //  Check for inventory in URL path
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/inventory")) {
                handleInventoryQuery(pathInfo, response);

            } else if (customerIdParam != null) {
                int customerId = Integer.parseInt(customerIdParam);
                List<Rental> rentals = rentalService.getCustomerRentals(customerId);
                objectMapper.writeValue(response.getOutputStream(), rentals);

            } else if ("true".equals(activeRentals)) {
                List<Rental> rentals = rentalService.getAllActiveRentals();
                objectMapper.writeValue(response.getOutputStream(), rentals);

            } else if (rentalIdParam != null) {
                int rentalId = Integer.parseInt(rentalIdParam);
                Rental rental = rentalService.getRentalById(rentalId);
                objectMapper.writeValue(response.getOutputStream(), rental);

            } else if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);
                List<Rental> rentals = rentalService.getRentalsByStore(storeId);
                objectMapper.writeValue(response.getOutputStream(), rentals);

            } else if ("true".equals(overdueParam)) {
                List<Rental> rentals = rentalService.getOverdueRentals();
                objectMapper.writeValue(response.getOutputStream(), rentals);

            } else {
                if (filmIdParam != null) {
                    int filmId = Integer.parseInt(filmIdParam);
                    Film film = filmService.getFilmById(filmId);
                    objectMapper.writeValue(response.getOutputStream(), film);

                } else if (languageIdParam != null) {
                    int languageId = Integer.parseInt(languageIdParam);
                    List<Film> films = filmService.getFilmsByLanguage(languageId);
                    objectMapper.writeValue(response.getOutputStream(), films);

                } else if (actorIdParam != null) {
                    int actorId = Integer.parseInt(actorIdParam);
                    List<Film> films = filmService.getFilmsByActor(actorId);
                    objectMapper.writeValue(response.getOutputStream(), films);

                } else if (categoryIdParam != null) {
                    int categoryId = Integer.parseInt(categoryIdParam);
                    List<Film> films = filmService.getFilmsByCategory(categoryId);
                    objectMapper.writeValue(response.getOutputStream(), films);

                } else {
                    List<Film> films = filmService.getAllFilms();
                    objectMapper.writeValue(response.getOutputStream(), films);
                }
            }

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


    //  Handle inventory queries - returns List instead of Map
    private void handleInventoryQuery(String pathInfo, HttpServletResponse response) throws SQLException, IOException {
        String[] pathParts = pathInfo.split("/");
        
        if (pathParts.length >= 3) {
            int filmId = Integer.parseInt(pathParts[1]);
            
            if (pathParts.length == 3) {
                // GET /films/1/inventory - get all inventory for this film as List
                List<Map<String, Object>> inventoryList = filmService.handleFilmInventoryQuery(filmId);
                objectMapper.writeValue(response.getOutputStream(), inventoryList);
            } else if (pathParts.length >= 4) {
                // GET /films/1/inventory/3 - get inventory for this film at specific store as List
                int storeId = Integer.parseInt(pathParts[3]);
                List<Map<String, Object>> inventoryList = filmService.handleFilmStoreInventoryQuery(filmId, storeId);
                objectMapper.writeValue(response.getOutputStream(), inventoryList);
            }
        } else {
            throw new IllegalArgumentException("Invalid inventory query URL");
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String filmIdParam = request.getParameter("filmId");
            
            if (filmIdParam != null) {
                int filmId = Integer.parseInt(filmIdParam);
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                Film film = filmService.updateFilm(filmId, requestData);
                
                //  SIMPLE RESPONSE FOR PUT (no complex object)
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("filmId", film.getFilmId());
                responseData.put("message", "Film updated successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Film ID is required");
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
            String filmIdParam = request.getParameter("filmId");
            
            if (filmIdParam != null) {
                int filmId = Integer.parseInt(filmIdParam);
                
                // Delegate to service
                Film film = filmService.deleteFilm(filmId);
                
                //  SIMPLE RESPONSE FOR DELETE (no complex object)
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("filmId", filmId);
                responseData.put("message", "Film deleted successfully");
                
                objectMapper.writeValue(response.getOutputStream(), responseData);
                
            } else {
                handleError(response, HttpServletResponse.SC_BAD_REQUEST, "Film ID is required");
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