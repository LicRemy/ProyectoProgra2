
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.lectores.LectorCSV;
import com.mycompany.projectofinalventas.modelo.RegistroVenta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== ANALISIS DE VENTAS ===");

        LectorCSV lector = new LectorCSV();

        List<String> errores = new ArrayList<>();

        List<RegistroVenta> ventas;

        try {
            ventas = lector.leer("entrada/ventas.csv", errores);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\nVENTAS ACEPTADAS:");

        for (RegistroVenta venta : ventas) {
            System.out.println("---------------------");
            System.out.println("ID: " + venta.getIdVenta());
            System.out.println("Sucursal: " + venta.getSucursal());
            System.out.println("Producto: " + venta.getProducto());
            System.out.println("Total: Q" + venta.calcularTotal());
        }

        System.out.println("\n=== RESUMEN ===");
        System.out.println("Aceptadas: " + ventas.size());
        System.out.println("Rechazadas: " + errores.size());

        try {
            Path salida = Path.of("errores_procesamiento.txt");

            Files.write(
                    salida,
                    errores,
                    StandardCharsets.UTF_8
            );

            System.out.println("Archivo de errores generado.");

        } catch (IOException e) {
            System.out.println(
                    "Error al generar reporte de errores: "
                    + e.getMessage());
        }
    }
}
