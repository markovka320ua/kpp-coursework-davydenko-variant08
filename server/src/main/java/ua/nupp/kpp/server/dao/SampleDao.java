package ua.nupp.kpp.server.dao;

import ua.nupp.kpp.server.db.DbConnection;
import ua.nupp.kpp.server.model.Sample;
import ua.nupp.kpp.server.model.SampleState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO таблиці sample. */
public class SampleDao {
    private static final String COLUMNS = "id, point_id, sampled_at, collector, state, integral_index";

    public List<Sample> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM sample ORDER BY sampled_at DESC, id DESC";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Sample> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати список проб", e);
        }
    }

    public Optional<Sample> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM sample WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося отримати пробу id=" + id, e);
        }
    }

    public int countByPoint(int pointId) {
        String sql = "SELECT COUNT(*) FROM sample WHERE point_id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pointId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося підрахувати проби пункту id=" + pointId, e);
        }
    }

    public int insert(Sample s) {
        String sql = "INSERT INTO sample (point_id, sampled_at, collector, state, integral_index) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getPointId());
            ps.setTimestamp(2, Timestamp.valueOf(s.getSampledAt()));
            ps.setString(3, s.getCollector());
            ps.setString(4, s.getState().name());
            setIndex(ps, 5, s.getIntegralIndex());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не вдалося додати пробу", e);
        }
    }

    /** Змінює редаговані поля проби (стан і показник змінюються окремими методами). */
    public boolean update(Sample s) {
        String sql = "UPDATE sample SET point_id = ?, sampled_at = ?, collector = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getPointId());
            ps.setTimestamp(2, Timestamp.valueOf(s.getSampledAt()));
            ps.setString(3, s.getCollector());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося змінити пробу id=" + s.getId(), e);
        }
    }

    public boolean updateState(int id, SampleState state) {
        String sql = "UPDATE sample SET state = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, state.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося змінити стан проби id=" + id, e);
        }
    }

    public boolean updateIndex(int id, BigDecimal index) {
        String sql = "UPDATE sample SET integral_index = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setIndex(ps, 1, index);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося зберегти показник проби id=" + id, e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM sample WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не вдалося видалити пробу id=" + id, e);
        }
    }

    private void setIndex(PreparedStatement ps, int pos, BigDecimal index) throws SQLException {
        if (index == null) {
            ps.setNull(pos, Types.DECIMAL);
        } else {
            ps.setBigDecimal(pos, index);
        }
    }

    private Sample map(ResultSet rs) throws SQLException {
        return new Sample(
                rs.getInt("id"),
                rs.getInt("point_id"),
                rs.getTimestamp("sampled_at").toLocalDateTime(),
                rs.getString("collector"),
                SampleState.valueOf(rs.getString("state")),
                rs.getBigDecimal("integral_index"));
    }
}
