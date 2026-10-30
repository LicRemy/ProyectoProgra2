
package com.mycompany.projectofinalventas.validacion;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ValidadorVenta {

    // Devuelve los errores encontrados
    public List<String> validar(RegistroVenta venta) {

        List<String> errores = new ArrayList<>();

        if (venta == null) {
            errores.add("Registro nulo");
            return errores;
        }

        if (vacio(venta.getIdVenta())) {
            errores.add("ID de venta vacio");
        }

        if (venta.getFecha() == null) {
            errores.add("Fecha vacia o invalida");
        }

        if (vacio(venta.getSucursal())) {
            errores.add("Sucursal vacia");
        }

        if (vacio(venta.getCodigoProducto())) {
            errores.add("Codigo de producto vacio");
        }

        if (vacio(venta.getProducto())) {
            errores.add("Nombre de producto vacio");
        }

        if (venta.getCantidad() <= 0) {
            errores.add("Cantidad debe ser mayor que cero");
        }

        BigDecimal precio = venta.getPrecioUnitario();

        if (precio == null
                || precio.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("Precio debe ser mayor que cero");
        }

        if (vacio(venta.getVendedor())) {
            errores.add("Vendedor vacio");
        }

        return errores;
    }

    // Comprueba si un texto esta vacio
    private boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    // Limpia textos sin cambiar cantidades o fechas
    public RegistroVenta normalizar(RegistroVenta venta) {

        return new RegistroVenta(
                limpiarMayusculas(venta.getIdVenta()),
                venta.getFecha(),
                limpiarMayusculas(venta.getSucursal()),
                limpiarMayusculas(venta.getCodigoProducto()),
                limpiarEspacios(venta.getProducto()),
                venta.getCantidad(),
                venta.getPrecioUnitario(),
                limpiarEspacios(venta.getVendedor())
        );
    }

    private String limpiarEspacios(String texto) {
        if (texto == null) {
            return null;
        }

        return texto.trim().replaceAll("\\s+", " ");
    }

    private String limpiarMayusculas(String texto) {
        String limpio = limpiarEspacios(texto);

        if (limpio == null) {
            return null;
        }

        return limpio.toUpperCase(Locale.ROOT);
    }
}
