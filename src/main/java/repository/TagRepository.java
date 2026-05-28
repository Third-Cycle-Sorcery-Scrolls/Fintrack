package repository;

import config.DBConnection;
import model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag — plain SQL only, no business logic.
 */
public class TagRepository implements Repository<Tag, Integer> {

    // ── save ──────────────────────────────────────────────────────────────────

    @Override
    public Tag save(Tag tag) {
        String sql = "INSERT INTO tags (profile_id, name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, tag.getProfileId());
            stmt.setString(2, tag.getName());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                tag.setId(keys.getInt(1));
            }
            return tag;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save tag: " + e.getMessage(), e);
        }
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Override
    public List<Tag> findAll() {
        String sql = "SELECT id, profile_id, name, created_at FROM tags";
        List<Tag> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all tags: " + e.getMessage(), e);
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Override
    public Optional<Tag> findById(Integer id) {
        String sql = "SELECT id, profile_id, name, created_at FROM tags WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tag by id: " + e.getMessage(), e);
        }
    }

    // ── findAllByProfile ──────────────────────────────────────────────────────

    public List<Tag> findAllByProfile(int profileId) {
        String sql = "SELECT id, profile_id, name, created_at FROM tags WHERE profile_id = ? ORDER BY name";
        List<Tag> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tags for profile: " + e.getMessage(), e);
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Override
    public void update(Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag.getName());
            stmt.setInt(2, tag.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update tag: " + e.getMessage(), e);
        }
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM tags WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tag: " + e.getMessage(), e);
        }
    }

    // ── findByNameAndProfile ──────────────────────────────────────────────────

    public Optional<Tag> findByNameAndProfile(String name, int profileId) {
        String sql = "SELECT id, profile_id, name, created_at FROM tags " +
                     "WHERE profile_id = ? AND LOWER(name) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profileId);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check tag name: " + e.getMessage(), e);
        }
    }

    // ── assignTagToTransaction ────────────────────────────────────────────────

    public void assignTagToTransaction(int transactionId, int tagId) {
        String sql = "INSERT INTO transaction_tags (transaction_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign tag to transaction: " + e.getMessage(), e);
        }
    }

    // ── removeTagFromTransaction ──────────────────────────────────────────────

    public void removeTagFromTransaction(int transactionId, int tagId) {
        String sql = "DELETE FROM transaction_tags WHERE transaction_id = ? AND tag_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove tag from transaction: " + e.getMessage(), e);
        }
    }

    // ── findTagsByTransaction ─────────────────────────────────────────────────

    public List<Tag> findTagsByTransaction(int transactionId) {
        String sql = "SELECT t.id, t.profile_id, t.name, t.created_at " +
                     "FROM tags t " +
                     "JOIN transaction_tags tt ON tt.tag_id = t.id " +
                     "WHERE tt.transaction_id = ? ORDER BY t.name";
        List<Tag> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tags for transaction: " + e.getMessage(), e);
        }
    }

    // ── private helper ────────────────────────────────────────────────────────

    private Tag mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Tag(
            rs.getInt("id"),
            rs.getInt("profile_id"),
            rs.getString("name"),
            ts != null ? ts.toLocalDateTime() : null
        );
    }
}
