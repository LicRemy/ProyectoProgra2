package com.mycompany.projectofinalventas.analisis;

import com.mycompany.projectofinalventas.datos.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/** Fuente unica de las consultas para consola y reportes. */
public class AnalizadorVentas {
    public String analizar(Connection cn) throws SQLException {
        StringBuilder reporte = new StringBuilder();
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
                    SELECT upper(sucursal) AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY upper(sucursal)
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "VENTAS POR PRODUCTO",
                    """
                    SELECT upper(codigo_producto) || ' - ' || upper(producto)
                               AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY upper(codigo_producto), upper(producto)
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "VENTAS POR VENDEDOR",
                    """
                    SELECT upper(vendedor) AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY upper(vendedor)
                    ORDER BY monto DESC, etiqueta
                    """, false);

            agregarConsulta(reporte, cn,
                    "TOP 3 VENDEDORES",
                    """
                    SELECT upper(vendedor) AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY upper(vendedor)
                    ORDER BY monto DESC, etiqueta
                    LIMIT 3
                    """, false);

            String productos = """
                    SELECT upper(codigo_producto) || ' - ' || upper(producto)
                               AS etiqueta,
                           COUNT(*) AS transacciones,
                           SUM(cantidad * precio_unitario) AS monto
                    FROM ventas
                    GROUP BY upper(codigo_producto), upper(producto)
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
        return reporte.toString();
    }

    public String analizar() throws SQLException {
        try (Connection cn = ConexionBD.conectar()) {
            cn.setReadOnly(true);
            cn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            cn.setAutoCommit(false);
            String resultado = analizar(cn);
            cn.commit();
            return resultado;
        }
    }

    public void mostrarAnalisisCompleto() throws SQLException {
        System.out.print(analizar());
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
