package repository;

import config.DBConnection;
import model.Currency;
import model.Transaction;
import model.TransactionType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository implements Repository<Transaction, Integer> {
    private static final String SQL_INSERT = """
            INSERT INTO transactions
                (profile_id, date, amount, type, currency, category_id, description, created_at, updated_at)
            VALUES (?, ?, ?, CAST(? AS transaction_type), CAST(? AS currency_type), ?, ?, ?, ?)
            """;
    private static final String SQL_FIND_ALL = """
            SELECT id, profile_id, date, amount, type, currency, category_id, description, created_at, updated_at
            FROM transactions
            ORDER BY date DESC, created_at DESC, id DESC
            """;
    private static final String SQL_FIND_BY_ID = """
            SELECT id, profile_id, date, amount, type, currency, category_id, description, created_at, updated_at
            FROM transactions
            WHERE id = ?
            """;
    private static final String SQL_FIND_BY_PROFILE = """
            SELECT id, profile_id, date, amount, type, currency, category_id, description, created_at, updated_at
            FROM transactions
            WHERE profile_id = ?
            ORDER BY date DESC, created_at DESC, id DESC
            """;
    private static final String SQL_UPDATE = """
            UPDATE transactions
            SET profile_id = ?,
                date = ?,
                amount = ?,
                type = CAST(? AS transaction_type),
                currency = CAST(? AS currency_type),
                category_id = ?,
                description = ?,
                updated_at = ?
            WHERE id = ?
            """;
    private static final String SQL_DELETE = "DELETE FROM transactions WHERE id = ?";

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(transaction.getCreatedAt());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindInsert(stmt, transaction);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
            return transaction;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save transaction.", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load transactions.", e);
        }
        return transactions;
    }

    public List<Transaction> findByProfileId(int profileId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PROFILE)) {
            stmt.setInt(1, profileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load transactions by profile.", e);
        }
        return transactions;
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load transaction by id.", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Transaction transaction) {
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(LocalDateTime.now());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            bindUpdate(stmt, transaction);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update transaction.", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete transaction.", e);
        }
    }

    private void bindInsert(PreparedStatement stmt, Transaction transaction) throws SQLException {
        stmt.setInt(1, transaction.getProfileId());
        stmt.setDate(2, Date.valueOf(transaction.getDate()));
        stmt.setBigDecimal(3, transaction.getAmount());
        stmt.setString(4, transaction.getType().name());
        stmt.setString(5, transaction.getCurrency().name());
        if (transaction.getCategoryId() == null) {
            stmt.setNull(6, Types.INTEGER);
        } else {
            stmt.setInt(6, transaction.getCategoryId());
        }
        stmt.setString(7, transaction.getDescription());
        stmt.setTimestamp(8, Timestamp.valueOf(transaction.getCreatedAt()));
        stmt.setTimestamp(9, Timestamp.valueOf(transaction.getUpdatedAt()));
    }

    private void bindUpdate(PreparedStatement stmt, Transaction transaction) throws SQLException {
        stmt.setInt(1, transaction.getProfileId());
        stmt.setDate(2, Date.valueOf(transaction.getDate()));
        stmt.setBigDecimal(3, transaction.getAmount());
        stmt.setString(4, transaction.getType().name());
        stmt.setString(5, transaction.getCurrency().name());
        if (transaction.getCategoryId() == null) {
            stmt.setNull(6, Types.INTEGER);
        } else {
            stmt.setInt(6, transaction.getCategoryId());
        }
        stmt.setString(7, transaction.getDescription());
        stmt.setTimestamp(8, Timestamp.valueOf(transaction.getUpdatedAt()));
        stmt.setInt(9, transaction.getId());
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new Transaction(
                rs.getInt("id"),
                rs.getInt("profile_id"),
                rs.getDate("date").toLocalDate(),
                rs.getBigDecimal("amount"),
                TransactionType.valueOf(rs.getString("type")),
                Currency.valueOf(rs.getString("currency")),
                (Integer) rs.getObject("category_id"),
                rs.getString("description"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime());
    }
}