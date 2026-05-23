package repository;

import config.DBConnection;
import model.CurrencyType;
import model.FrequencyType;
import model.RecurringExpense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecurringExpenseRepository implements Repository<RecurringExpense, Integer> {

    private static final String SQL_INSERT =
            "INSERT INTO recurring_expenses (profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active FROM recurring_expenses";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, profile_id, amount, currency, category_id, description, frequency, start_date, end_date, is_active FROM recurring_expenses WHERE id = ?";

    private static final String SQL_UPDATE =
            "UPDATE recurring_expenses SET profile_id = ?, amount = ?, currency = ?, category_id = ?, description = ?, frequency = ?, start_date = ?, end_date = ?, is_active = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM recurring_expenses WHERE id = ?";

    private RecurringExpense mapRow(ResultSet rs) throws SQLException {
        RecurringExpense re = new RecurringExpense();
        re.setId(rs.getInt("id"));
        re.setProfileId(rs.getInt("profile_id"));
        re.setAmount(rs.getBigDecimal("amount"));
        re.setCurrency(CurrencyType.valueOf(rs.getString("currency")));
        re.setCategoryId(rs.getInt("category_id"));
        re.setDescription(rs.getString("description"));
        re.setFrequency(FrequencyType.valueOf(rs.getString("frequency")));
        re.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        re.setEndDate(endDate != null ? endDate.toLocalDate() : null);
        re.setActive(rs.getBoolean("is_active"));
        return re;
    }

    @Override
    public RecurringExpense save(RecurringExpense re) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, re.getProfileId());
            stmt.setBigDecimal(2, re.getAmount());
            stmt.setString(3, re.getCurrency().name());
            stmt.setInt(4, re.getCategoryId());
            stmt.setString(5, re.getDescription());
            stmt.setString(6, re.getFrequency().name());
            stmt.setDate(7, Date.valueOf(re.getStartDate()));
            stmt.setDate(8, re.getEndDate() != null ? Date.valueOf(re.getEndDate()) : null);
            stmt.setBoolean(9, re.isActive());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                re.setId(generatedKeys.getInt(1));
            }
            stmt.close();
            return re;
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.save() failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<RecurringExpense> findAll() {
        List<RecurringExpense> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.findAll() failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<RecurringExpense> findById(Integer id) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Optional<RecurringExpense> result = Optional.of(mapRow(rs));
                rs.close();
                stmt.close();
                return result;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.findById() failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void update(RecurringExpense re) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE);
            
            stmt.setInt(1, re.getProfileId());
            stmt.setBigDecimal(2, re.getAmount());
            stmt.setString(3, re.getCurrency().name());
            stmt.setInt(4, re.getCategoryId());
            stmt.setString(5, re.getDescription());
            stmt.setString(6, re.getFrequency().name());
            stmt.setDate(7, Date.valueOf(re.getStartDate()));
            stmt.setDate(8, re.getEndDate() != null ? Date.valueOf(re.getEndDate()) : null);
            stmt.setBoolean(9, re.isActive());
            stmt.setInt(10, re.getId());

            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.update() failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Integer id) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SQL_DELETE);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.deleteById() failed: " + e.getMessage());
        }
    }

    public List<RecurringExpense> findByProfileId(int profileId) {
        List<RecurringExpense> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_expenses WHERE profile_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, profileId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("RecurringExpenseRepository.findByProfileId() failed: " + e.getMessage());
        }
        return list;
    }
}
