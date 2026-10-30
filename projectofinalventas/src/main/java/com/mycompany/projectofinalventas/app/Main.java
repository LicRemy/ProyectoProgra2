

package com.mycompany.projectofinalventas.app;


import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.lectores.LectorCSV;
import com.mycompany.projectofinalventas.lectores.LectorJSON;
import com.mycompany.projectofinalventas.lectores.LectorXML;
import com.mycompany.projectofinalventas.validacion.DeduplicadorVentas;
import com.mycompany.projectofinalventas.datos.VentaDAO;
import com.mycompany.projectofinalventas.modelo.EstadisticasProcesamiento;
import com.mycompany.projectofinalventas.reportes.GeneradorReportes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== INTEGRACION DE VENTAS ===");

        
        
        EstadisticasProcesamiento estadisticas =
        new EstadisticasProcesamiento();
            
        List<RegistroVenta> ventas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        int erroresAntesLectura = errores.size();
        
        LectorCSV csv = new LectorCSV();
        LectorJSON json = new LectorJSON();
        LectorXML xml = new LectorXML();

        // Leer los tres formatos
        try {
            ventas.addAll(csv.leer("entrada/ventas.csv", errores));
            System.out.println("CSV procesado.");
        } catch (IllegalStateException e) {
            errores.add("CSV: " + e.getMessage());
            estadisticas.archivosFallidos++;
        }

        try {
            ventas.addAll(json.leer("entrada/ventas.json", errores));
            System.out.println("JSON procesado.");
        } catch (IllegalStateException e) {
            errores.add("JSON: " + e.getMessage());
            estadisticas.archivosFallidos++;
        }

        try {
            ventas.addAll(xml.leer("entrada/ventas.xml", errores));
            System.out.println("XML procesado.");
        } catch (IllegalStateException e) {
            errores.add("XML: " + e.getMessage());
            estadisticas.archivosFallidos++;
        }

        estadisticas.validos = ventas.size();
        estadisticas.rechazados =
                errores.size() - erroresAntesLectura;
        estadisticas.recibidos =
                estadisticas.validos + estadisticas.rechazados;

        // Contar registros que pasaron la validacion
        int validadas = ventas.size();

        // Detectar IDs repetidos
        DeduplicadorVentas deduplicador =
                new DeduplicadorVentas();

        List<RegistroVenta> ventasUnicas =
                deduplicador.eliminarDuplicados(ventas, errores);
        
        estadisticas.duplicados =
        deduplicador.getDuplicados();

estadisticas.unicos = ventasUnicas.size();
        
        
VentaDAO dao = new VentaDAO();

int insertadas = 0;
int existentes = 0;
int fallosBD = 0;

for (RegistroVenta venta : ventasUnicas) {

    try {
        boolean insertada = dao.insertar(venta);

        if (insertada) {
            insertadas++;
        } else {
            existentes++;
        }

    } catch (SQLException e) {
        fallosBD++;

        errores.add(
                "Error SQL en venta "
                + venta.getIdVenta()
                + ": " + e.getMessage());
    }
}

estadisticas.insertados = insertadas;
estadisticas.existentesBD = existentes;
estadisticas.fallosBD = fallosBD;

System.out.println("\n=== RESULTADO POSTGRESQL ===");

System.out.println("Ventas insertadas: " + insertadas);
System.out.println("Ya existentes en BD: " + existentes);
System.out.println("Fallos SQL: " + fallosBD);

try {
    System.out.println(
            "Total almacenadas: " + dao.contarVentas());

} catch (SQLException e) {
    System.out.println(
            "Error consultando total: " + e.getMessage());
}


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

        GeneradorReportes generador = new GeneradorReportes();

        try {
            generador.generar(estadisticas, errores);

            System.out.println("\nREPORTES GENERADOS:");
            System.out.println("reporte_analisis.txt");
            System.out.println("errores_procesamiento.txt");

        } catch (IOException | SQLException e) {
            System.out.println(
                    "No se pudieron generar los reportes: "
                    + e.getMessage());
        }
    }
}
