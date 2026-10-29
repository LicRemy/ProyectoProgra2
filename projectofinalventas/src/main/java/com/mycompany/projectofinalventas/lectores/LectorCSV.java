
package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class LectorCSV {

    public ArrayList<RegistroVenta> leer(String ruta) {

        ArrayList<RegistroVenta> ventas = new ArrayList<>();

        Path archivo = Path.of(ruta);

        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {

                numeroLinea++;

                // La primera fila contiene los encabezados
                if (numeroLinea == 1) {
                    continue;
                }

                // Ignorar filas completamente vacias
                if (linea.isBlank()) {
                    continue;
                }

                // Separar los ocho campos
                String[] datos = linea.split(",", -1);

                if (datos.length != 8) {
                    System.out.println(
                            "Error en linea " + numeroLinea
                            + ": se esperaban 8 campos.");
                    continue;
                }

                try {

                    String id = datos[0].trim();

                    LocalDate fecha = LocalDate.parse(
                            datos[1].trim());

                    String sucursal = datos[2].trim();

                    String codigo = datos[3].trim();

                    String producto = datos[4].trim();

                    int cantidad = Integer.parseInt(
                            datos[5].trim());

                    BigDecimal precio = new BigDecimal(
                            datos[6].trim());

                    String vendedor = datos[7].trim();

                    RegistroVenta venta = new RegistroVenta(
                            id,
                            fecha,
                            sucursal,
                            codigo,
                            producto,
                            cantidad,
                            precio,
                            vendedor
                    );

                    ventas.add(venta);

                } catch (DateTimeParseException e) {

                    System.out.println(
                            "Fecha incorrecta en linea "
                            + numeroLinea);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Numero incorrecto en linea "
                            + numeroLinea);
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error al leer archivo " + ruta
                    + ": " + e.getMessage());
        }

        return ventas;
    }
}
