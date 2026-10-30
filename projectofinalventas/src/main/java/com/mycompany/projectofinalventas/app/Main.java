
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.lectores.LectorCSV;
import com.mycompany.projectofinalventas.lectores.LectorJSON;
import com.mycompany.projectofinalventas.lectores.LectorXML;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== INTEGRACION DE VENTAS ===");

        List<RegistroVenta> ventas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        LectorCSV csv = new LectorCSV();
        LectorJSON json = new LectorJSON();
        LectorXML xml = new LectorXML();

        // Leer CSV
        try {
            ventas.addAll(
                    csv.leer("entrada/ventas.csv", errores));

            System.out.println("CSV procesado.");

        } catch (IllegalStateException e) {
            errores.add(e.getMessage());
        }

        // Leer JSON
        try {
            ventas.addAll(
                    json.leer("entrada/ventas.json", errores));

            System.out.println("JSON procesado.");

        } catch (IllegalStateException e) {
            errores.add(e.getMessage());
        }

        // Leer XML
        try {
            ventas.addAll(
                    xml.leer("entrada/ventas.xml", errores));

            System.out.println("XML procesado.");

        } catch (IllegalStateException e) {
            errores.add(e.getMessage());
        }

        // Mostrar datos validos
        System.out.println("\n=== REGISTROS ACEPTADOS ===");

        for (RegistroVenta venta : ventas) {
            System.out.println("--------------------------");
            System.out.println("ID: " + venta.getIdVenta());
            System.out.println("Fecha: " + venta.getFecha());
            System.out.println("Sucursal: " + venta.getSucursal());
            System.out.println("Producto: " + venta.getProducto());
            System.out.println("Vendedor: " + venta.getVendedor());
            System.out.println("Total: Q" + venta.calcularTotal());
        }

        // Resumen
        System.out.println("\n=== RESUMEN PRELIMINAR ===");
        System.out.println("Convertidos y validados: "
                + ventas.size());
        System.out.println("Incidencias: " + errores.size());

        // Guardar evidencia de errores
        try {
            Files.write(
                    Path.of("errores_procesamiento.txt"),
                    errores,
                    StandardCharsets.UTF_8);

            System.out.println("Archivo de errores generado.");

        } catch (IOException e) {
            System.out.println("Error al escribir reporte: "
                    + e.getMessage());
        }
    }
}
