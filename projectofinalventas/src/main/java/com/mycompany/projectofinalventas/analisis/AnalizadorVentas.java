
package com.mycompany.projectofinalventas.analisis;

import com.mycompany.projectofinalventas.datos.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalizadorVentas {

    // 1. RESUMEN GENERAL
    public void mostrarResumen() throws SQLException {

        String sql = """
            SELECT
                COUNT(*) AS transacciones,
                COALESCE(SUM(cantidad * precio_unitario), 0)
                    AS total,
                COALESCE(AVG(cantidad * precio_unitario), 0)
                    AS promedio
            FROM ventas
            """;

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("\n=== RESUMEN GENERAL ===");
                System.out.println("Transacciones: "
                        + rs.getInt("transacciones"));

                imprimirDinero("Total vendido",
                        rs.getBigDecimal("total"));

                imprimirDinero("Promedio por transaccion",
                        rs.getBigDecimal("promedio"));
            }
        }
    }

    // 2. MOSTRAR TABLAS AGRUPADAS
    private void mostrarAgrupacion(String titulo, String sql)
            throws SQLException {

        System.out.println("\n=== " + titulo + " ===");

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;

                System.out.println(
                        rs.getString("etiqueta")
                        + " | Ventas: "
                        + rs.getLong("transacciones")
                        + " | Monto: Q"
                        + rs.getBigDecimal("monto"));
            }

            if (!hayDatos) {
                System.out.println("Sin registros.");
            }
        }
    }

    // 3. VENTAS POR MES
    public void mostrarPorMes() throws SQLException {

        String sql = """
            SELECT
                TO_CHAR(fecha, 'YYYY-MM') AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY TO_CHAR(fecha, 'YYYY-MM')
            ORDER BY etiqueta
            """;

        mostrarAgrupacion("VENTAS POR MES", sql);
    }

    // 4. VENTAS POR SUCURSAL
    public void mostrarPorSucursal() throws SQLException {

        String sql = """
            SELECT
                sucursal AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY sucursal
            ORDER BY monto DESC, etiqueta
            """;

        mostrarAgrupacion("VENTAS POR SUCURSAL", sql);
    }

    // 5. VENTAS POR PRODUCTO
    public void mostrarPorProducto() throws SQLException {

        String sql = """
            SELECT
                codigo_producto || ' - ' || producto AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY codigo_producto, producto
            ORDER BY monto DESC, etiqueta
            """;

        mostrarAgrupacion("VENTAS POR PRODUCTO", sql);
    }

    // 6. VENTAS POR VENDEDOR
    public void mostrarPorVendedor() throws SQLException {

        String sql = """
            SELECT
                vendedor AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY vendedor
            ORDER BY monto DESC, etiqueta
            """;

        mostrarAgrupacion("VENTAS POR VENDEDOR", sql);
    }

    // 7. TOP 3 VENDEDORES
    public void mostrarTopVendedores() throws SQLException {

        String sql = """
            SELECT
                vendedor AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY vendedor
            ORDER BY monto DESC, etiqueta
            LIMIT 3
            """;

        mostrarAgrupacion("TOP 3 VENDEDORES", sql);
    }

    // 8. PRODUCTOS EXTREMOS POR MONTO
    public void mostrarProductosExtremos() throws SQLException {

        String base = """
            SELECT
                codigo_producto || ' - ' || producto AS etiqueta,
                COUNT(*) AS transacciones,
                SUM(cantidad * precio_unitario) AS monto
            FROM ventas
            GROUP BY codigo_producto, producto
            """;

        mostrarExtremo("PRODUCTO MAYOR MONTO",
                base + " ORDER BY monto DESC, etiqueta LIMIT 1");

        mostrarExtremo("PRODUCTO MENOR MONTO",
                base + " ORDER BY monto ASC, etiqueta LIMIT 1");
    }

    // 9. TRANSACCIONES EXTREMAS
    public void mostrarTransaccionesExtremas()
            throws SQLException {

        String base = """
            SELECT
                id_venta AS etiqueta,
                1 AS transacciones,
                cantidad * precio_unitario AS monto
            FROM ventas
            """;

        mostrarExtremo("TRANSACCION MAYOR MONTO",
                base + " ORDER BY monto DESC, etiqueta LIMIT 1");

        mostrarExtremo("TRANSACCION MENOR MONTO",
                base + " ORDER BY monto ASC, etiqueta LIMIT 1");
    }

    private void mostrarExtremo(String titulo, String sql)
            throws SQLException {

        System.out.println("\n=== " + titulo + " ===");

        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("Registro: "
                        + rs.getString("etiqueta"));

                imprimirDinero("Monto",
                        rs.getBigDecimal("monto"));
            } else {
                System.out.println("Sin registros.");
            }
        }
    }

    private void imprimirDinero(
            String descripcion, BigDecimal valor) {

        System.out.printf("%s: Q%.2f%n", descripcion, valor);
    }

    // EJECUTAR TODOS LOS ANALISIS
    public void mostrarAnalisisCompleto()
            throws SQLException {

        mostrarResumen();
        mostrarPorMes();
        mostrarPorSucursal();
        mostrarPorProducto();
        mostrarPorVendedor();
        mostrarProductosExtremos();
        mostrarTransaccionesExtremas();
        mostrarTopVendedores();
    }
}
