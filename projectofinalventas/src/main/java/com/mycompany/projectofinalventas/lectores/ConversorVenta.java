package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Las reglas de conversion son las mismas para los tres formatos. */
public class ConversorVenta {
    public static final List<String> CAMPOS = List.of("id_venta", "fecha", "sucursal",
            "codigo_producto", "producto", "cantidad", "precio_unitario", "vendedor");

    public void agregar(Map<String, String> datos, String referencia,
            List<RegistroVenta> ventas, List<String> errores) {
        List<String> problemas = new ArrayList<>();
        LocalDate fecha = null;
        int cantidad = 0;
        BigDecimal precio = null;
        try {
            fecha = LocalDate.parse(texto(datos, "fecha"));
        } catch (DateTimeParseException e) {
            problemas.add("fecha invalida: se requiere yyyy-MM-dd y una fecha real");
        }
        try {
            cantidad = Integer.parseInt(texto(datos, "cantidad"));
        } catch (NumberFormatException e) {
            problemas.add("cantidad invalida: se requiere un entero de 32 bits");
        }
        try {
            precio = new BigDecimal(texto(datos, "precio_unitario"));
        } catch (NumberFormatException e) {
            problemas.add("precio_unitario invalido: se requiere un decimal con punto");
        }
        RegistroVenta venta = new RegistroVenta(texto(datos, "id_venta"), fecha,
                texto(datos, "sucursal"), texto(datos, "codigo_producto"),
                texto(datos, "producto"), cantidad, precio, texto(datos, "vendedor"));
        ValidadorVenta validador = new ValidadorVenta();
        venta = validador.normalizar(venta);
        venta.setOrigen(referencia);
        problemas.addAll(validador.validar(venta));
        if (problemas.isEmpty()) {
            ventas.add(venta);
        } else {
            errores.add(referencia + " ID=" + venta.getIdVenta() + ": "
                    + String.join("; ", problemas));
        }
    }

    private String texto(Map<String, String> datos, String campo) {
        return datos.getOrDefault(campo, "").strip();
    }
}
