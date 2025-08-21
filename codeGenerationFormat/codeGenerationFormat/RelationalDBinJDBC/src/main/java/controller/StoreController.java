package controller;

import service.StoreManagementService;
import model.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import controller.CustomerController.*;
import controller.CustomerController.CountryMixin;
import controller.CustomerController.RentalMixin;
import controller.CustomerController.StaffMixin;
import controller.RentalController.FilmMixin;
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

        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }

    abstract class StoreMixin {
        @JsonIgnore abstract List<Customer> getCustomerList();
        @JsonIgnore abstract List<Inventory> getInventoryList();
        @JsonIgnore abstract List<Staff> getStaffList();
    }

    abstract class CustomerMixin {
        @JsonIgnore abstract List<Payment> getPaymentList();
        @JsonIgnore abstract List<Rental> getRentalList();
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);

            Store createdStore = storeService.createStore(requestData);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("storeId", createdStore.getStoreId());
            responseData.put("message", "Store created successfully");

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
            String storeIdParam = request.getParameter("storeId");
            String inventory = request.getParameter("inventory");
            String customers = request.getParameter("customers");
            String staff = request.getParameter("staff");
            String rentals = request.getParameter("rentals");
            String city = request.getParameter("city");

            if (storeIdParam != null) {
                int storeId = Integer.parseInt(storeIdParam);

                if ("true".equals(inventory)) {
                    List<Map<String, Object>> inventories = storeService.getStoreInventory(storeId);
                    objectMapper.writeValue(response.getOutputStream(), inventories);

                } else if ("true".equals(customers)) {
                    List<Customer> customerList = storeService.getStoreCustomers(storeId);
                    objectMapper.writeValue(response.getOutputStream(), customerList);

                } else if ("true".equals(staff)) {
                    List<Staff> staffList = storeService.getStoreStaff(storeId);
                    objectMapper.writeValue(response.getOutputStream(), staffList);

                } else if ("true".equals(rentals)) {
                    List<Rental> rentalList = storeService.getStoreRentals(storeId);
                    objectMapper.writeValue(response.getOutputStream(), rentalList);

                } else {
                    Store store = storeService.getStoreById(storeId);
                    objectMapper.writeValue(response.getOutputStream(), store);
                }

            } else if (city != null) {
                List<Store> stores = storeService.getStoresByCity(city);
                objectMapper.writeValue(response.getOutputStream(), stores);

            } else {
                List<Store> stores = storeService.getAllStores();
                objectMapper.writeValue(response.getOutputStream(), stores);
            }
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
                Store updatedStore = storeService.updateStore(storeId, requestData);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("storeId", updatedStore.getStoreId());
                responseData.put("message", "Store updated successfully");
                objectMapper.writeValue(response.getOutputStream(), responseData);

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
                Store deletedStore = storeService.deleteStore(storeId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("storeId", storeId);
                responseData.put("message", "Store deleted successfully");
                objectMapper.writeValue(response.getOutputStream(), responseData);

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
            response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
        }
    }
}
