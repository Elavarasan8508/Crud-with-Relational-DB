package service;

import dao.*;
import model.*;
import DataBaseConnection.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class FilmManagementService {
    
    private final FilmDao filmDao;
    private final ActorDao actorDao;
    private final CategoryDao categoryDao;
    private final LanguageDao languageDao;
    private final FilmActorDao filmActorDao;
    private final FilmCategoryDao filmCategoryDao;
    private final InventoryDao inventoryDao;
    private final RentalDao rentalDao;
    private final StoreDao storeDao;
    
    public FilmManagementService() {
        this.filmDao = new FilmDao();
        this.actorDao = new ActorDao();
        this.categoryDao = new CategoryDao();
        this.languageDao = new LanguageDao();
        this.filmActorDao = new FilmActorDao();
        this.filmCategoryDao = new FilmCategoryDao();
        this.inventoryDao = new InventoryDao();
        this.rentalDao = new RentalDao();
        this.storeDao = new StoreDao();
    }
    
  
    
    //  Handle film inventory query - GET /films/1/inventory
    public Map<String, Object> handleFilmInventoryQuery(int filmId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            try {
                // Validate film exists
                Film film = filmDao.findById(connection, filmId);
                if (film == null) {
                    throw new IllegalArgumentException("Film not found with ID: " + filmId);
                }
                
                // Get all inventory for this film
                List<Inventory> inventoryList = inventoryDao.findByFilmId(connection, filmId);
                
                // Group by store and count available/rented
                Map<Integer, Map<String, Object>> storeInventory = new HashMap<>();
                
                for (Inventory inventory : inventoryList) {
                    int storeId = inventory.getStore().getStoreId();
                    storeInventory.putIfAbsent(storeId, new HashMap<>());
                    
                    Map<String, Object> storeData = storeInventory.get(storeId);
                    storeData.putIfAbsent("storeId", storeId);
                    storeData.putIfAbsent("total", 0);
                    storeData.putIfAbsent("available", 0);
                    storeData.putIfAbsent("rented", 0);
                    
                    // Count total
                    storeData.put("total", (Integer) storeData.get("total") + 1);
                    
                    // Check if this inventory is currently rented
                    List<Rental> activeRentals = rentalDao.findByInventoryId(connection, inventory.getInventoryId())
                        .stream()
                        .filter(r -> r.getReturnDate() == null)
                        .collect(Collectors.toList());
                    
                    if (activeRentals.isEmpty()) {
                        storeData.put("available", (Integer) storeData.get("available") + 1);
                    } else {
                        storeData.put("rented", (Integer) storeData.get("rented") + 1);
                    }
                }
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("filmId", filmId);
                responseData.put("filmTitle", film.getTitle());
                responseData.put("totalCopies", inventoryList.size());
                responseData.put("storeInventory", storeInventory.values());
                responseData.put("message", "Inventory retrieved for film: " + film.getTitle());
                
                return responseData;
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve film inventory: " + e.getMessage(), e);
            }
        });
    }

    //  NEW: Handle film store inventory query - GET /films/1/inventory/3
    public Map<String, Object> handleFilmStoreInventoryQuery(int filmId, int storeId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            try {
                // Validate film exists
                Film film = filmDao.findById(connection, filmId);
                if (film == null) {
                    throw new IllegalArgumentException("Film not found with ID: " + filmId);
                }
                
                // Validate store exists
                Store store = storeDao.findById(connection, storeId);
                if (store == null) {
                    throw new IllegalArgumentException("Store not found with ID: " + storeId);
                }
                
                // Get inventory for this film at this store
                List<Inventory> inventoryList = inventoryDao.findByFilmId(connection, filmId)
                    .stream()
                    .filter(inv -> inv.getStore().getStoreId() == storeId)
                    .collect(Collectors.toList());
                
                List<Map<String, Object>> inventoryDetails = new ArrayList<>();
                int availableCount = 0;
                int rentedCount = 0;
                
                for (Inventory inventory : inventoryList) {
                    Map<String, Object> inventoryData = new HashMap<>();
                    inventoryData.put("inventoryId", inventory.getInventoryId());
                    inventoryData.put("lastUpdate", inventory.getLastUpdate().toString());
                    
                    // Check rental status
                    List<Rental> activeRentals = rentalDao.findByInventoryId(connection, inventory.getInventoryId())
                        .stream()
                        .filter(r -> r.getReturnDate() == null)
                        .collect(Collectors.toList());
                    
                    if (activeRentals.isEmpty()) {
                        inventoryData.put("status", "AVAILABLE");
                        availableCount++;
                    } else {
                        Rental rental = activeRentals.get(0);
                        inventoryData.put("status", "RENTED");
                        inventoryData.put("rentalId", rental.getRentalId());
                        inventoryData.put("customerId", rental.getCustomer().getCustomerId());
                        inventoryData.put("rentalDate", rental.getRentalDate().toString());
                        rentedCount++;
                    }
                    
                    inventoryDetails.add(inventoryData);
                }
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("filmId", filmId);
                responseData.put("filmTitle", film.getTitle());
                responseData.put("storeId", storeId);
                responseData.put("totalCopies", inventoryList.size());
                responseData.put("available", availableCount);
                responseData.put("rented", rentedCount);
                responseData.put("inventory", inventoryDetails);
                responseData.put("message", "Store inventory retrieved for film: " + film.getTitle());
                
                return responseData;
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve store inventory: " + e.getMessage(), e);
            }
        });
    }


    // Handle inventory creation
    public Map<String, Object> handleInventoryCreation(int filmId, int storeId, int quantity) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            try {
                // Validate film exists
                Film film = filmDao.findById(connection, filmId);
                if (film == null) {
                    throw new IllegalArgumentException("Film not found with ID: " + filmId);
                }
                
                // Validate store exists
                Store store = storeDao.findById(connection, storeId);
                if (store == null) {
                    throw new IllegalArgumentException("Store not found with ID: " + storeId);
                }
                
                List<Integer> inventoryIds = new ArrayList<>();
                
                // Create multiple inventory entries for the quantity
                for (int i = 0; i < quantity; i++) {
                    Inventory inventory = new Inventory();
                    inventory.setFilm(film);
                    inventory.setStore(store);
                    inventory.setLastUpdate(LocalDateTime.now());
                    
                    int inventoryId = inventoryDao.insert(connection, inventory);
                    inventoryIds.add(inventoryId);
                }
                
                // Response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("filmId", filmId);
                responseData.put("storeId", storeId);
                responseData.put("quantity", quantity);
                responseData.put("inventoryIds", inventoryIds);
                responseData.put("message", quantity + " copies of film added to store inventory");
                
                return responseData;
                
            } catch (Exception e) {
                throw new RuntimeException("Inventory creation failed: " + e.getMessage(), e);
            }
        });
    }

    //  Business Logic: Create Film - FIXED VERSION
    public Film createFilm(Map<String, Object> requestData) throws SQLException {
        try {
            return TransactionManager.executeInTransaction(connection -> {
                System.out.println("🎬 Starting film creation transaction...");
                
                try {
                    // Extract and validate data
                    Map<String, Object> filmData = (Map<String, Object>) requestData.get("film");
                    List<Map<String, Object>> actorsData = (List<Map<String, Object>>) requestData.get("actors");
                    List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) requestData.get("categories");
                    Map<String, Object> languageData = (Map<String, Object>) requestData.get("language");
                    
                    if (filmData == null) {
                        throw new IllegalArgumentException("Film data is required");
                    }
                    
                    Film film = mapToFilm(filmData);
                    List<Actor> actors = actorsData != null ? 
                        actorsData.stream().map(this::mapToActor).collect(Collectors.toList()) : 
                        new ArrayList<>();
                    List<Category> categories = categoriesData != null ? 
                        categoriesData.stream().map(this::mapToCategory).collect(Collectors.toList()) : 
                        new ArrayList<>();
                    Language language = languageData != null ? mapToLanguage(languageData) : null;
                    
                    // Create language if provided
                    if (language != null) {
                        //  CHECK IF LANGUAGE EXISTS FIRST
                        List<Language> existingLanguages = languageDao.findAll(connection)
                            .stream()
                            .filter(l -> l.getName().equalsIgnoreCase(language.getName()))
                            .collect(Collectors.toList());
                        
                        if (!existingLanguages.isEmpty()) {
                            film.setLanguage(existingLanguages.get(0));
                        } else {
                            language.setLastUpdate(LocalDateTime.now());
                            int languageId = languageDao.insert(connection, language);
                            language.setLanguageId(languageId);
                            film.setLanguage(language);
                        }
                    }
                    
                    film.setLastUpdate(LocalDateTime.now());
                    int filmId = filmDao.insert(connection, film);
                    film.setFilmId(filmId);
                    
                    //  ACTORS: Avoid duplicates
                    if (!actors.isEmpty()) {
                        for (Actor actor : actors) {
                            // Check for existing actor by name
                            List<Actor> existingActors = actorDao.findByName(connection, 
                                actor.getFirstName(), actor.getLastName());
                            
                            Actor actualActor;
                            if (!existingActors.isEmpty()) {
                                actualActor = existingActors.get(0);  // Reuse existing
                                System.out.println(" Reusing existing actor: " + actualActor.getFirstName() + " " + actualActor.getLastName());
                            } else {
                                actor.setLastUpdate(LocalDateTime.now());
                                int actorId = actorDao.insert(connection, actor);
                                actor.setActorId(actorId);
                                actualActor = actor;
                                System.out.println(" Created new actor: " + actualActor.getFirstName() + " " + actualActor.getLastName());
                            }
                            
                            // Insert relationship
                            FilmActor filmActor = new FilmActor();
                            filmActor.setFilm(film);
                            filmActor.setActor(actualActor);
                            filmActor.setLastUpdate(LocalDateTime.now());
                            filmActorDao.insert(connection, filmActor);
                        }
                    }
                    
                    //  CATEGORIES: Avoid duplicates
                    if (!categories.isEmpty()) {
                        List<Category> allCategories = categoryDao.findAll(connection);
                        for (Category category : categories) {
                            Category actualCategory = allCategories.stream()
                                .filter(c -> c.getName().equalsIgnoreCase(category.getName()))
                                .findFirst()
                                .orElse(null);
                            
                            if (actualCategory == null) {
                                category.setLastUpdate(LocalDateTime.now());
                                int categoryId = categoryDao.insert(connection, category);
                                category.setCategoryId(categoryId);
                                actualCategory = category;
                                System.out.println(" Created new category: " + actualCategory.getName());
                            } else {
                                System.out.println(" Reusing existing category: " + actualCategory.getName());
                            }
                            
                            FilmCategory filmCategory = new FilmCategory();
                            filmCategory.setFilm(film);
                            filmCategory.setCategory(actualCategory);
                            filmCategory.setLastUpdate(LocalDateTime.now());
                            filmCategoryDao.insert(connection, filmCategory);
                        }
                    }
                    
                    return film;
                    
                } catch (Exception e) {
                    throw new RuntimeException("Film creation failed", e);
                }
            });
        } catch (Exception e) {
            throw new SQLException("Failed to create film: " + e.getMessage(), e);
        }
    }

    // Business Logic: Get Film by ID - Returns Film with FULL relationships
    public Film getFilmById(int filmId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Film film = buildFilmWithRelationships(connection, filmId);
            
            if (film == null) {
                throw new IllegalArgumentException("Film not found with ID: " + filmId);
            }
            
            return film;
        });
    }
    
    // Business Logic: Get All Films - Returns List<Film> with FULL relationships
    public List<Film> getAllFilms() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Film> films = filmDao.findAll(connection);
            List<Film> filmsWithDetails = new ArrayList<>();
            
            for (Film film : films) {
                try {
                    Film filmWithDetails = buildFilmWithRelationships(connection, film.getFilmId());
                    if (filmWithDetails != null) {
                        filmsWithDetails.add(filmWithDetails);
                    }
                } catch (Exception e) {
                    // Set empty lists if relationship loading fails
                    film.setFilmActorList(new ArrayList<>());
                    film.setFilmCategoryList(new ArrayList<>());
                    film.setInventoryList(new ArrayList<>());
                    filmsWithDetails.add(film);
                }
            }
            
            return filmsWithDetails;
        });
    }
    
    // Business Logic: Get Films by Title - Returns List<Film> with FULL relationships
    public List<Film> getFilmsByTitle(String title) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Film> films = filmDao.findByTitle(connection, title);
            List<Film> filmsWithDetails = new ArrayList<>();
            
            for (Film film : films) {
                try {
                    Film filmWithDetails = buildFilmWithRelationships(connection, film.getFilmId());
                    if (filmWithDetails != null) {
                        filmsWithDetails.add(filmWithDetails);
                    }
                } catch (Exception e) {
                    film.setFilmActorList(new ArrayList<>());
                    film.setFilmCategoryList(new ArrayList<>());
                    film.setInventoryList(new ArrayList<>());
                    filmsWithDetails.add(film);
                }
            }
            
            return filmsWithDetails;
        });
    }
    
    // Business Logic: Get Films by Language - Returns List<Film> with FULL relationships
    public List<Film> getFilmsByLanguage(int languageId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            List<Film> films = filmDao.findByLanguageId(connection, languageId);
            List<Film> filmsWithDetails = new ArrayList<>();
            
            for (Film film : films) {
                try {
                    Film filmWithDetails = buildFilmWithRelationships(connection, film.getFilmId());
                    if (filmWithDetails != null) {
                        filmsWithDetails.add(filmWithDetails);
                    }
                } catch (Exception e) {
                    film.setFilmActorList(new ArrayList<>());
                    film.setFilmCategoryList(new ArrayList<>());
                    film.setInventoryList(new ArrayList<>());
                    filmsWithDetails.add(film);
                }
            }
            
            return filmsWithDetails;
        });
    }
    
    // Business Logic: Get Films by Actor - Returns List<Film> with FULL relationships
    public List<Film> getFilmsByActor(int actorId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            // Get film IDs that this actor appears in
            List<FilmActor> filmActors = filmActorDao.findByActorId(connection, actorId);
            List<Film> filmsWithDetails = new ArrayList<>();
            
            for (FilmActor filmActor : filmActors) {
                try {
                    Film filmWithDetails = buildFilmWithRelationships(connection, filmActor.getFilm().getFilmId());
                    if (filmWithDetails != null) {
                        filmsWithDetails.add(filmWithDetails);
                    }
                } catch (Exception e) {
                    // Add basic film if relationship loading fails
                    Film basicFilm = filmDao.findById(connection, filmActor.getFilm().getFilmId());
                    if (basicFilm != null) {
                        basicFilm.setFilmActorList(new ArrayList<>());
                        basicFilm.setFilmCategoryList(new ArrayList<>());
                        basicFilm.setInventoryList(new ArrayList<>());
                        filmsWithDetails.add(basicFilm);
                    }
                }
            }
            
            return filmsWithDetails;
        });
    }
    
    // Business Logic: Get Films by Category - Returns List<Film> with FULL relationships
    public List<Film> getFilmsByCategory(int categoryId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            // Get film IDs in this category
            List<FilmCategory> filmCategories = filmCategoryDao.findByCategoryId(connection, categoryId);
            List<Film> filmsWithDetails = new ArrayList<>();
            
            for (FilmCategory filmCategory : filmCategories) {
                try {
                    Film filmWithDetails = buildFilmWithRelationships(connection, filmCategory.getFilm().getFilmId());
                    if (filmWithDetails != null) {
                        filmsWithDetails.add(filmWithDetails);
                    }
                } catch (Exception e) {
                    // Add basic film if relationship loading fails
                    Film basicFilm = filmDao.findById(connection, filmCategory.getFilm().getFilmId());
                    if (basicFilm != null) {
                        basicFilm.setFilmActorList(new ArrayList<>());
                        basicFilm.setFilmCategoryList(new ArrayList<>());
                        basicFilm.setInventoryList(new ArrayList<>());
                        filmsWithDetails.add(basicFilm);
                    }
                }
            }
            
            return filmsWithDetails;
        });
    }
    
    // Business Logic: Update Film - Returns Film object (no relationships)
    public Film updateFilm(int filmId, Map<String, Object> requestData) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Film existingFilm = filmDao.findById(connection, filmId);
            if (existingFilm == null) {
                throw new IllegalArgumentException("Film not found with ID: " + filmId);
            }
            
            Map<String, Object> filmData = (Map<String, Object>) requestData.get("film");
            Film updatedFilm = mapToFilm(filmData);
            updatedFilm.setFilmId(filmId);
            updatedFilm.setLastUpdate(LocalDateTime.now());
            
            // Preserve existing relationships if not being updated
            if (updatedFilm.getLanguage() == null) {
                updatedFilm.setLanguage(existingFilm.getLanguage());
            }
            
            filmDao.update(connection, updatedFilm);
            
            // Return simple film object (no relationships loaded)
            updatedFilm.setTitle(filmData.get("title") != null ? (String) filmData.get("title") : existingFilm.getTitle());
            return updatedFilm;
        });
    }
    
    // Business Logic: Delete Film - Returns Film object (no relationships)
    public Film deleteFilm(int filmId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Film film = filmDao.findById(connection, filmId);
            if (film == null) {
                throw new IllegalArgumentException("Film not found with ID: " + filmId);
            }
            
            // Business rule: Check for active rentals
            List<Inventory> inventories = inventoryDao.findByFilmId(connection, filmId);
            for (Inventory inventory : inventories) {
                List<Rental> activeRentals = rentalDao.findByInventoryId(connection, inventory.getInventoryId())
                    .stream()
                    .filter(r -> r.getReturnDate() == null)
                    .collect(Collectors.toList());
                
                if (!activeRentals.isEmpty()) {
                    throw new IllegalStateException("Cannot delete film with active rentals");
                }
            }
            
            // Delete relationships first
            List<FilmActor> filmActors = filmActorDao.findByFilmId(connection, filmId);
            for (FilmActor fa : filmActors) {
                filmActorDao.delete(connection, fa.getActor().getActorId(), filmId);
            }
            
            List<FilmCategory> filmCategories = filmCategoryDao.findByFilmId(connection, filmId);
            for (FilmCategory fc : filmCategories) {
                filmCategoryDao.delete(connection, filmId, fc.getCategory().getCategoryId());
            }
            
            for (Inventory inventory : inventories) {
                inventoryDao.deleteById(connection, inventory.getInventoryId());
            }
            
            filmDao.deleteById(connection, filmId);
            
            // Return simple film object (no relationships)
            return film;
        });
    }
    
    //  Controller Support Methods - POST/PUT/DELETE return simple messages
    public Map<String, Object> handleFilmCreation(Map<String, Object> requestData) throws SQLException {
        try {
            Film createdFilm = createFilm(requestData);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("filmId", createdFilm.getFilmId());
            responseData.put("message", "Film created successfully");
            
            return responseData;
            
        } catch (Exception e) {
            throw new SQLException("Film creation failed: " + e.getMessage(), e);
        }
    }
    
    //  GET methods return full film objects with relationships
    public Map<String, Object> handleFilmQuery(String filmIdParam, String titleParam, String languageIdParam, String actorIdParam, String categoryIdParam) throws SQLException {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        
        if (filmIdParam != null) {
            // Get single film by ID with full relationships
            int filmId = Integer.parseInt(filmIdParam);
            Film film = getFilmById(filmId);
            responseData.put("film", film);
            responseData.put("message", "Film retrieved successfully");
            
        } else if (titleParam != null) {
            // Get films by title with full relationships
            List<Film> films = getFilmsByTitle(titleParam);
            responseData.put("films", films);
            responseData.put("totalResults", films.size());
            responseData.put("searchTerm", titleParam);
            responseData.put("message", films.size() + " films found by title");
            
        } else if (languageIdParam != null) {
            // Get films by language with full relationships
            int languageId = Integer.parseInt(languageIdParam);
            List<Film> films = getFilmsByLanguage(languageId);
            responseData.put("films", films);
            responseData.put("totalResults", films.size());
            responseData.put("languageId", languageId);
            responseData.put("message", films.size() + " films found for language");
            
        } else if (actorIdParam != null) {
            // Get films by actor with full relationships
            int actorId = Integer.parseInt(actorIdParam);
            List<Film> films = getFilmsByActor(actorId);
            responseData.put("films", films);
            responseData.put("totalResults", films.size());
            responseData.put("actorId", actorId);
            responseData.put("message", films.size() + " films found for actor");
            
        } else if (categoryIdParam != null) {
            // Get films by category with full relationships
            int categoryId = Integer.parseInt(categoryIdParam);
            List<Film> films = getFilmsByCategory(categoryId);
            responseData.put("films", films);
            responseData.put("totalResults", films.size());
            responseData.put("categoryId", categoryId);
            responseData.put("message", films.size() + " films found for category");
            
        } else {
            // Get all films with full relationships
            List<Film> films = getAllFilms();
            responseData.put("films", films);
            responseData.put("totalFilms", films.size());
            responseData.put("message", "All films retrieved successfully");
        }
        
        return responseData;
    }
    
    public Map<String, Object> handleFilmUpdate(int filmId, Map<String, Object> requestData) throws SQLException {
        Film film = updateFilm(filmId, requestData);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("filmId", film.getFilmId());
        responseData.put("message", "Film updated successfully");
        
        return responseData;
    }
    
    public Map<String, Object> handleFilmDeletion(int filmId) throws SQLException {
        Film film = deleteFilm(filmId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("filmId", filmId);
        responseData.put("message", "Film deleted successfully");
        
        return responseData;
    }
    
    /**
     * Loads a film together with language, actors, categories and inventory
     * using the same JDBC connection and returns a fully-hydrated Film object.
     */
    private Film buildFilmWithRelationships(Connection connection, int filmId) throws SQLException {

        Film film = filmDao.findById(connection, filmId);
        if (film == null) {
            return null;
        }

        try {
            /* -------- language & original language -------- */
            if (film.getLanguage() != null) {
                Language lang = languageDao.findById(connection,
                                                     film.getLanguage().getLanguageId());
                if (lang != null) film.setLanguage(lang);
            }

            if (film.getOriginalLanguage() != null) {
                Language oLang = languageDao.findById(connection,
                                                      film.getOriginalLanguage().getLanguageId());
                if (oLang != null) film.setOriginalLanguage(oLang);
            }

            /* -------- actors -------- */
            List<FilmActor> filmActors = filmActorDao.findByFilmId(connection, filmId);
            for (FilmActor fa : filmActors) {
                int actorId = fa.getActor() != null ? fa.getActor().getActorId() : 0;
                if (actorId > 0) {
                    Actor actor = actorDao.findById(connection, actorId);
                    if (actor != null) fa.setActor(actor);       // hydrate actor
                }
                fa.setFilm(film);                                // set back-reference
            }
            film.setFilmActorList(filmActors != null ? filmActors : new ArrayList<>());

            /* -------- categories -------- */
            List<FilmCategory> filmCategories = filmCategoryDao.findByFilmId(connection, filmId);
            for (FilmCategory fc : filmCategories) {
                int catId = fc.getCategory() != null ? fc.getCategory().getCategoryId() : 0;
                if (catId > 0) {
                    Category cat = categoryDao.findById(connection, catId);
                    if (cat != null) fc.setCategory(cat);        // hydrate category
                }
                fc.setFilm(film);                                // set back-reference
            }
            film.setFilmCategoryList(filmCategories != null ? filmCategories : new ArrayList<>());

            /* -------- inventory -------- */
            List<Inventory> inventory = inventoryDao.findByFilmId(connection, filmId);
            film.setInventoryList(inventory != null ? inventory : new ArrayList<>());

        } catch (Exception ex) {
            System.err.println("⚠️  Error loading relationships for film "
                               + filmId + ": " + ex.getMessage());
            // keep film but return with empty lists to avoid NPEs
            film.setFilmActorList(new ArrayList<>());
            film.setFilmCategoryList(new ArrayList<>());
            film.setInventoryList(new ArrayList<>());
        }

        return film;
    }
    
    // Helper mapping methods
    private Film mapToFilm(Map<String, Object> data) {
        Film film = new Film();
        
        film.setTitle((String) data.get("title"));
        film.setDescription((String) data.get("description"));
        
        if (data.get("releaseYear") != null) {
            film.setReleaseYear(((Number) data.get("releaseYear")).intValue());
        }
        
        if (data.get("rentalDuration") != null) {
            film.setRentalDuration(((Number) data.get("rentalDuration")).intValue());
        }
        
        if (data.get("length") != null) {
            film.setLength(((Number) data.get("length")).intValue());
        }
        
        if (data.get("rentalRate") != null) {
            film.setRentalRate(new BigDecimal(data.get("rentalRate").toString()));
        }
        
        if (data.get("replacementCost") != null) {
            film.setReplacementCost(new BigDecimal(data.get("replacementCost").toString()));
        }
        
        film.setRating((String) data.get("rating"));
        film.setSpecialFeatures((String) data.get("specialFeatures"));
        
        return film;
    }
    
    private Actor mapToActor(Map<String, Object> data) {
        Actor actor = new Actor();
        actor.setFirstName((String) data.get("firstName"));
        actor.setLastName((String) data.get("lastName"));
        return actor;
    }
    
    private Category mapToCategory(Map<String, Object> data) {
        Category category = new Category();
        category.setName((String) data.get("name"));
        return category;
    }
    
    private Language mapToLanguage(Map<String, Object> data) {
        Language language = new Language();
        language.setName((String) data.get("name"));
        return language;
    }
}
