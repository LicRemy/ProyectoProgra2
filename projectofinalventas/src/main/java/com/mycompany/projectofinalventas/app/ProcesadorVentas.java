package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.datos.VentaDAO;
import com.mycompany.projectofinalventas.lectores.*;
import com.mycompany.projectofinalventas.modelo.*;
import com.mycompany.projectofinalventas.validacion.DeduplicadorVentas;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ProcesadorVentas {
    public List<RegistroVenta> leer(List<Path> archivos, EstadisticasProcesamiento e, List<String> errores) {
        List<RegistroVenta> ventas = new ArrayList<>();
        for (Path archivo : archivos) {
            List<String> rechazos = new ArrayList<>();
            try {
                String ruta = archivo.toString();
                List<RegistroVenta> leidas = switch (extension(archivo)) {
                    case "csv" -> new LectorCSV().leer(ruta, rechazos);
                    case "json" -> new LectorJSON().leer(ruta, rechazos);
                    case "xml" -> new LectorXML().leer(ruta, rechazos);
                    default -> throw new IllegalStateException(ruta + ": extension no admitida");
                };
                ventas.addAll(leidas);
                errores.addAll(rechazos);
                e.validos += leidas.size();
                e.rechazados += rechazos.size();
                e.recibidos += leidas.size() + rechazos.size();
                e.archivosProcesados++;
                System.out.println(archivo.getFileName() + " procesado.");
            } catch (IllegalStateException ex) {
                e.archivosFallidos++;
                errores.add(ex.getMessage());
            }
        }
        DeduplicadorVentas deduplicador = new DeduplicadorVentas();
        List<RegistroVenta> unicas = deduplicador.eliminarDuplicados(ventas, errores);
        e.duplicados = deduplicador.getDuplicados();
        e.unicos = unicas.size();
        return unicas;
    }

    public void guardar(List<RegistroVenta> ventas, EstadisticasProcesamiento e, List<String> errores) {
        VentaDAO dao = new VentaDAO();
        for (RegistroVenta venta : ventas) {
            try {
                switch (dao.guardar(venta)) {
                    case INSERTADA -> e.insertados++;
                    case EXISTENTE -> e.existentesBD++;
                    case CONFLICTO -> {
                        e.conflictosBD++;
                        errores.add(venta.getOrigen() + " ID=" + venta.getIdVenta()
                                + ": CONTRADICCION con PostgreSQL; se conserva el dato almacenado");
                    }
                }
            } catch (SQLException ex) {
                e.fallosBD++;
                errores.add(venta.getOrigen() + " ID=" + venta.getIdVenta()
                        + ": fallo JDBC, SQLState=" + ex.getSQLState());
            }
        }
    }

    public List<Path> descubrir(Path entrada) throws IOException {
        if (!Files.isDirectory(entrada)) throw new IOException("No existe el directorio de entrada");
        try (Stream<Path> rutas = Files.list(entrada)) {
            return rutas.filter(Files::isRegularFile)
                    .filter(p -> List.of("csv", "json", "xml").contains(extension(p)))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    private static String extension(Path ruta) {
        String nombre = ruta.getFileName().toString().toLowerCase(Locale.ROOT);
        return nombre.substring(nombre.lastIndexOf('.') + 1);
    }
}
