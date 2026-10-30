package com.mycompany.projectofinalventas;

import com.mycompany.projectofinalventas.app.ProcesadorVentas;
import com.mycompany.projectofinalventas.lectores.*;
import com.mycompany.projectofinalventas.modelo.*;
import com.mycompany.projectofinalventas.validacion.*;
import com.mycompany.projectofinalventas.reportes.GeneradorReportes;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ProcesamientoTest {
    @TempDir Path temporal;
    static final String HEADER=String.join(",",ConversorVenta.CAMPOS);
    Map<String,String> datos() {
        return new HashMap<>(Map.of("id_venta"," v001 ","fecha","2026-10-01",
            "sucursal"," Chiquimula ","codigo_producto","tec001","producto","Teclado",
            "cantidad","2","precio_unitario","250.00","vendedor","Carlos"));
    }
    List<RegistroVenta> convertir(Map<String,String> datos,List<String> errores) {
        List<RegistroVenta> ventas=new ArrayList<>();
        new ConversorVenta().agregar(datos,"prueba registro 1",ventas,errores);
        return ventas;
    }
    Path archivo(String nombre,String contenido) throws Exception {
        return Files.writeString(temporal.resolve(nombre),contenido);
    }
    List<RegistroVenta> leer(Path archivo,List<String> errores) {
        String ruta=archivo.toString();
        if(ruta.endsWith(".csv"))return new LectorCSV().leer(ruta,errores);
        if(ruta.endsWith(".json"))return new LectorJSON().leer(ruta,errores);
        return new LectorXML().leer(ruta,errores);
    }
    @Test void ventaCorrecta() {
        var errores=new ArrayList<String>();var ventas=convertir(datos(),errores);
        assertTrue(errores.isEmpty());assertEquals(new BigDecimal("500.00"),ventas.get(0).calcularTotal());
        assertEquals("V001",ventas.get(0).getIdVenta());
    }
    @ParameterizedTest @ValueSource(strings={"id_venta","fecha","sucursal","codigo_producto","producto","cantidad","precio_unitario","vendedor"})
    void cadaCampoObligatorio(String campo) {
        var datos=datos();datos.remove(campo);var errores=new ArrayList<String>();
        assertTrue(convertir(datos,errores).isEmpty());assertEquals(1,errores.size());
        assertTrue(errores.get(0).contains("prueba registro 1"));
    }
    @ParameterizedTest @ValueSource(strings={"2026-02-30","2025-02-29","fecha-invalida","2026-13-01","0000-01-01"})
    void fechaInvalida(String valor) {
        var datos=datos();datos.put("fecha",valor);var errores=new ArrayList<String>();
        assertTrue(convertir(datos,errores).isEmpty());assertTrue(errores.get(0).toLowerCase().contains("fecha"));
    }
    @ParameterizedTest @ValueSource(strings={"-3","0","2.5","abc","2147483648"})
    void cantidadInvalida(String valor) {
        var datos=datos();datos.put("cantidad",valor);
        assertTrue(convertir(datos,new ArrayList<>()).isEmpty());
    }
    @ParameterizedTest @ValueSource(strings={"0","-1","NaN","abc","12,50","1.234","10000000000.00"})
    void precioInvalidoSinRedondear(String valor) {
        var datos=datos();datos.put("precio_unitario",valor);
        assertTrue(convertir(datos,new ArrayList<>()).isEmpty());
    }
    @Test void normalizacionPreservaEconomiaYOrigen() {
        var datos=datos();datos.put("producto","  Teclado\t mecanico  ");datos.put("vendedor"," Carlos\u00a0 Perez ");
        var venta=convertir(datos,new ArrayList<>()).get(0);
        assertEquals("Teclado mecanico",venta.getProducto());assertEquals("Carlos Perez",venta.getVendedor());
        assertEquals("TEC001",venta.getCodigoProducto());assertEquals("CHIQUIMULA",venta.getSucursal());
        assertEquals(new BigDecimal("250.00"),venta.getPrecioUnitario());assertEquals("prueba registro 1",venta.getOrigen());
    }
    @Test void rechazaTextosFueraDeLongitud() {
        var datos=datos();datos.put("id_venta","X".repeat(51));assertTrue(convertir(datos,new ArrayList<>()).isEmpty());
    }
    @Test void csvComillasComasSaltosYBOM() throws Exception {
        var ruta=archivo("comillas.csv","\uFEFF"+HEADER+"\nV1,2026-10-01,Guatemala,P1,\"Teclado, modelo \"\"A\"\"\nUSB\",1,12.50,Ana\n");
        var errores=new ArrayList<String>();var ventas=leer(ruta,errores);
        assertTrue(errores.isEmpty(),errores.toString());assertEquals(1,ventas.size());
        assertEquals("Teclado, modelo \"A\" USB",ventas.get(0).getProducto());
    }
    @Test void csvEncabezadoTrasLineaVaciaYReordenado() throws Exception {
        var ruta=archivo("reorden.csv","\nVENDEDOR,precio_unitario,cantidad,producto,codigo_producto,sucursal,fecha,id_venta\nAna,12.00,1,Mouse,P1,Zacapa,2026-10-01,V1");
        assertEquals(1,leer(ruta,new ArrayList<>()).size());
    }
    @Test void encabezadoIncorrectoFallaArchivo() throws Exception {
        var ruta=archivo("malo.csv","id_venta,fecha\nV1,2026-10-01");
        assertThrows(IllegalStateException.class,()->leer(ruta,new ArrayList<>()));
    }
    @Test void camposFaltantesNoDetienenSiguienteRegistro() throws Exception {
        var ruta=archivo("faltantes.csv",HEADER+"\nV1,2026-10-01\nV2,2026-10-01,Zacapa,P1,Mouse,1,20,Ana");
        var errores=new ArrayList<String>();assertEquals(1,leer(ruta,errores).size());assertEquals(1,errores.size());
    }
    @ParameterizedTest @ValueSource(strings={"csv","json","xml"})
    void archivoFisicamenteVacioFalla(String extension) throws Exception {
        Path ruta=archivo("vacio."+extension,"");assertThrows(IllegalStateException.class,()->leer(ruta,new ArrayList<>()));
    }
    @Test void coleccionesVaciasValidas() throws Exception {
        assertTrue(leer(archivo("vacio.json","[]"),new ArrayList<>()).isEmpty());
        assertTrue(leer(archivo("vacio.xml","<ventas/>"),new ArrayList<>()).isEmpty());
        assertTrue(leer(archivo("vacio.csv",HEADER),new ArrayList<>()).isEmpty());
    }
    @ParameterizedTest @ValueSource(strings={"{","[{\"id_venta\":1}","[] []","{\"ventas\":[]}","[{\"id_venta\":1,\"id_venta\":2}]"})
    void jsonMalFormadoOEstructuraIncorrecta(String contenido) throws Exception {
        var ruta=archivo("malo.json",contenido);assertThrows(IllegalStateException.class,()->leer(ruta,new ArrayList<>()));
    }
    @ParameterizedTest @ValueSource(strings={"<ventas>","\n<?xml version=\"1.0\"?><ventas/>",
        "<!DOCTYPE ventas [<!ENTITY x SYSTEM \"file:///no-leer\">]><ventas>&x;</ventas>","<otra/>"})
    void xmlMalFormadoYXXE(String contenido) throws Exception {
        var ruta=archivo("malo.xml",contenido);assertThrows(IllegalStateException.class,()->leer(ruta,new ArrayList<>()));
    }
    static String jsonVenta() {
        return """
            {"id_venta":"v001","fecha":"2026-10-01","sucursal":"Chiquimula","codigo_producto":"TEC001",
             "producto":"Teclado","cantidad":2,"precio_unitario":250.00,"vendedor":"Carlos"}
            """;
    }
    @Test void jsonIncompletoNoDetieneSiguienteRegistro() throws Exception {
        var ruta=archivo("incompleto.json","[{},null,"+jsonVenta()+"]");var errores=new ArrayList<String>();
        assertEquals(1,leer(ruta,errores).size());assertEquals(2,errores.size());
    }
    @ParameterizedTest @ValueSource(strings={"json","xml"})
    void duplicadosEntreCSVYOtroFormato(String formato) throws Exception {
        var csv=archivo("uno.csv",HEADER+"\nV001,2026-10-01,Chiquimula,TEC001,Teclado,2,250,Carlos");
        String segundo=formato.equals("json") ? "["+jsonVenta()+"]" :
            "<ventas><venta><id_venta> v001 </id_venta><fecha>2026-10-01</fecha><sucursal>Chiquimula</sucursal>"
            +"<codigo_producto>TEC001</codigo_producto><producto>Teclado</producto><cantidad>2</cantidad>"
            +"<precio_unitario>250.00</precio_unitario><vendedor>Carlos</vendedor></venta></ventas>";
        var errores=new ArrayList<String>();var e=new EstadisticasProcesamiento();
        var unicas=new ProcesadorVentas().leer(List.of(csv,archivo("dos."+formato,segundo)),e,errores);
        assertEquals(1,unicas.size());assertEquals(2,e.recibidos);assertEquals(1,e.duplicados);assertEquals(0,e.rechazados);
        assertTrue(errores.get(0).contains("identico"));assertTrue(errores.get(0).contains("dos."));
        assertTrue(errores.get(0).contains("uno.csv"));
    }
    @Test void conflictoConservaPrimeroYReiniciaDeduplicador() {
        var errores=new ArrayList<String>();var primera=convertir(datos(),errores).get(0);
        var datos=datos();datos.put("cantidad","3");var segunda=convertir(datos,errores).get(0);
        var dedup=new DeduplicadorVentas();var unicas=dedup.eliminarDuplicados(List.of(primera,segunda),errores);
        assertEquals(2,unicas.get(0).getCantidad());assertEquals(1,dedup.getDuplicados());
        assertTrue(errores.get(0).contains("CONTRADICTORIO"));
        assertEquals(1,dedup.eliminarDuplicados(List.of(primera),new ArrayList<>()).size());assertEquals(0,dedup.getDuplicados());
    }
    @Test void falloArchivoNoEsRegistroRechazadoYDescubreEntradasNuevas() throws Exception {
        archivo("tres.json","[]");archivo("ignorar.txt","no es venta");archivo("roto.xml","<ventas>");
        var p=new ProcesadorVentas();var archivos=p.descubrir(temporal);assertEquals(2,archivos.size());
        var e=new EstadisticasProcesamiento();var errores=new ArrayList<String>();p.leer(archivos,e,errores);
        assertEquals(0,e.recibidos);assertEquals(0,e.rechazados);assertEquals(1,e.archivosFallidos);
        assertEquals(1,e.archivosProcesados);assertEquals(1,errores.size());e.comprobar();
    }
    @Test void contadoresYPorcentaje() {
        var e=new EstadisticasProcesamiento();assertEquals(0,e.porcentajeRechazados());
        e.recibidos=11;e.validos=5;e.rechazados=6;e.unicos=4;e.duplicados=1;e.existentesBD=4;e.comprobar();
        assertEquals(54.545454,e.porcentajeRechazados(),0.000001);
        e.recibidos++;assertThrows(IllegalStateException.class,e::comprobar);
    }
    @Test void fallaSQLDejaReporteIncompletoYErrores() throws Exception {
        String anterior=System.getProperty("db.config");
        try {
            Path config=archivo("invalido.properties","db.url=NO_POSTGRESQL\ndb.user=x\ndb.password=NO_IMPRIMIR");
            System.setProperty("db.config",config.toString());
            assertThrows(java.sql.SQLException.class,()->new GeneradorReportes().generar(
                new EstadisticasProcesamiento(),List.of("archivo roto"),temporal.resolve("salida")));
            assertTrue(Files.readString(temporal.resolve("salida/reporte_analisis.txt")).contains("INCOMPLETO"));
            assertEquals("archivo roto\n",Files.readString(temporal.resolve("salida/errores_procesamiento.txt")));
            assertFalse(Files.readString(temporal.resolve("salida/reporte_analisis.txt")).contains("NO_IMPRIMIR"));
        } finally {
            if(anterior==null)System.clearProperty("db.config");else System.setProperty("db.config",anterior);
        }
    }
}
