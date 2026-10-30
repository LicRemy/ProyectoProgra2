
package com.mycompany.projectofinalventas.reportes;

import com.mycompany.projectofinalventas.datos.ConexionBD;
import com.mycompany.projectofinalventas.modelo.EstadisticasProcesamiento;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class GeneradorReportes {

    public void generar(
            EstadisticasProcesamiento e,
            List<String> errores)
            throws IOException, SQLException {

        StringBuilder reporte = new StringBuilder();

        reporte.append(
                "SISTEMA DE INTEGRACION Y ANALISIS DE VENTAS\n");
        reporte.append("=========================================\n\n");

        reporte.append("ESTADISTICAS DE ESTA EJECUCION\n");
        reporte.append("--------------------------------\n");

        reporte.append("Registros recibidos: ")
                .append(e.recibidos).append("\n");

        reporte.append("Validos antes de duplicados: ")
                .append(e.validos).append("\n");

        reporte.append("Rechazados por validacion: ")
                .append(e.rechazados).append("\n");

        reporte.append("Duplicados entre archivos: ")
                .append(e.duplicados).append("\n");

        reporte.append("Registros unicos: ")
                .append(e.unicos).append("\n");

        reporte.append(String.format(Locale.ROOT,
                "Porcentaje de rechazados: %.2f%%%n",
                e.porcentajeRechazados()));

        reporte.append("Insertados en PostgreSQL: ")
                .append(e.insertados).append("\n");

        reporte.append("Ya existentes en PostgreSQL: ")
                .append(e.existentesBD).append("\n");

        reporte.append("Fallos de insercion SQL: ")
                .append(e.fallosBD).append("\n");

        reporte.append("Archivos fallidos: ")
                .append(e.archivosFallidos).append("\n\n");

        // Consultas sobre el contenido consolidado de PostgreSQL
        try (Connection cn = ConexionBD.conectar()) {

            agregarConsulta(reporte, cn,
                    "RESUMEN GENERAL",
                    """
                    SELECT COUNT(*) AS cantidad,
                           COALESCE(SUM(cantidad * precio_unitario), 0)
                               AS monto,
                           COALESCE(AVG(cantidad * precio_unitario), 0)
                               AS promedio
                    FROM ventas
                    """,
                    true);

            agregarConsulta(reporte, cn,
                    "VENTAS POR MES",
                    """
                    SELECT TO_CHAR(fecha, 'YYYY-MM') AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY TO_CHAR(fecha, 'YYYY-MM')
                    ORDER BY etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "VENTAS POR SUCURSAL",
                    """
                    SELECT sucursal AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY sucursal
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "VENTAS POR PRODUCTO",
                    """
                    SELECT codigo_producto || ' - ' || producto
                               AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY codigo_producto, producto
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "VENTAS POR VENDEDOR",
                    """
                    SELECT vendedor AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY vendedor
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "TOP 3 VENDEDORES",
                    """
                    SELECT vendedor AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY vendedor
                    ORDER BY monto DESC, etiqueta
                    LIMIT 3
                    """, false);

            String productos = """
                    SELECT codigo_producto || ' - ' || producto
                               AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY codigo_producto, producto
                    """;

            agregarConsulta(reporte, cn,
                    "PRODUCTO CON MAYOR MONTO",
                    productos + " ORDER BY monto DESC, etiqueta LIMIT 1",
                    false);

            agregarConsulta(reporte, cn,
                    "PRODUCTO CON MENOR MONTO",
                    productos + " ORDER BY monto ASC, etiqueta LIMIT 1",
                    false);

            String transacciones = """
                    SELECT id_venta AS etiqueta,
                           1 AS transacciones,
                           cantidad * precio_unitario AS monto
                    FROM ventas
                    """;

            agregarConsulta(reporte, cn,
                    "TRANSACCION DE MAYOR MONTO",
                    transacciones
                            + " ORDER BY monto DESC, etiqueta LIMIT 1",
                    false);

            agregarConsulta(reporte, cn,
                    "TRANSACCION DE MENOR MONTO",
                    transacciones
                            + " ORDER BY monto ASC, etiqueta LIMIT 1",
                    false);
        }

        reporte.append("\nCONCLUSIONES DE LOS ESTUDIANTES\n");
        reporte.append("--------------------------------\n");
        reporte.append(
                "PENDIENTE: redactar tres conclusiones "
                + "por cada estudiante basadas en los datos reales.\n");

        // No dejar resultados incompletos ante un error SQL
        Files.writeString(
                Path.of("reporte_analisis.txt"),
                reporte.toString(),
                StandardCharsets.UTF_8);

        Files.write(
                Path.of("errores_procesamiento.txt"),
                errores,
                StandardCharsets.UTF_8);
    }

    private void agregarConsulta(
            StringBuilder reporte,
            Connection cn,
            String titulo,
            String sql,
            boolean resumen)
            throws SQLException {

        reporte.append("\n").append(titulo).append("\n");
        reporte.append("--------------------------------\n");

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean contieneDatos = false;

            while (rs.next()) {
                contieneDatos = true;

                if (resumen) {
                    reporte.append("Transacciones almacenadas: ")
                            .append(rs.getLong("cantidad"))
                            .append("\n");

                    reporte.append("Monto total: ")
                            .append(dinero(rs.getBigDecimal("monto")))
                            .append("\n");

                    reporte.append("Promedio por transaccion: ")
                            .append(dinero(rs.getBigDecimal("promedio")))
                            .append("\n");

                } else {
                    reporte.append(rs.getString("etiqueta"))
                            .append(" | Transacciones: ")
                            .append(rs.getLong("transacciones"))
                            .append(" | Monto: ")
                            .append(dinero(rs.getBigDecimal("monto")))
                            .append("\n");
                }
            }

            if (!contieneDatos) {
                reporte.append("Sin registros.\n");
            }
        }
    }

    private String dinero(BigDecimal valor) {
        return "Q" + String.format(
                Locale.ROOT, "%.2f", valor);
    }
}
