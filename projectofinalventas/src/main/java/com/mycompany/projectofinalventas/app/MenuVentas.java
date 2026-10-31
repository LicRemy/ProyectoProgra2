package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.analisis.AnalizadorVentas;
import com.mycompany.projectofinalventas.datos.ConexionBD;
import com.mycompany.projectofinalventas.datos.VentaDAO;
import com.mycompany.projectofinalventas.modelo.EstadisticasProcesamiento;
import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.reportes.GeneradorReportes;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Interfaz de consola. Reutiliza la logica existente del proyecto. */
public class MenuVentas {

    private final Scanner teclado = new Scanner(System.in);
    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProcesadorVentas procesador = new ProcesadorVentas();
    private final GeneradorReportes reportes = new GeneradorReportes();
    private final AnalizadorVentas analizador = new AnalizadorVentas();

    // Contadores solo de la ultima importacion realizada durante esta sesion.
    private EstadisticasProcesamiento ultimaImportacion = new EstadisticasProcesamiento();
    private List<String> ultimosErrores = new ArrayList<>();
    private boolean importacionRealizada = false;

    public void iniciar() {
        while (true) {
            imprimirMenu();
            String opcion = leerLinea("Seleccione una opcion: ");
            if (opcion == null) {
                System.out.println("\nEntrada cerrada. Fin del programa.");
                return;
            }
            switch (opcion) {
                case "1" -> registrarVenta();
                case "2" -> consultarVentas();
                case "3" -> buscarVenta();
                case "4" -> importarArchivos();
                case "5" -> mostrarAnalisis();
                case "6" -> generarReportes();
                case "7" -> mostrarEstadisticas();
                case "8" -> probarConexion();
                case "0" -> {
                    System.out.println("Programa finalizado.");
                    return;
                }
                default -> System.out.println("Opcion incorrecta. Elija un numero del 0 al 8.");
            }
        }
    }

    private void imprimirMenu() {
        System.out.println("\n===============================================");
        System.out.println("    SISTEMA DE INTEGRACION Y ANALISIS DE VENTAS");
        System.out.println("===============================================");
        System.out.println("  1. Registrar una venta manualmente");
        System.out.println("  2. Consultar las ultimas 20 ventas");
        System.out.println("  3. Buscar una venta por ID");
        System.out.println("  4. Importar archivos CSV, JSON y XML");
        System.out.println("  5. Mostrar analisis estadisticos");
        System.out.println("  6. Generar reportes TXT");
        System.out.println("  7. Ver estadisticas de procesamiento");
        System.out.println("  8. Probar conexion con PostgreSQL");
        System.out.println("  0. Salir");
        System.out.println("===============================================");
        if (Boolean.getBoolean("soloLectura")) {
            System.out.println("AVISO: MODO SOLO LECTURA ACTIVO");
        }
    }

    /** Lee texto completo; evita mezclar nextInt() con nextLine(). */
    private String leerLinea(String etiqueta) {
        System.out.print(etiqueta);
        if (!teclado.hasNextLine()) {
            return null;
        }
        return teclado.nextLine().strip();
    }

    private void registrarVenta() {
        if (Boolean.getBoolean("soloLectura")) {
            System.out.println("No puede registrar ventas en modo solo lectura.");
            return;
        }
        System.out.println("\n--- REGISTRAR VENTA ---");
        System.out.println("Escriba CANCELAR en cualquier campo para volver al menu.");
        try {
            String id = pedir("ID de venta: ");
            LocalDate fecha = LocalDate.parse(pedir("Fecha (AAAA-MM-DD): "));
            String sucursal = pedir("Sucursal: ");
            String codigo = pedir("Codigo de producto: ");
            String producto = pedir("Nombre del producto: ");
            int cantidad = Integer.parseInt(pedir("Cantidad (entero positivo): "));
            BigDecimal precio = new BigDecimal(pedir("Precio unitario en Q (ejemplo 25.50): "));
            String vendedor = pedir("Vendedor: ");

            RegistroVenta original = new RegistroVenta(id, fecha, sucursal, codigo,
                    producto, cantidad, precio, vendedor);
            ValidadorVenta validador = new ValidadorVenta();
            RegistroVenta venta = validador.normalizar(original);
            List<String> problemas = validador.validar(venta);
            if (!problemas.isEmpty()) {
                System.out.println("Venta no guardada. Revise los datos:");
                problemas.forEach(motivo -> System.out.println(" - " + motivo));
                return;
            }

            switch (ventaDAO.guardar(venta)) {
                case INSERTADA -> System.out.println("Venta registrada correctamente. Total: Q"
                        + venta.calcularTotal().toPlainString());
                case EXISTENTE -> System.out.println("La venta ya estaba registrada con los mismos datos.");
                case CONFLICTO -> System.out.println("El ID ya existe, pero sus datos son diferentes."
                        + " No se modifico la venta almacenada.");
            }
        } catch (Cancelacion ex) {
            System.out.println("Registro cancelado. No se guardaron cambios.");
        } catch (DateTimeParseException ex) {
            System.out.println("Fecha incorrecta. Use AAAA-MM-DD; ejemplo: 2026-10-31.");
        } catch (NumberFormatException ex) {
            System.out.println("Cantidad o precio incorrecto. Cantidad entera; precio decimal con punto.");
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private String pedir(String etiqueta) {
        String respuesta = leerLinea(etiqueta);
        if (respuesta == null || respuesta.equalsIgnoreCase("CANCELAR")) {
            throw new Cancelacion();
        }
        return respuesta;
    }

    private void consultarVentas() {
        System.out.println("\n--- ULTIMAS 20 VENTAS REGISTRADAS ---");
        String sql = """
                SELECT id_venta, fecha, sucursal, codigo_producto, producto,
                       cantidad, precio_unitario, vendedor,
                       cantidad * precio_unitario AS total
                FROM ventas
                ORDER BY fecha DESC, id_venta DESC
                LIMIT 20
                """;
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int total = 0;
            while (rs.next()) {
                total++;
                imprimirVenta(rs);
            }
            if (total == 0) {
                System.out.println("No hay ventas guardadas en la base de datos.");
            } else {
                System.out.println("Mostradas: " + total + " (maximo 20).");
            }
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void buscarVenta() {
        String id = leerLinea("\nIngrese el ID a buscar: ");
        if (id == null || id.isBlank()) {
            System.out.println("Debe escribir un ID de venta.");
            return;
        }
        String sql = """
                SELECT id_venta, fecha, sucursal, codigo_producto, producto,
                       cantidad, precio_unitario, vendedor,
                       cantidad * precio_unitario AS total
                FROM ventas
                WHERE upper(btrim(id_venta)) = ?
                ORDER BY id_venta
                LIMIT 1
                """;
        try (Connection cn = ConexionBD.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    imprimirVenta(rs);
                } else {
                    System.out.println("No se encontro una venta con ese ID.");
                }
            }
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void imprimirVenta(ResultSet rs) throws SQLException {
        System.out.printf("%nID: %s | Fecha: %s | Sucursal: %s%n",
                rs.getString("id_venta"), rs.getDate("fecha"), rs.getString("sucursal"));
        System.out.printf("Producto: %s (%s) | Cantidad: %d | Precio: Q%s%n",
                rs.getString("producto"), rs.getString("codigo_producto"),
                rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario").toPlainString());
        System.out.printf("Vendedor: %s | Total: Q%s%n",
                rs.getString("vendedor"), rs.getBigDecimal("total").toPlainString());
    }

    private void importarArchivos() {
        System.out.println("\n--- IMPORTAR CSV / JSON / XML ---");
        String predeterminada = System.getProperty("entrada", "entrada");
        String carpeta = leerLinea("Carpeta de archivos [Enter = " + predeterminada + "]: ");
        if (carpeta == null) {
            System.out.println("Operacion cancelada.");
            return;
        }
        if (carpeta.isBlank()) {
            carpeta = predeterminada;
        }
        try {
            // Si no hay base de datos accesible, avisar ANTES de intentar la carga.
            ventaDAO.contarVentas();
            List<Path> archivos = procesador.descubrir(Path.of(carpeta));
            if (archivos.isEmpty()) {
                System.out.println("No hay archivos CSV, JSON o XML en esa carpeta.");
                return;
            }
            EstadisticasProcesamiento estadisticas = new EstadisticasProcesamiento();
            List<String> errores = new ArrayList<>();
            var ventas = procesador.leer(archivos, estadisticas, errores);
            procesador.guardar(ventas, estadisticas, errores);
            estadisticas.comprobar();

            ultimaImportacion = estadisticas;
            ultimosErrores = errores;
            importacionRealizada = true;
            System.out.println("\nProcesamiento terminado. Resultado:");
            mostrarResumen(estadisticas);
            if (estadisticas.archivosFallidos + estadisticas.fallosBD
                    + estadisticas.conflictosBD + estadisticas.rechazados > 0) {
                System.out.println("Hay incidencias. Use la opcion 6 para generar errores_procesamiento.txt.");
            }
        } catch (IOException | InvalidPathException ex) {
            System.out.println("No se pudo leer la carpeta: " + ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        } catch (IllegalStateException ex) {
            System.out.println("Error de integridad en los contadores: " + ex.getMessage());
        }
    }

    private void mostrarAnalisis() {
        System.out.println("\n--- ANALISIS ESTADISTICO (POSTGRESQL) ---");
        try {
            analizador.mostrarAnalisisCompleto();
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void generarReportes() {
        System.out.println("\n--- GENERAR ARCHIVOS TXT ---");
        if (!importacionRealizada) {
            System.out.println("No se han importado archivos en esta sesion.");
            System.out.println("Los contadores de ejecucion seran 0; el analisis SQL reflejara"
                    + " las ventas ya almacenadas en PostgreSQL.");
        }
        Path salida;
        try {
            salida = Path.of(System.getProperty("salida", "."));
        } catch (InvalidPathException ex) {
            System.out.println("Ruta de salida no valida.");
            return;
        }
        try {
            reportes.generar(ultimaImportacion, ultimosErrores, salida);
            System.out.println("Archivos generados en: " + salida.toAbsolutePath().normalize());
            System.out.println(" - reporte_analisis.txt");
            System.out.println(" - errores_procesamiento.txt");
        } catch (IOException ex) {
            System.out.println("Error escribiendo reportes: " + ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("El analisis SQL no se pudo completar; revise el reporte incompleto.");
            mostrarErrorBD(ex);
        }
    }

    private void mostrarEstadisticas() {
        System.out.println("\n--- CONTADORES DE LA ULTIMA IMPORTACION ---");
        if (!importacionRealizada) {
            System.out.println("Todavia no se ha importado ningun archivo en esta sesion.");
            System.out.println("Las ventas registradas manualmente no cuentan como importaciones.");
            return;
        }
        mostrarResumen(ultimaImportacion);
        if (!ultimosErrores.isEmpty()) {
            System.out.println("Incidencias registradas: " + ultimosErrores.size());
            System.out.println("Puede generar el archivo de errores con la opcion 6.");
        }
    }

    private void mostrarResumen(EstadisticasProcesamiento e) {
        System.out.printf("Archivos procesados: %d | Archivos fallidos: %d%n",
                e.archivosProcesados, e.archivosFallidos);
        System.out.printf("Recibidos: %d | Validos: %d | Rechazados: %d%n",
                e.recibidos, e.validos, e.rechazados);
        System.out.printf("Unicos: %d | Duplicados entre archivos: %d%n",
                e.unicos, e.duplicados);
        System.out.printf("Insertados: %d | Existentes: %d | Conflictos BD: %d | Fallos BD: %d%n",
                e.insertados, e.existentesBD, e.conflictosBD, e.fallosBD);
        System.out.printf(Locale.ROOT, "Porcentaje rechazados: %.2f%%%n", e.porcentajeRechazados());
    }

    private void probarConexion() {
        System.out.println("\n--- COMPROBAR POSTGRESQL ---");
        try {
            int cantidad = ventaDAO.contarVentas();
            System.out.println("Conexion correcta. Tabla 'ventas' disponible.");
            System.out.println("Ventas almacenadas actualmente: " + cantidad);
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void mostrarErrorBD(SQLException ex) {
        System.out.println("No se pudo ejecutar la operacion con PostgreSQL.");
        System.out.println("SQLState: " + ex.getSQLState());
        if ("42P01".equals(ex.getSQLState())) {
            System.out.println("No existe la tabla 'ventas'. Ejecute sql/crear_base_datos.sql en pgAdmin.");
        } else {
            System.out.println("Verifique PostgreSQL, la base 'analisis_ventas' y config/db.properties.");
        }
    }

    private static class Cancelacion extends RuntimeException {
    }
}
