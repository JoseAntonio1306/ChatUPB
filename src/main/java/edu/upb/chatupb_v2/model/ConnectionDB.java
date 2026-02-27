package edu.upb.chatupb_v2.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Maneja una conexión SQLite.
 *
 * - Por defecto usa ./chat_upb.sqlite (en el directorio de ejecución)
 * - Puedes override con variable de entorno SQLITE_URL (ej: jdbc:sqlite:/abs/path/chat_upb.sqlite)
 */
public final class ConnectionDB {

    private static final ConnectionDB INSTANCE = new ConnectionDB();

    private final String url;

    private ConnectionDB() {
        String envUrl = System.getenv("SQLITE_URL");
        this.url = (envUrl == null || envUrl.isBlank())
                ? "jdbc:sqlite:./chat_upb.sqlite"
                : envUrl.trim();

        if (!this.url.startsWith("jdbc:sqlite:")) {
            throw new IllegalStateException("SQLITE_URL debe empezar con 'jdbc:sqlite:' => " + this.url);
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encuentra el driver JDBC de SQLite", e);
        }
    }

    public static ConnectionDB getInstance() {
        return INSTANCE;
    }

    public Connection getConection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
