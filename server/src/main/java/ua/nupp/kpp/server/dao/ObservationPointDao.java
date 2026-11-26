package ua.nupp.kpp.server.dao;

import ua.nupp.kpp.server.db.DbConnection;
import ua.nupp.kpp.server.model.ObservationPoint;
import ua.nupp.kpp.server.model.WaterUse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO таблиці observation_point. Усі SQL-запити цієї сутності зосереджені тут. */
public class ObservationPointDao {
    private static final String COLUMNS = "id, code, name, water_body, water_use, sensitivity";

    public List<ObservationPoint> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM observation_point ORDER BY id";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ObservationPoint> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати список пунктів спостереження", e);
        }
    }

    public Optional<ObservationPoint> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM observation_point WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати пункт спостереження id=" + id, e);
        }
    }

    /** Чи існує інший пункт із таким кодом (excludeId — пункт, який редагується; 0 для нового). */
    public boolean existsByCode(String code, int excludeId) {
        String sql = "SELECT COUNT(*) FROM observation_point WHERE code = ? AND id <> ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося перевірити унікальність коду " + code, e);
        }
    }

    /** Додає пункт і повертає згенерований id. */
    public int insert(ObservationPoint p) {
        String sql = "INSERT INTO observation_point (code, name, water_body, water_use, sensitivity) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getWaterBody());
            ps.setString(4, p.getWaterUse().name());
            ps.setInt(5, p.getSensitivity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося додати пункт спостереження", e);
        }
    }

    public boolean update(ObservationPoint p) {
        String sql = "UPDATE observation_point SET code = ?, name = ?, water_body = ?, "
                + "water_use = ?, sensitivity = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getWaterBody());
            ps.setString(4, p.getWaterUse().name());
            ps.setInt(5, p.getSensitivity());
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося змінити пункт спостереження id=" + p.getId(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM observation_point WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося видалити пункт спостереження id=" + id, e);
        }
    }

    private ObservationPoint map(ResultSet rs) throws SQLException {
        return new ObservationPoint(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("water_body"),
                WaterUse.valueOf(rs.getString("water_use")),
                rs.getInt("sensitivity"));
    }
}
