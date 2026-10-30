
package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class LectorCSV {

    private final ValidadorVenta validador =
            new ValidadorVenta();

    public List<RegistroVenta> leer(
            String ruta, List<String> errores) {

        List<RegistroVenta> ventas = new ArrayList<>();
        Path archivo = Path.of(ruta);

        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                // Primera fila: encabezado
                if (numeroLinea == 1) {
                    continue;
                }

                if (linea.isBlank()) {
                    continue;
                }

                // Este lector admite CSV sencillo
                String[] datos = linea.split(",", -1);

                if (datos.length != 8) {
                    errores.add(ruta + " linea " + numeroLinea
                            + ": se esperaban 8 campos");
                    continue;
                }

                try {
                    RegistroVenta original = new RegistroVenta(
                            datos[0],
                            LocalDate.parse(datos[1].trim()),
                            datos[2],
                            datos[3],
                            datos[4],
                            Integer.parseInt(datos[5].trim()),
                            new BigDecimal(datos[6].trim()),
                            datos[7]
                    );

                    RegistroVenta normalizada =
                            validador.normalizar(original);

                    List<String> problemas =
                            validador.validar(normalizada);

                    if (problemas.isEmpty()) {
                        ventas.add(normalizada);
                    } else {
                        errores.add(ruta + " linea " + numeroLinea
                                + " ID=" + datos[0].trim()
                                + ": " + String.join("; ", problemas));
                    }

                } catch (DateTimeParseException e) {
                    errores.add(ruta + " linea " + numeroLinea
                            + " ID=" + datos[0].trim()
                            + ": fecha invalida");

                } catch (NumberFormatException e) {
                    errores.add(ruta + " linea " + numeroLinea
                            + " ID=" + datos[0].trim()
                            + ": cantidad o precio invalido");
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer " + ruta, e);
        }

        return ventas;
    }
}
