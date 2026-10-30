
package com.mycompany.projectofinalventas.datos;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentaDAO {

    public boolean insertar(RegistroVenta venta)
            throws SQLException {

        String sql = """
            INSERT INTO ventas (
                id_venta, fecha, sucursal, codigo_producto,
                producto, cantidad, precio_unitario, vendedor
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id_venta) DO NOTHING
            """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, venta.getIdVenta());
            ps.setDate(2, Date.valueOf(venta.getFecha()));
            ps.setString(3, venta.getSucursal());
            ps.setString(4, venta.getCodigoProducto());
            ps.setString(5, venta.getProducto());
            ps.setInt(6, venta.getCantidad());
            ps.setBigDecimal(7, venta.getPrecioUnitario());
            ps.setString(8, venta.getVendedor());

            return ps.executeUpdate() == 1;
        }
    }

    public int contarVentas() throws SQLException {

        String sql = "SELECT COUNT(*) FROM ventas";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }
}
