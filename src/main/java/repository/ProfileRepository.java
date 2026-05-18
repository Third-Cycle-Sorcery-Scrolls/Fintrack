package repository;

import config.DBConnection;
import model.Currency;
import model.Profile;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles all database operations for the Profile model.
 *
 * Layer rules:
 *   - SQL only — no validation, no business decisions.
 *   - Every method catches SQLException, prints it, and returns a safe fallback.
 *   - update() trusts that the caller (ProfileService) already called findById()
 *     and verified the profile exists before passing it here.
 */
public class ProfileRepository implements Repository<Profile, Integer> {

    // -------------------------------------------------------------------------
    // SQL constants
    // Defined once at the top so they are easy to find and update if the
    // database schema ever changes. Never build SQL by string concatenation —
    // always use PreparedStatements with ? placeholders to prevent SQL injection.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Helper — mapRow()
    // Converts one ResultSet row into a Profile object.
    // Called by every method that reads from the database, so the mapping
    // logic lives in exactly one place.
    // -------------------------------------------------------------------------

    private Profile mapRow(ResultSet rs) throws SQLException {
        Integer       id              = rs.getInt("id");
        String        name            = rs.getString("name");
        Currency      defaultCurrency = Currency.valueOf(rs.getString("default_currency"));
        LocalDateTime createdAt       = rs.getTimestamp("created_at").toLocalDateTime();

        return new Profile(id, name, defaultCurrency, createdAt);
    }

    // -------------------------------------------------------------------------
    // save()
    // Inserts a new profile row and returns the same Profile object with its
    // database-generated id filled in.
    // Returns null if the insert fails.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // findAll()
    // Returns every profile row as a list.
    // Returns an empty list (never null) if the query fails or table is empty.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // findById()
    // Returns the profile with the given id wrapped in Optional,
    // or Optional.empty() if not found or if the query fails.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // findByName()
    // Extra method beyond the base interface — used by ProfileService to check
    // for duplicate profile names before saving a new one.
    // Returns Optional.empty() if no profile with that name exists.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // update()
    // Updates an existing profile row using the id inside the Profile object.
    //
    // IMPORTANT: This method assumes the profile already exists in the database.
    // ProfileService is responsible for calling findById() first, modifying the
    // returned object, and only then passing it here. Never call update() on a
    // profile that has not been saved yet.
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // deleteById()
    // Deletes the profile row with the given id.
    // If the id does not exist, the query runs but affects 0 rows — no error.
    // -------------------------------------------------------------------------

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