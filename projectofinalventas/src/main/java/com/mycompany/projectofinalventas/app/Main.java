package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.modelo.EstadisticasProcesamiento;
import com.mycompany.projectofinalventas.reportes.GeneradorReportes;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Punto de entrada: menu interactivo por defecto y modo lote opcional. */
public class Main {

    public static void main(String[] args) {
        // Con argumentos (archivos individuales) o -DmodoLote=true se mantiene
        // el comportamiento automatico anterior del proyecto.
        if (args.length > 0 || Boolean.getBoolean("modoLote")) {
            ejecutarLote(args);
        } else {
            new MenuVentas().iniciar();
        }
    }

    public static void ejecutarLote(String[] args) {
        System.out.println("=== INTEGRACION DE VENTAS (MODO LOTE) ===");
        if (Boolean.getBoolean("soloLectura")) {
            System.out.println("MODO SOLO LECTURA: PostgreSQL impedira cambios; los IDs nuevos no se insertaran.");
        }

        EstadisticasProcesamiento estadisticas = new EstadisticasProcesamiento();
        List<String> errores = new ArrayList<>();
        ProcesadorVentas procesador = new ProcesadorVentas();
        Path salida = Path.of(System.getProperty("salida", "."));

        try {
            List<Path> archivos = args.length == 0
                    ? procesador.descubrir(Path.of(System.getProperty("entrada", "entrada")))
                    : Arrays.stream(args).map(Path::of).toList();
            if (archivos.isEmpty()) {
                throw new IOException("No hay archivos CSV, JSON o XML para procesar");
            }
            var ventas = procesador.leer(archivos, estadisticas, errores);
            procesador.guardar(ventas, estadisticas, errores);
            estadisticas.comprobar();
        } catch (IOException ex) {
            estadisticas.archivosFallidos++;
            errores.add("Entrada no disponible: " + ex.getMessage());
        }

        try {
            new GeneradorReportes().generar(estadisticas, errores, salida);
            System.out.println("REPORTES GENERADOS en " + salida.toAbsolutePath().normalize());
            System.out.printf("Recibidos: %d | Aceptados unicos: %d | Rechazados: %d | Duplicados: %d%n",
                    estadisticas.recibidos, estadisticas.unicos,
                    estadisticas.rechazados, estadisticas.duplicados);
            if (estadisticas.archivosFallidos + estadisticas.fallosBD + estadisticas.conflictosBD > 0) {
                System.out.println("PROCESAMIENTO CON INCIDENCIAS: revisar errores_procesamiento.txt");
            }
        } catch (IOException | SQLException ex) {
            System.err.println("REPORTES INCOMPLETOS: revisar archivos y configuracion JDBC. "
                    + (ex instanceof SQLException sql ? "SQLState=" + sql.getSQLState() : ex.getMessage()));
        }
    }
}
