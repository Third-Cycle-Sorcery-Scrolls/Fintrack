package repository;

import model.Category;
import config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Category.
 *
 * Rules:
 *  - Implements Repository<Category, Integer>   (T = Category, ID = Integer)
 *  - Plain SQL only — zero business logic here
 *  - Handle every SQLException: print it, return safe fallback
 *  - Naming: save() / findAll() / findById() / update() / deleteById()
 *
 * DB table assumed (tell Integration teammate):
 *   CREATE TABLE categories (
 *       id         INTEGER PRIMARY KEY AUTO_INCREMENT,
 *       profile_id INTEGER NOT NULL,
 *       name       VARCHAR(100) NOT NULL,
 *       UNIQUE (profile_id, name),           -- uniqueness constraint
 *       FOREIGN KEY (profile_id) REFERENCES profiles(id)
 *   );
 */
public class CategoryRepository implements Repository<Category, Integer> {

    // ── save ──────────────────────────────────────────────────────────────────

    /**
     * Inserts a new category row.
     * Returns the saved Category with the database-generated id filled in.
     * Returns null if the insert fails (e.g. duplicate name in same profile).
     */
    @Override
    public Category save(Category category) {
        // We use RETURN_GENERATED_KEYS so we can fill in the auto-increment id
        String sql = "INSERT INTO categories (profile_id, name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, category.getProfileId());
            stmt.setString(2, category.getName());
            stmt.executeUpdate();

            // Grab the auto-generated id and set it on our object
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                category.setId(keys.getInt(1));
            }

            return category;

        } catch (SQLException e) {
            System.out.println("CategoryRepository.save() error: " + e.getMessage());
            return null;
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    /**
     * Returns ALL categories across every profile.
     * Typically you won't call this directly — use findAllByProfileId() instead.
     */
    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, profile_id, name FROM categories";
        List<Category> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("CategoryRepository.findAll() error: " + e.getMessage());
        }

        return list;
    }

    /**
     * Returns all categories that belong to a specific profile.
     * This is the method you'll actually use most of the time.
     */
    public List<Category> findAllByProfileId(int profileId) {
        String sql = "SELECT id, profile_id, name FROM categories WHERE profile_id = ?";
        List<Category> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("CategoryRepository.findAllByProfileId() error: " + e.getMessage());
        }

        return list;
    }

    // ── findById ──────────────────────────────────────────────────────────────

    /**
     * Finds one category by its primary key id.
     * Returns Optional.empty() if not found.
     * Use .isPresent() or .orElse(null) in the service to unwrap.
     */
    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT id, profile_id, name FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));  // found — wrap in Optional
            }

        } catch (SQLException e) {
            System.out.println("CategoryRepository.findById() error: " + e.getMessage());
        }

        return Optional.empty(); // not found
    }

    // ── update ────────────────────────────────────────────────────────────────

    /**
     * Updates a category that already exists in the database.
     * Returns void — matches the Repository interface.
     * Service layer calls findById() first before calling this.
     */
    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("CategoryRepository.update() error: " + e.getMessage());
        }
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    /**
     * Deletes the category row with the given id.
     * Returns void — matches the Repository interface.
     * NOTE: If transactions reference this category, the DB foreign key
     * will block deletion and an SQLException will be printed.
     */
    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("CategoryRepository.deleteById() error: " + e.getMessage());
        }
    }

    // ── findByNameAndProfileId ────────────────────────────────────────────────

    /**
     * Looks up a category by (profileId + name).
     * Used by the service layer to enforce the unique-name-per-profile rule.
     * Returns null if no match.
     */
    public Category findByNameAndProfileId(String name, int profileId) {
        String sql = "SELECT id, profile_id, name FROM categories " +
                     "WHERE profile_id = ? AND LOWER(name) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profileId);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.out.println("CategoryRepository.findByNameAndProfileId() error: " + e.getMessage());
        }

        return null;
    }

    // ── private helper ────────────────────────────────────────────────────────

    /**
     * Converts one ResultSet row into a Category object.
     * Keeps column-name mapping in one place — easier to fix if DB changes.
     */
    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getInt("profile_id"),
            rs.getString("name")
        );
    }
}
