/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

/**
 * Conexión JDBC configurable por variables de entorno.
 *
 * En local, si no se define ninguna variable, usa:
 * 127.0.0.1:3306 / gluco_api / root / root
 *
 * En Render se configuran DB_HOST, DB_PORT, DB_NAME, DB_USER,
 * DB_PASSWORD y DB_SSL_MODE.
 *
 * @author mykec
 */
public class ConnectionMysql {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "gluco_api";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final String DEFAULT_SSL_MODE = "DISABLED";

    private static final Set<String> SSL_MODES = Set.of(
            "DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY");

    public Connection open() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontró el driver de MySQL. Verifica mysql-connector-j en WEB-INF/lib.", e);
        }

        String url = env("DB_URL", null);

        if (url == null || url.isBlank()) {
            String host = env("DB_HOST", DEFAULT_HOST);
            String port = env("DB_PORT", DEFAULT_PORT);
            String db = env("DB_NAME", DEFAULT_DB);
            String sslMode = env("DB_SSL_MODE", DEFAULT_SSL_MODE).toUpperCase(Locale.ROOT);

            if (!SSL_MODES.contains(sslMode)) {
                sslMode = DEFAULT_SSL_MODE;
            }

            url = "jdbc:mysql://" + host + ":" + port + "/" + db
                    + "?sslMode=" + sslMode
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8"
                    + "&serverTimezone=America/Mexico_City"
                    + "&connectTimeout=10000"
                    + "&socketTimeout=30000";
        }

        String user = env("DB_USER", DEFAULT_USER);
        String password = env("DB_PASSWORD", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
