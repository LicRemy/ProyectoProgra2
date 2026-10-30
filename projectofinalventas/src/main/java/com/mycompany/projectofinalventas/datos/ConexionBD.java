
package com.mycompany.projectofinalventas.datos;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

public class ConexionBD {

    public static Connection conectar()
            throws SQLException {

        Properties config = new Properties();

        Path archivo = Path.of("config", "db.properties");

        try (InputStream entrada =
                Files.newInputStream(archivo)) {

            config.load(entrada);

        } catch (IOException e) {
            throw new SQLException(
                    "No se pudo leer config/db.properties", e);
        }

        String url = config.getProperty("db.url");
        String usuario = config.getProperty("db.user");
        String clave = config.getProperty("db.password");

        if (url == null || usuario == null || clave == null) {
            throw new SQLException(
                    "Configuracion de base de datos incompleta");
        }

        return DriverManager.getConnection(
                url, usuario, clave);
    }
}
