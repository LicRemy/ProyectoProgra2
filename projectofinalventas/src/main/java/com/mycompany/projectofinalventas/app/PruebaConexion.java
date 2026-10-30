
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.datos.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        System.out.println("PROBANDO POSTGRESQL...");

        try (Connection conexion = ConexionBD.conectar()) {

            System.out.println(
                    "Conexion establecida correctamente.");

            System.out.println(
                    "Base de datos: "
                    + conexion.getCatalog());

        } catch (SQLException e) {

            System.out.println(
                    "Error de conexion: " + e.getMessage());
        }
    }
}
