package repository;

import config.DBConnection;
import model.Currency;
import model.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileRepository implements Repository<Profile, Integer> {
    private static final String SQL_INSERT = "INSERT INTO profiles (name, default_currency, created_at) VALUES (?, ?::currency_type, ?)";
    private static final String SQL_FIND_ALL = "SELECT id, name, default_currency, created_at FROM profiles ORDER BY name";
    private static final String SQL_FIND_BY_ID = "SELECT id, name, default_currency, created_at FROM profiles WHERE id = ?";
    private static final String SQL_FIND_BY_NAME = "SELECT id, name, default_currency, created_at FROM profiles WHERE LOWER(name) = LOWER(?)";
    private static final String SQL_UPDATE = "UPDATE profiles SET name = ?, default_currency = ?::currency_type WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM profiles WHERE id = ?";

    @Override
    public Profile save(Profile profile) {
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getDefaultCurrency().name());
            stmt.setTimestamp(3, Timestamp.valueOf(profile.getCreatedAt()));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setId(generatedKeys.getInt(1));
                }
            }
            return profile;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save profile.", e);
        }
    }

    @Override
    public List<Profile> findAll() {
        List<Profile> profiles = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                profiles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load profiles.", e);
        }
        return profiles;
    }

    @Override
    public Optional<Profile> findById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load profile by id.", e);
        }
        return Optional.empty();
    }

    public Optional<Profile> findByName(String name) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load profile by name.", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Profile profile) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getDefaultCurrency().name());
            stmt.setInt(3, profile.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update profile.", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete profile.", e);
        }
    }

    private Profile mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        Currency defaultCurrency = Currency.valueOf(rs.getString("default_currency"));
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new Profile(id, name, defaultCurrency, createdAt);
    }
}