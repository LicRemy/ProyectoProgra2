
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.lectores.LectorCSV;
import com.mycompany.projectofinalventas.lectores.LectorJSON;
import com.mycompany.projectofinalventas.lectores.LectorXML;
import com.mycompany.projectofinalventas.validacion.DeduplicadorVentas;

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

        // Leer los tres formatos
        try {
            ventas.addAll(csv.leer("entrada/ventas.csv", errores));
            System.out.println("CSV procesado.");
        } catch (IllegalStateException e) {
            errores.add("CSV: " + e.getMessage());
        }

        try {
            ventas.addAll(json.leer("entrada/ventas.json", errores));
            System.out.println("JSON procesado.");
        } catch (IllegalStateException e) {
            errores.add("JSON: " + e.getMessage());
        }

        try {
            ventas.addAll(xml.leer("entrada/ventas.xml", errores));
            System.out.println("XML procesado.");
        } catch (IllegalStateException e) {
            errores.add("XML: " + e.getMessage());
        }

        // Contar registros que pasaron la validacion
        int validadas = ventas.size();

        // Detectar IDs repetidos
        DeduplicadorVentas deduplicador =
                new DeduplicadorVentas();

        List<RegistroVenta> ventasUnicas =
                deduplicador.eliminarDuplicados(ventas, errores);

        // Mostrar las ventas unicas
        System.out.println("\n=== VENTAS UNICAS ===");

        for (RegistroVenta venta : ventasUnicas) {

            System.out.println("-----------------------");
            System.out.println("ID: " + venta.getIdVenta());
            System.out.println("Producto: " + venta.getProducto());
            System.out.println("Sucursal: " + venta.getSucursal());
            System.out.println("Total: Q" + venta.calcularTotal());
        }

        int duplicados = deduplicador.getDuplicados();

        System.out.println("\n=== RESUMEN PRELIMINAR ===");
        System.out.println("Registros validados: " + validadas);
        System.out.println("Registros unicos: " + ventasUnicas.size());
        System.out.println("Duplicados: " + duplicados);
        System.out.println("Incidencias totales: " + errores.size());

        // Generar archivo de errores
        try {

            Files.write(
                    Path.of("errores_procesamiento.txt"),
                    errores,
                    StandardCharsets.UTF_8
            );

            System.out.println("Archivo de errores generado.");

        } catch (IOException e) {

            System.out.println(
                    "Error al guardar incidencias: "
                    + e.getMessage());
        }
    }
}
