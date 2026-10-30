package com.mycompany.projectofinalventas.datos;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    public static Connection conectar() throws SQLException {
        Properties config = new Properties();
        Path archivo = Path.of(System.getProperty("db.config", "config/db.properties"));
        if (Files.exists(archivo)) {
            try (Reader entrada = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                config.load(entrada);
            } catch (IOException e) {
                throw new SQLException("No se pudo leer la configuracion JDBC", "08001");
            }
        }
        String url = valor(config, "db.url", "DB_URL");
        String usuario = valor(config, "db.user", "DB_USER");
        String clave = valor(config, "db.password", "DB_PASSWORD");
        if (url == null || !url.startsWith("jdbc:postgresql:") || usuario == null
                || usuario.isBlank() || clave == null) {
            throw new SQLException("Configurar PostgreSQL en config/db.properties o DB_URL, DB_USER, DB_PASSWORD", "08001");
        }
        Properties credenciales = new Properties();
        credenciales.setProperty("user", usuario);
        credenciales.setProperty("password", clave);
        credenciales.setProperty("connectTimeout", "5");
        credenciales.setProperty("socketTimeout", "15");
        try {
            Connection cn = DriverManager.getConnection(url, credenciales);
            if (Boolean.getBoolean("soloLectura")) {
                cn.setReadOnly(true);
                cn.setAutoCommit(false);
            }
            return cn;
        } catch (SQLException e) {
            // Los detalles del servidor pueden contener datos privados.
            throw new SQLException("No se pudo conectar a PostgreSQL; revisar servicio y configuracion local",
                    e.getSQLState());
        }
    }

    private static String valor(Properties config, String clave, String variable) {
        String entorno = System.getenv(variable);
        return entorno != null ? entorno : config.getProperty(clave);
    }
}
