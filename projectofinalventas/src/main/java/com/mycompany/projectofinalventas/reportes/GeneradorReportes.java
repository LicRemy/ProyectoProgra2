package com.mycompany.projectofinalventas.reportes;

import com.mycompany.projectofinalventas.analisis.AnalizadorVentas;
import com.mycompany.projectofinalventas.modelo.EstadisticasProcesamiento;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class GeneradorReportes {
    public void generar(EstadisticasProcesamiento e, List<String> errores) throws IOException, SQLException {
        generar(e, errores, Path.of("."));
    }

    public void generar(EstadisticasProcesamiento e, List<String> errores, Path salida)
            throws IOException, SQLException {
        guardar(salida.resolve("errores_procesamiento.txt"), String.join("\n", errores)
                + (errores.isEmpty() ? "" : "\n"));
        try {
            publicar(e, salida, new AnalizadorVentas().analizar());
        } catch (SQLException ex) {
            guardar(salida.resolve("reporte_analisis.txt"), "REPORTE INCOMPLETO: analisis SQL no disponible.\n"
                    + "No utilizar como resultado exitoso. SQLState=" + ex.getSQLState() + "\n" + estadisticas(e));
            throw ex;
        }
    }

    /** Variante para verificar reportes usando una conexion con tabla temporal. */
    public void generar(EstadisticasProcesamiento e, List<String> errores, Path salida, Connection cn)
            throws IOException, SQLException {
        guardar(salida.resolve("errores_procesamiento.txt"), String.join("\n", errores) + "\n");
        publicar(e, salida, new AnalizadorVentas().analizar(cn));
    }

    private void publicar(EstadisticasProcesamiento e, Path salida, String analisis) throws IOException {
        String conclusiones = """

                
                -
                """;
        guardar(salida.resolve("reporte_analisis.txt"),
                "SISTEMA DE INTEGRACION Y ANALISIS DE VENTAS\n"
                + "=========================================\n"
                + (e.archivosFallidos + e.fallosBD + e.conflictosBD > 0
                    ? "ESTADO: CON INCIDENCIAS; ver errores_procesamiento.txt\n" : "ESTADO: PROCESAMIENTO COMPLETO\n")
                + estadisticas(e)
                + "\nINDICADORES ACUMULADOS EN POSTGRESQL (instantanea consistente)\n"
                + "Los nombres se agrupan sin distinguir mayusculas; montos expresados en Q.\n"
                + analisis + conclusiones);
    }

    private String estadisticas(EstadisticasProcesamiento e) {
        return String.format(Locale.ROOT, """

                ESTADISTICAS DE ESTA EJECUCION
                --------------------------------
                Archivos procesados: %d
                Archivos fallidos: %d
                Registros recibidos: %d
                Validos antes de duplicados: %d
                Aceptados unicos: %d
                Rechazados por validacion: %d
                Duplicados entre archivos: %d
                Porcentaje de rechazados: %.2f%%
                Insertados en PostgreSQL: %d
                Ya existentes identicos en PostgreSQL: %d
                Conflictos con PostgreSQL: %d
                Fallos de insercion SQL: %d
                Definicion: rechazados / recibidos * 100; cero si no hay registros.
                recibidos = validos + rechazados; validos = unicos + duplicados.
                unicos = insertados + existentes + conflictos + fallosBD.
                Los archivos ilegibles no aportan registros: su cantidad es desconocida.
                Los duplicados NO son rechazos de validacion.
                """, e.archivosProcesados, e.archivosFallidos, e.recibidos, e.validos, e.unicos,
                e.rechazados, e.duplicados, e.porcentajeRechazados(), e.insertados, e.existentesBD,
                e.conflictosBD, e.fallosBD);
    }

    private void guardar(Path destino, String contenido) throws IOException {
        Path directorio = destino.toAbsolutePath().getParent();
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, "reporte-", ".tmp");
        try {
            Files.writeString(temporal, contenido, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino.toAbsolutePath(), StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
