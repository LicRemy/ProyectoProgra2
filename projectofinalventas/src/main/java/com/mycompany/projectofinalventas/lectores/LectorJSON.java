
package com.mycompany.projectofinalventas.lectores;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LectorJSON {

    private final ValidadorVenta validador =
            new ValidadorVenta();

    public List<RegistroVenta> leer(
            String ruta, List<String> errores) {

        List<RegistroVenta> ventas = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode raiz = mapper.readTree(Path.of(ruta).toFile());

            if (raiz == null || !raiz.isArray()) {
                throw new IllegalStateException(
                        "El JSON debe contener una lista de ventas");
            }

            int numero = 0;

            for (JsonNode nodo : raiz) {
                numero++;

                String referencia = ruta + " registro " + numero;

                try {
                    if (!nodo.isObject()) {
                        throw new IllegalArgumentException(
                                "El registro no es un objeto");
                    }

                    RegistroVenta original = new RegistroVenta(
                            texto(nodo, "id_venta"),
                            LocalDate.parse(texto(nodo, "fecha").trim()),
                            texto(nodo, "sucursal"),
                            texto(nodo, "codigo_producto"),
                            texto(nodo, "producto"),
                            Integer.parseInt(
                                    texto(nodo, "cantidad").trim()),
                            new BigDecimal(
                                    texto(nodo, "precio_unitario").trim()),
                            texto(nodo, "vendedor")
                    );

                    RegistroVenta normalizada =
                            validador.normalizar(original);

                    List<String> problemas =
                            validador.validar(normalizada);

                    if (problemas.isEmpty()) {
                        ventas.add(normalizada);
                    } else {
                        errores.add(referencia + " ID="
                                + normalizada.getIdVenta()
                                + ": " + String.join("; ", problemas));
                    }

                } catch (RuntimeException e) {
                    errores.add(referencia + ": "
                            + "datos mal formados: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al leer JSON " + ruta, e);
        }

        return ventas;
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);

        if (valor == null || valor.isNull()
                || !valor.isValueNode()) {
            return "";
        }

        return valor.asText();
    }
}
