package repository;

import config.DBConnection;
import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Category — plain SQL only, no business logic.
 *
 * Error handling: SQLException is caught, wrapped in RuntimeException, and thrown.
 * This lets the service layer know exactly what went wrong instead of silent failure.
 */
public class CategoryRepository implements Repository<Category, Integer> {

    // ── save ──────────────────────────────────────────────────────────────────

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (profile_id, name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, category.getProfileId());
            stmt.setString(2, category.getName());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                category.setId(keys.getInt(1));
            }
            return category;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category: " + e.getMessage(), e);
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

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
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all categories: " + e.getMessage(), e);
        }
    }

    // ── findAllByProfileId ────────────────────────────────────────────────────

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
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch categories for profile: " + e.getMessage(), e);
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT id, profile_id, name FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find category by id: " + e.getMessage(), e);
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
        }
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }

    // ── findByNameAndProfileId ────────────────────────────────────────────────

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
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check category name: " + e.getMessage(), e);
        }
    }

    // ── private helper ────────────────────────────────────────────────────────

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getInt("profile_id"),
            rs.getString("name")
        );
    }
}
