package ua.nupp.kpp.server.dao;

import ua.nupp.kpp.server.db.DbConnection;
import ua.nupp.kpp.server.model.Measurement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO таблиці measurement. */
public class MeasurementDao {
    private static final String COLUMNS = "id, sample_id, parameter, value, unit, norm_limit";

    public List<Measurement> findAll() {
        return query("SELECT " + COLUMNS + " FROM measurement ORDER BY sample_id, id", null);
    }

    public List<Measurement> findBySample(int sampleId) {
        return query("SELECT " + COLUMNS + " FROM measurement WHERE sample_id = ? ORDER BY id", sampleId);
    }

    public Optional<Measurement> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM measurement WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати вимірювання id=" + id, e);
        }
    }

    public int countBySample(int sampleId) {
        String sql = "SELECT COUNT(*) FROM measurement WHERE sample_id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sampleId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося підрахувати вимірювання проби id=" + sampleId, e);
        }
    }

    public int insert(Measurement m) {
        String sql = "INSERT INTO measurement (sample_id, parameter, value, unit, norm_limit) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getSampleId());
            ps.setString(2, m.getParameter());
            ps.setBigDecimal(3, m.getValue());
            ps.setString(4, m.getUnit());
            ps.setBigDecimal(5, m.getNormLimit());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося додати вимірювання", e);
        }
    }

    public boolean update(Measurement m) {
        String sql = "UPDATE measurement SET parameter = ?, value = ?, unit = ?, norm_limit = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getParameter());
            ps.setBigDecimal(2, m.getValue());
            ps.setString(3, m.getUnit());
            ps.setBigDecimal(4, m.getNormLimit());
            ps.setInt(5, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося змінити вимірювання id=" + m.getId(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM measurement WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося видалити вимірювання id=" + id, e);
        }
    }

    private List<Measurement> query(String sql, Integer param) {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Measurement> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати список вимірювань", e);
        }
    }

    private Measurement map(ResultSet rs) throws SQLException {
        return new Measurement(
                rs.getInt("id"),
                rs.getInt("sample_id"),
                rs.getString("parameter"),
                rs.getBigDecimal("value"),
                rs.getString("unit"),
                rs.getBigDecimal("norm_limit"));
    }
}
