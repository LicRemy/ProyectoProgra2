package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class LectorCSV {
    public List<RegistroVenta> leer(String ruta, List<String> errores) {
        List<RegistroVenta> ventas = new ArrayList<>();
        List<String> incidencias = new ArrayList<>();
        CSVFormat formato = CSVFormat.RFC4180.builder()
                .setIgnoreEmptyLines(true).setIgnoreSurroundingSpaces(true).get();
        try (Reader reader = Files.newBufferedReader(Path.of(ruta), StandardCharsets.UTF_8);
             CSVParser parser = formato.parse(reader)) {
            List<String> cabecera = null;
            for (CSVRecord fila : parser) {
                if (cabecera == null) {
                    cabecera = new ArrayList<>();
                    for (String campo : fila) {
                        cabecera.add(campo.replace("\uFEFF", "").strip().toLowerCase(Locale.ROOT));
                    }
                    if (cabecera.size() != 8
                            || new HashSet<>(cabecera).size() != 8
                            || !new HashSet<>(cabecera).equals(new HashSet<>(ConversorVenta.CAMPOS))) {
                        throw new IllegalStateException(ruta + ": encabezado CSV incorrecto; se requieren "
                                + String.join(",", ConversorVenta.CAMPOS));
                    }
                    continue;
                }
                String ref = ruta + " registro CSV " + fila.getRecordNumber();
                if (fila.size() != 8) {
                    incidencias.add(ref + ": se esperaban 8 campos, recibidos " + fila.size());
                    continue;
                }
                Map<String, String> datos = new HashMap<>();
                for (int i = 0; i < cabecera.size(); i++) datos.put(cabecera.get(i), fila.get(i));
                new ConversorVenta().agregar(datos, ref, ventas, incidencias);
            }
            if (cabecera == null) throw new IllegalStateException(ruta + ": archivo CSV vacio");
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException(ruta + ": no se pudo leer CSV o hay comillas mal formadas", e);
        }
        errores.addAll(incidencias);
        return ventas;
    }
}
