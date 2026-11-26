package ua.nupp.kpp.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Підключення до MySQL. Параметри читаються з db.properties (classpath),
 * щоб логін і пароль не були зашиті в коді.
 */
public final class DbConnection {
    private static final Properties PROPS = load();

    static {
        try {
            // Явна реєстрація драйвера: у Tomcat JDBC-драйвер лежить у WEB-INF/lib
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC-драйвер не знайдено в classpath", e);
        }
    }

    private DbConnection() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = DbConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Файл db.properties не знайдено в resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати db.properties", e);
        }
        return props;
    }

    /** Повертає нове з'єднання; закривати його має викликач (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password", ""));
    }
}
