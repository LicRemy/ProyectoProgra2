
package com.mycompany.projectofinalventas.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RegistroVenta {

    private String idVenta;
    private LocalDate fecha;
    private String sucursal;
    private String codigoProducto;
    private String producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String vendedor;

    public RegistroVenta(
            String idVenta,
            LocalDate fecha,
            String sucursal,
            String codigoProducto,
            String producto,
            int cantidad,
            BigDecimal precioUnitario,
            String vendedor) {

        this.idVenta = idVenta;
        this.fecha = fecha;
        this.sucursal = sucursal;
        this.codigoProducto = codigoProducto;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.vendedor = vendedor;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getSucursal() {
        return sucursal;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public String getVendedor() {
        return vendedor;
    }

    public BigDecimal calcularTotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
}
