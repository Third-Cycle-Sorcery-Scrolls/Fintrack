package repository;

import config.DBConnection;
import model.Category;
import model.Currency;
import model.FrequencyType;
import model.RecurringExpense;
import model.Transaction;
import model.TransactionType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecurringExpenseRepository implements Repository<RecurringExpense, Integer> {
    private static final String SQL_INSERT = """
            INSERT INTO recurring_expenses
                (profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active)
            VALUES (?, ?, CAST(? AS currency_type), ?, ?, CAST(? AS frequency_type), ?, ?, ?)
            """;
    private static final String SQL_FIND_ALL = """
            SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active
            FROM recurring_expenses
            ORDER BY start_date DESC, id DESC
            """;
    private static final String SQL_FIND_BY_ID = """
            SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active
            FROM recurring_expenses
            WHERE id = ?
            """;
    private static final String SQL_FIND_BY_PROFILE = """
            SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active
            FROM recurring_expenses
            WHERE profile_id = ?
            ORDER BY is_active DESC, start_date DESC, id DESC
            """;
    private static final String SQL_FIND_ACTIVE_BY_PROFILE = """
            SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active
            FROM recurring_expenses
            WHERE profile_id = ? AND is_active = true
            ORDER BY start_date, id
            """;
    private static final String SQL_UPDATE = """
            UPDATE recurring_expenses
            SET profile_id = ?,
                amount = ?,
                currency = CAST(? AS currency_type),
                category_id = ?,
                description = ?,
                frequency = CAST(? AS frequency_type),
                start_date = ?,
                end_date = ?,
                is_active = ?
            WHERE id = ?
            """;
    private static final String SQL_DELETE = "DELETE FROM recurring_expenses WHERE id = ?";
    private static final String SQL_SET_ACTIVE = "UPDATE recurring_expenses SET is_active = ? WHERE id = ?";
    private static final String SQL_FIND_CATEGORIES_BY_PROFILE = """
            SELECT id, profile_id, name
            FROM categories
            WHERE profile_id = ?
            ORDER BY name
            """;
    private static final String SQL_INSERT_GENERATED_TRANSACTION = """
            INSERT INTO transactions
                (profile_id, date, amount, type, currency, category_id, description)
            SELECT ?, ?, ?, CAST(? AS transaction_type), CAST(? AS currency_type), ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM transactions
                WHERE profile_id = ?
                  AND date = ?
                  AND amount = ?
                  AND type = CAST(? AS transaction_type)
                  AND currency = CAST(? AS currency_type)
                  AND category_id = ?
                  AND COALESCE(description, '') = COALESCE(?, '')
            )
            """;

    @Override
    public RecurringExpense save(RecurringExpense recurringExpense) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bindRecurringExpense(stmt, recurringExpense);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recurringExpense.setId(generatedKeys.getInt(1));
                }
            }
            return recurringExpense;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save recurring expense.", e);
        }
    }

    @Override
    public List<RecurringExpense> findAll() {
        List<RecurringExpense> recurringExpenses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recurringExpenses.add(mapRecurringExpense(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load recurring expenses.", e);
        }
        return recurringExpenses;
    }

    @Override
    public Optional<RecurringExpense> findById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRecurringExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load recurring expense by id.", e);
        }
        return Optional.empty();
    }

    public List<RecurringExpense> findByProfileId(int profileId) {
        return findByProfile(profileId, SQL_FIND_BY_PROFILE);
    }

    public List<RecurringExpense> findActiveByProfileId(int profileId) {
        return findByProfile(profileId, SQL_FIND_ACTIVE_BY_PROFILE);
    }

    @Override
    public void update(RecurringExpense recurringExpense) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            bindRecurringExpense(stmt, recurringExpense);
            stmt.setInt(10, recurringExpense.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update recurring expense.", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete recurring expense.", e);
        }
    }

    public void setActive(int id, boolean active) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SET_ACTIVE)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update recurring expense status.", e);
        }
    }

    public List<Category> findCategoriesByProfileId(int profileId) {
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_FIND_CATEGORIES_BY_PROFILE)) {
            stmt.setInt(1, profileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("id"),
                            rs.getInt("profile_id"),
                            rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load categories for recurring expenses.", e);
        }
        return categories;
    }

    public Optional<Transaction> saveGeneratedExpense(RecurringExpense recurringExpense, LocalDate dueDate) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_GENERATED_TRANSACTION, Statement.RETURN_GENERATED_KEYS)) {
            bindGeneratedTransaction(stmt, recurringExpense, dueDate);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return Optional.of(new Transaction(
                            generatedKeys.getInt(1),
                            recurringExpense.getProfileId(),
                            recurringExpense.getCategoryId(),
                            recurringExpense.getAmount(),
                            TransactionType.EXPENSE,
                            recurringExpense.getCurrency(),
                            recurringExpense.getDescription(),
                            dueDate));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to generate recurring expense transaction.", e);
        }
    }

    private List<RecurringExpense> findByProfile(int profileId, String sql) {
        List<RecurringExpense> recurringExpenses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recurringExpenses.add(mapRecurringExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load recurring expenses by profile.", e);
        }
        return recurringExpenses;
    }

    private void bindRecurringExpense(PreparedStatement stmt, RecurringExpense recurringExpense) throws SQLException {
        stmt.setInt(1, recurringExpense.getProfileId());
        stmt.setBigDecimal(2, recurringExpense.getAmount());
        stmt.setString(3, recurringExpense.getCurrency().name());
        stmt.setInt(4, recurringExpense.getCategoryId());
        stmt.setString(5, recurringExpense.getDescription());
        stmt.setString(6, recurringExpense.getFrequency().name());
        stmt.setDate(7, Date.valueOf(recurringExpense.getStartDate()));
        if (recurringExpense.getEndDate() == null) {
            stmt.setDate(8, null);
        } else {
            stmt.setDate(8, Date.valueOf(recurringExpense.getEndDate()));
        }
        stmt.setBoolean(9, recurringExpense.isActive());
    }

    private void bindGeneratedTransaction(PreparedStatement stmt, RecurringExpense recurringExpense, LocalDate dueDate) throws SQLException {
        stmt.setInt(1, recurringExpense.getProfileId());
        stmt.setDate(2, Date.valueOf(dueDate));
        stmt.setBigDecimal(3, recurringExpense.getAmount());
        stmt.setString(4, TransactionType.EXPENSE.name());
        stmt.setString(5, recurringExpense.getCurrency().name());
        stmt.setInt(6, recurringExpense.getCategoryId());
        stmt.setString(7, recurringExpense.getDescription());
        stmt.setInt(8, recurringExpense.getProfileId());
        stmt.setDate(9, Date.valueOf(dueDate));
        stmt.setBigDecimal(10, recurringExpense.getAmount());
        stmt.setString(11, TransactionType.EXPENSE.name());
        stmt.setString(12, recurringExpense.getCurrency().name());
        stmt.setInt(13, recurringExpense.getCategoryId());
        stmt.setString(14, recurringExpense.getDescription());
    }

    private RecurringExpense mapRecurringExpense(ResultSet rs) throws SQLException {
        Date endDate = rs.getDate("end_date");
        return new RecurringExpense(
                rs.getInt("id"),
                rs.getInt("profile_id"),
                rs.getInt("category_id"),
                rs.getBigDecimal("amount"),
                Currency.valueOf(rs.getString("currency")),
                rs.getString("description"),
                FrequencyType.valueOf(rs.getString("frequency")),
                rs.getDate("start_date").toLocalDate(),
                endDate == null ? null : endDate.toLocalDate(),
                rs.getBoolean("is_active"));
    }
}
