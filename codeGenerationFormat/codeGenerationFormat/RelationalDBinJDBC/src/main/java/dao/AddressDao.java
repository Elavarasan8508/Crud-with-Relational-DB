package dao;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {
    
    private static final String INSERT_SQL = 
        "INSERT INTO address (address, address2, district, city_id, postal_code, phone, last_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT address_id, address, address2, district, city_id, postal_code, phone, last_update " +
        "FROM address WHERE address_id = ?";
    
    private static final String FIND_ALL_SQL = 
        "SELECT address_id, address, address2, district, city_id, postal_code, phone, last_update " +
        "FROM address ORDER BY address_id";
    
    private static final String FIND_BY_CITY_ID_SQL = 
        "SELECT address_id, address, address2, district, city_id, postal_code, phone, last_update " +
        "FROM address WHERE city_id = ?";
    
    private static final String UPDATE_SQL = 
        "UPDATE address SET address = ?, address2 = ?, district = ?, city_id = ?, postal_code = ?, " +
        "phone = ?, last_update = ? WHERE address_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM address WHERE address_id = ?";
    
    public int insert(Connection connection, Address address) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, address.getAddress());
            statement.setString(2, address.getAddress2());
            statement.setString(3, address.getDistrict());
            
            // Extract city ID from City object
            if (address.getCity() != null && address.getCity().getCityId() > 0) {
                statement.setInt(4, address.getCity().getCityId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            
            statement.setString(5, address.getPostalCode());
            statement.setString(6, address.getPhone());
            
            // Use provided LocalDateTime or current time
            if (address.getLastUpdate() != null) {
                statement.setTimestamp(7, Timestamp.valueOf(address.getLastUpdate()));
            } else {
                statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            }
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating address failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int addressId = generatedKeys.getInt(1);
                    address.setAddressId(addressId);
                    return addressId;
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
    }
    
    public Address findById(Connection connection, int addressId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, addressId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractAddressFromResultSet(resultSet);
                }
                return null;
            }
        }
    }
    
    public List<Address> findAll(Connection connection) throws SQLException {
        List<Address> addresses = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                addresses.add(extractAddressFromResultSet(resultSet));
            }
        }
        
        return addresses;
    }
    
    public List<Address> findByCityId(Connection connection, int cityId) throws SQLException {
        List<Address> addresses = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CITY_ID_SQL)) {
            statement.setInt(1, cityId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    addresses.add(extractAddressFromResultSet(resultSet));
                }
            }
        }
        
        return addresses;
    }
    
    public void update(Connection connection, Address address) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, address.getAddress());
            statement.setString(2, address.getAddress2());
            statement.setString(3, address.getDistrict());
            
            // Extract city ID from City object
            if (address.getCity() != null && address.getCity().getCityId() > 0) {
                statement.setInt(4, address.getCity().getCityId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            
            statement.setString(5, address.getPostalCode());
            statement.setString(6, address.getPhone());
            
            // Use provided LocalDateTime or current time
            if (address.getLastUpdate() != null) {
                statement.setTimestamp(7, Timestamp.valueOf(address.getLastUpdate()));
            } else {
                statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            }
            
            statement.setInt(8, address.getAddressId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating address failed, no rows affected.");
            }
        }
    }
    
    public void deleteById(Connection connection, int addressId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, addressId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting address failed, no rows affected.");
            }
        }
    }
    
    private Address extractAddressFromResultSet(ResultSet resultSet) throws SQLException {
        Address address = new Address();
        address.setAddressId(resultSet.getInt("address_id"));
        address.setAddress(resultSet.getString("address"));
        address.setAddress2(resultSet.getString("address2"));
        address.setDistrict(resultSet.getString("district"));
        address.setPostalCode(resultSet.getString("postal_code"));
        address.setPhone(resultSet.getString("phone"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp lastUpdate = resultSet.getTimestamp("last_update");
        if (lastUpdate != null) {
            address.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        // Create placeholder City object with ID for service layer to load full object
        int cityId = resultSet.getInt("city_id");
        if (cityId > 0) {
            City tempCity = new City();
            tempCity.setCityId(cityId);
            address.setCity(tempCity);
        }
        
        return address;
    }
}
