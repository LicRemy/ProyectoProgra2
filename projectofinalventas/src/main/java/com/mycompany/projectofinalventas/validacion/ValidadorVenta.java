
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

        if (venta.getFecha() != null
                && (venta.getFecha().getYear() < 1 || venta.getFecha().getYear() > 9999)) {
            errores.add("Fecha fuera del rango 0001-9999");
        }
        longitud(venta.getIdVenta(), 50, "id_venta", errores);
        longitud(venta.getSucursal(), 100, "sucursal", errores);
        longitud(venta.getCodigoProducto(), 50, "codigo_producto", errores);
        longitud(venta.getProducto(), 150, "producto", errores);
        longitud(venta.getVendedor(), 100, "vendedor", errores);
        if (precio != null && (precio.stripTrailingZeros().scale() > 2
                || precio.abs().compareTo(new BigDecimal("9999999999.99")) > 0)) {
            errores.add("Precio incompatible con NUMERIC(12,2); no se redondea automaticamente");
        }

        return errores;
    }

    // Comprueba si un texto esta vacio
    private void longitud(String texto, int limite, String campo, List<String> errores) {
        if (texto != null && texto.codePointCount(0, texto.length()) > limite)
            errores.add(campo + " excede " + limite + " caracteres");
    }

    private boolean vacio(String texto) {
        return texto == null || texto.strip().isEmpty();
    }

    // Limpia textos sin cambiar cantidades o fechas
    public RegistroVenta normalizar(RegistroVenta venta) {

        if (venta == null) return null;
        RegistroVenta resultado = new RegistroVenta(
                limpiarMayusculas(venta.getIdVenta()),
                venta.getFecha(),
                limpiarMayusculas(venta.getSucursal()),
                limpiarMayusculas(venta.getCodigoProducto()),
                limpiarEspacios(venta.getProducto()),
                venta.getCantidad(),
                venta.getPrecioUnitario(),
                limpiarEspacios(venta.getVendedor())
        );
        resultado.setOrigen(venta.getOrigen());
        return resultado;
    }

    private String limpiarEspacios(String texto) {
        if (texto == null) {
            return null;
        }

        return texto.replaceAll("(?U)\\s+", " ").strip();
    }

    private String limpiarMayusculas(String texto) {
        String limpio = limpiarEspacios(texto);

        if (limpio == null) {
            return null;
        }

        return limpio.toUpperCase(Locale.ROOT);
    }
}
