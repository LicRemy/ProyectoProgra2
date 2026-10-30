package com.mycompany.projectofinalventas.datos;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentaDAO {
    public enum Resultado { INSERTADA, EXISTENTE, CONFLICTO }

    public Resultado guardar(RegistroVenta venta) throws SQLException {
        try (Connection cn = ConexionBD.conectar()) {
            return guardar(cn, venta);
        }
    }

    /** La conexion prestada permite pruebas con una tabla TEMPORARY aislada. */
    public Resultado guardar(Connection cn, RegistroVenta original) throws SQLException {
        ValidadorVenta validador = new ValidadorVenta();
        RegistroVenta venta = validador.normalizar(original);
        if (!validador.validar(venta).isEmpty()) throw new SQLException("Venta no valida", "22000");
        RegistroVenta anterior = buscar(cn, venta.getIdVenta());
        if (anterior != null) return anterior.mismosDatos(venta) ? Resultado.EXISTENTE : Resultado.CONFLICTO;
        String sql = """
                INSERT INTO ventas (id_venta, fecha, sucursal, codigo_producto,
                    producto, cantidad, precio_unitario, vendedor)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_venta) DO NOTHING
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, venta.getIdVenta());
            ps.setDate(2, Date.valueOf(venta.getFecha()));
            ps.setString(3, venta.getSucursal());
            ps.setString(4, venta.getCodigoProducto());
            ps.setString(5, venta.getProducto());
            ps.setInt(6, venta.getCantidad());
            ps.setBigDecimal(7, venta.getPrecioUnitario());
            ps.setString(8, venta.getVendedor());
            if (ps.executeUpdate() == 1) return Resultado.INSERTADA;
        }
        anterior = buscar(cn, venta.getIdVenta());
        return anterior != null && anterior.mismosDatos(venta) ? Resultado.EXISTENTE : Resultado.CONFLICTO;
    }

    private RegistroVenta buscar(Connection cn, String id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT * FROM ventas WHERE upper(btrim(id_venta)) = ? ORDER BY id_venta LIMIT 1")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new RegistroVenta(rs.getString("id_venta"), rs.getDate("fecha").toLocalDate(),
                        rs.getString("sucursal"), rs.getString("codigo_producto"), rs.getString("producto"),
                        rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario"), rs.getString("vendedor"));
            }
        }
    }

    public boolean insertar(RegistroVenta venta) throws SQLException {
        return guardar(venta) == Resultado.INSERTADA;
    }

    public int contarVentas() throws SQLException {
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM ventas");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
