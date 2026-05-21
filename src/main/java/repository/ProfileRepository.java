package repository;

import config.DBConnection;
import model.Currency;
import model.Profile;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileRepository implements Repository<Profile, Integer> {

    // SQL constants

    private static final String SQL_INSERT =
            "INSERT INTO profiles (name, default_currency, created_at) VALUES (?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT id, name, default_currency, created_at FROM profiles";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, name, default_currency, created_at FROM profiles WHERE id = ?";

    private static final String SQL_FIND_BY_NAME =
            "SELECT id, name, default_currency, created_at FROM profiles WHERE name = ?";

    private static final String SQL_UPDATE =
            "UPDATE profiles SET name = ?, default_currency = ?, created_at = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM profiles WHERE id = ?";

    // Helper — mapRow()
    
    private Profile mapRow(ResultSet rs) throws SQLException {
        Integer       id              = rs.getInt("id");
        String        name            = rs.getString("name");
        Currency      defaultCurrency = Currency.valueOf(rs.getString("default_currency"));
        LocalDateTime createdAt       = rs.getTimestamp("created_at").toLocalDateTime();

        return new Profile(id, name, defaultCurrency, createdAt);
    }

  
    // save()
  
    @Override
    public Profile save(Profile profile) {
        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

            stmt.setString   (1, profile.getName());
            stmt.setString   (2, profile.getDefaultCurrency().name()); // stores "USD", "ETB", etc.
            stmt.setTimestamp(3, Timestamp.valueOf(profile.getCreatedAt()));

            stmt.executeUpdate();

            // Retrieve the auto-generated id assigned by the database
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                profile.setId(generatedKeys.getInt(1));
            }

            return profile;

        } catch (SQLException e) {
            System.out.println("ProfileRepository.save() failed:");
            e.printStackTrace();
            return null;
        }
    }

    // findAll()

    @Override
    public List<Profile> findAll() {
        List<Profile> profiles = new ArrayList<>();

        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
            ResultSet         rs   = stmt.executeQuery();

            while (rs.next()) {
                profiles.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("ProfileRepository.findAll() failed:");
            e.printStackTrace();
        }

        return profiles; // empty list if something went wrong
    }

    // findById()
    
    @Override
    public Optional<Profile> findById(Integer id) {
        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID);

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("ProfileRepository.findById() failed:");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // findByName()

    public Optional<Profile> findByName(String name) {
        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NAME);

            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("ProfileRepository.findByName() failed:");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // update()
    
    @Override
    public void update(Profile profile) {
        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE);

            stmt.setString   (1, profile.getName());
            stmt.setString   (2, profile.getDefaultCurrency().name());
            stmt.setTimestamp(3, Timestamp.valueOf(profile.getCreatedAt()));
            stmt.setInt      (4, profile.getId()); // WHERE id = ?

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("ProfileRepository.update() failed:");
            e.printStackTrace();
        }
    }

    // deleteById()
   
    @Override
    public void deleteById(Integer id) {
        try {
            Connection        conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_DELETE);

            stmt.setInt(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("ProfileRepository.deleteById() failed:");
            e.printStackTrace();
        }
    }
}