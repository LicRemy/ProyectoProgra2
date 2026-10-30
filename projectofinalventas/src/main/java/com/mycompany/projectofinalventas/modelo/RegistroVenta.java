
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
    private String origen = "origen no disponible";

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

    public String getOrigen() { return origen; }

    public void setOrigen(String origen) { this.origen = origen; }

    /** Compara el contenido sin confundir escala decimal o mayusculas con cambios. */
    public boolean mismosDatos(RegistroVenta otra) {
        return otra != null && igual(idVenta, otra.idVenta)
                && java.util.Objects.equals(fecha, otra.fecha)
                && igual(sucursal, otra.sucursal)
                && igual(codigoProducto, otra.codigoProducto)
                && igual(producto, otra.producto) && cantidad == otra.cantidad
                && precioUnitario.compareTo(otra.precioUnitario) == 0
                && igual(vendedor, otra.vendedor);
    }

    private static boolean igual(String a, String b) {
        return a != null && b != null && a.strip().replaceAll("(?U)\\s+", " ")
                .equalsIgnoreCase(b.strip().replaceAll("(?U)\\s+", " "));
    }

    public BigDecimal calcularTotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
}
