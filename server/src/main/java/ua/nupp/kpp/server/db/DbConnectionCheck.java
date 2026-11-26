package ua.nupp.kpp.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Ручна перевірка JDBC-з'єднання: запускається як звичайна програма (Run). */
public class DbConnectionCheck {
    public static void main(String[] args) {
        try (Connection c = DbConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT code, name, sensitivity FROM observation_point ORDER BY id")) {
            System.out.println("З'єднання з MySQL встановлено: " + c.getMetaData().getURL());
            while (rs.next()) {
                System.out.println(rs.getString("code") + " | " + rs.getString("name")
                        + " | чутливість " + rs.getInt("sensitivity"));
            }
        } catch (SQLException e) {
            System.err.println("Помилка підключення: " + e.getMessage());
        }
    }
}
