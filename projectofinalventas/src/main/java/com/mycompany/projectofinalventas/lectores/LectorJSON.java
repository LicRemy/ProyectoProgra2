package com.mycompany.projectofinalventas.lectores;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LectorJSON {
    public List<RegistroVenta> leer(String ruta, List<String> errores) {
        List<RegistroVenta> ventas = new ArrayList<>();
        ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
        final JsonNode raiz;
        try {
            raiz = mapper.readTree(Path.of(ruta).toFile());
        } catch (IOException e) {
            // No incluir el contenido del archivo dentro del mensaje.
            throw new IllegalStateException(ruta + ": JSON mal formado o archivo ilegible", e);
        }
        if (raiz == null || !raiz.isArray()) {
            throw new IllegalStateException(ruta + ": JSON vacio o raiz incorrecta; se requiere un arreglo");
        }
        int numero = 0;
        for (JsonNode nodo : raiz) {
            String ref = ruta + " registro JSON " + (++numero);
            if (!nodo.isObject()) {
                errores.add(ref + ": se esperaba un objeto de venta");
                continue;
            }
            Map<String, String> datos = new HashMap<>();
            boolean estructuraValida = true;
            var campos = nodo.properties().iterator();
            while (campos.hasNext()) {
                var campo = campos.next();
                String clave = campo.getKey().strip().toLowerCase(Locale.ROOT);
                if (!ConversorVenta.CAMPOS.contains(clave) || datos.containsKey(clave)) {
                    estructuraValida = false;
                    continue;
                }
                JsonNode valor = campo.getValue();
                if (!valor.isNull() && !valor.isTextual() && !valor.isNumber()) estructuraValida = false;
                datos.put(clave, valor.isNull() ? "" : valor.asText());
            }
            if (!estructuraValida) {
                errores.add(ref + ": campos desconocidos, repetidos o valores no escalares");
            } else {
                new ConversorVenta().agregar(datos, ref, ventas, errores);
            }
        }
        return ventas;
    }
}
