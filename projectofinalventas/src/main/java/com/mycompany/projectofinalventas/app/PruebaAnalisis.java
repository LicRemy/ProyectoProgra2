
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.analisis.AnalizadorVentas;
import java.sql.SQLException;

public class PruebaAnalisis {

    public static void main(String[] args) {

        System.out.println("SISTEMA DE ANALISIS DE VENTAS");

        AnalizadorVentas analizador =
                new AnalizadorVentas();

        try {
            analizador.mostrarAnalisisCompleto();

            System.out.println(
                    "\nAnalisis finalizado correctamente.");

        } catch (SQLException e) {
            System.out.println(
                    "Error en analisis SQL: " + e.getMessage());
        }
    }
}
