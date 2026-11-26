package ua.nupp.kpp.server.dao;

import ua.nupp.kpp.server.db.DbConnection;
import ua.nupp.kpp.server.model.Inspection;
import ua.nupp.kpp.server.model.InspectionResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO таблиці inspection. */
public class InspectionDao {
    private static final String COLUMNS = "id, sample_id, inspector, checked_at, result, note";

    public List<Inspection> findAll() {
        return query("SELECT " + COLUMNS + " FROM inspection ORDER BY checked_at DESC, id DESC", null);
    }

    public List<Inspection> findBySample(int sampleId) {
        return query("SELECT " + COLUMNS + " FROM inspection WHERE sample_id = ? ORDER BY checked_at, id", sampleId);
    }

    public Optional<Inspection> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM inspection WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати перевірку id=" + id, e);
        }
    }

    public int insert(Inspection i) {
        String sql = "INSERT INTO inspection (sample_id, inspector, checked_at, result, note) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getSampleId());
            ps.setString(2, i.getInspector());
            ps.setTimestamp(3, Timestamp.valueOf(i.getCheckedAt()));
            ps.setString(4, i.getResult().name());
            ps.setString(5, i.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося додати перевірку", e);
        }
    }

    public boolean update(Inspection i) {
        String sql = "UPDATE inspection SET inspector = ?, checked_at = ?, result = ?, note = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.getInspector());
            ps.setTimestamp(2, Timestamp.valueOf(i.getCheckedAt()));
            ps.setString(3, i.getResult().name());
            ps.setString(4, i.getNote());
            ps.setInt(5, i.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося змінити перевірку id=" + i.getId(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM inspection WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося видалити перевірку id=" + id, e);
        }
    }

    private List<Inspection> query(String sql, Integer param) {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Inspection> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати список перевірок", e);
        }
    }

    private Inspection map(ResultSet rs) throws SQLException {
        return new Inspection(
                rs.getInt("id"),
                rs.getInt("sample_id"),
                rs.getString("inspector"),
                rs.getTimestamp("checked_at").toLocalDateTime(),
                InspectionResult.valueOf(rs.getString("result")),
                rs.getString("note"));
    }
}
