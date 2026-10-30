package com.mycompany.projectofinalventas;

import com.mycompany.projectofinalventas.analisis.AnalizadorVentas;
import com.mycompany.projectofinalventas.app.ProcesadorVentas;
import com.mycompany.projectofinalventas.datos.*;
import com.mycompany.projectofinalventas.modelo.*;
import com.mycompany.projectofinalventas.reportes.GeneradorReportes;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Solo usa pg_temp.ventas y ROLLBACK. Nunca modifica public.ventas. */
@EnabledIfSystemProperty(named="pg.integration", matches="true")
class PostgresTest {
    @TempDir Path temporal;
    Connection cn;
    String huellaAntes;
    @BeforeEach void preparar() throws Exception {
        cn=ConexionBD.conectar();
        cn.setAutoCommit(false);
        huellaAntes=huellaPublica();
        try(Statement st=cn.createStatement()) {
            st.execute("SET LOCAL search_path TO pg_temp");
            String script=Files.readString(Path.of("sql/crear_base_datos.sql"))
                .replace("CREATE TABLE IF NOT EXISTS ventas", "CREATE TEMPORARY TABLE ventas");
            st.execute(script);
        }
        try(var ps=cn.prepareStatement("SELECT relpersistence FROM pg_class WHERE oid='ventas'::regclass");
            var rs=ps.executeQuery()) {
            assertTrue(rs.next());assertEquals("t",rs.getString(1),"Debe ser temporal antes de insertar");
        }
    }
    @AfterEach void limpiar() throws Exception {
        if(cn!=null) {
            try {
                cn.rollback();
                if(huellaAntes!=null) assertEquals(huellaAntes,huellaPublica(),"Las ventas personales deben conservarse");
            } finally { cn.close(); }
        }
    }
    String huellaPublica() throws SQLException {
        try(var st=cn.createStatement();var rs=st.executeQuery("SELECT to_regclass('public.ventas')")) {
            rs.next();if(rs.getString(1)==null)return "tabla permanente ausente";
        }
        try(var st=cn.createStatement();var rs=st.executeQuery("""
            SELECT md5(COALESCE(string_agg(row_to_json(v)::text, E'\n' ORDER BY id_venta),''))
            FROM public.ventas v
            """)) {
            rs.next();return rs.getString(1);
        }
    }
    RegistroVenta venta(String id,int cantidad) {
        return new RegistroVenta(id,java.time.LocalDate.of(2026,10,1),"Guatemala","TEC1",
            "Teclado",cantidad,new BigDecimal("25.50"),"Ana");
    }
    @Test void reejecucionYConflictoNoDuplicanNiSobrescriben() throws Exception {
        var dao=new VentaDAO();
        assertEquals(VentaDAO.Resultado.INSERTADA,dao.guardar(cn,venta(" v1 ",2)));
        assertEquals(VentaDAO.Resultado.EXISTENTE,dao.guardar(cn,venta("V1",2)));
        assertEquals(VentaDAO.Resultado.CONFLICTO,dao.guardar(cn,venta("v1",5)));
        try(var st=cn.createStatement();var rs=st.executeQuery("SELECT COUNT(*), SUM(cantidad) FROM ventas")) {
            rs.next();assertEquals(1,rs.getInt(1));assertEquals(2,rs.getInt(2));
        }
    }
    @Test void restriccionesSQLRechazanCantidadCero() throws Exception {
        var savepoint=cn.setSavepoint();
        try(var ps=cn.prepareStatement("""
            INSERT INTO ventas VALUES ('T1','2026-10-01','Guatemala','P1','Producto',0,1.00,'Ana')
            """)) {
            var ex=assertThrows(SQLException.class,ps::executeUpdate);assertEquals("23514",ex.getSQLState());
        } finally {cn.rollback(savepoint);}
    }
    @Test void recorridoCompletoIndicadoresYReporteReproducible() throws Exception {
        var p=new ProcesadorVentas();var e=new EstadisticasProcesamiento();var errores=new ArrayList<String>();
        var ventas=p.leer(p.descubrir(Path.of("src/test/resources/entrada")),e,errores);
        assertEquals(11,e.recibidos);assertEquals(5,e.validos);assertEquals(6,e.rechazados);
        assertEquals(1,e.duplicados);assertEquals(4,e.unicos);assertEquals(54.545454,e.porcentajeRechazados(),0.000001);
        var dao=new VentaDAO();
        for(var v:ventas) {assertEquals(VentaDAO.Resultado.INSERTADA,dao.guardar(cn,v));e.insertados++;}
        for(var v:ventas) assertEquals(VentaDAO.Resultado.EXISTENTE,dao.guardar(cn,v));
        e.comprobar();
        var generador=new GeneradorReportes();generador.generar(e,errores,temporal,cn);
        String reporte=Files.readString(temporal.resolve("reporte_analisis.txt"));
        generador.generar(e,errores,temporal,cn);
        assertEquals(reporte,Files.readString(temporal.resolve("reporte_analisis.txt")));
        assertTrue(reporte.contains("Monto total: Q11350.00"));
        assertTrue(reporte.contains("Promedio por transaccion: Q2837.50"));
        assertTrue(reporte.contains("CARLOS | Transacciones: 2 | Monto: Q9500.00"));
        assertTrue(reporte.contains("CHIQUIMULA | Transacciones: 3 | Monto: Q9850.00"));
        assertTrue(reporte.contains("TEC002 - TECLADO MECANICO | Transacciones: 1 | Monto: Q350.00"));
        assertTrue(reporte.contains("V007 | Transacciones: 1 | Monto: Q9000.00"));
        assertTrue(reporte.contains("V005 | Transacciones: 1 | Monto: Q350.00"));
        for(String titulo:new String[]{"VENTAS POR MES","VENTAS POR SUCURSAL","VENTAS POR PRODUCTO",
                "VENTAS POR VENDEDOR","TOP 3 VENDEDORES","PRODUCTO CON MAYOR MONTO","PRODUCTO CON MENOR MONTO",
                "TRANSACCION DE MAYOR MONTO","TRANSACCION DE MENOR MONTO"}) assertTrue(reporte.contains(titulo));
        try(var st=cn.createStatement();var rs=st.executeQuery("SELECT SUM(cantidad*precio_unitario),AVG(cantidad*precio_unitario) FROM ventas")) {
            rs.next();assertEquals(0,new BigDecimal("11350.00").compareTo(rs.getBigDecimal(1)));
            assertEquals(0,new BigDecimal("2837.50").compareTo(rs.getBigDecimal(2)));
        }
        assertEquals(7,Files.readAllLines(temporal.resolve("errores_procesamiento.txt")).size());
        // Comparar cada fila de las nueve agrupaciones/extremos con su SQL independiente.
        String[] consultas={
            "SELECT to_char(fecha,'YYYY-MM'),count(*),sum(cantidad*precio_unitario) FROM ventas GROUP BY 1",
            "SELECT upper(sucursal),count(*),sum(cantidad*precio_unitario) FROM ventas GROUP BY 1",
            "SELECT upper(codigo_producto)||' - '||upper(producto),count(*),sum(cantidad*precio_unitario) FROM ventas GROUP BY 1",
            "SELECT upper(vendedor),count(*),sum(cantidad*precio_unitario) FROM ventas GROUP BY 1",
            "SELECT upper(vendedor),count(*),sum(cantidad*precio_unitario) m FROM ventas GROUP BY 1 ORDER BY m DESC,1 LIMIT 3",
            "SELECT upper(codigo_producto)||' - '||upper(producto),count(*),sum(cantidad*precio_unitario) m FROM ventas GROUP BY 1 ORDER BY m DESC,1 LIMIT 1",
            "SELECT upper(codigo_producto)||' - '||upper(producto),count(*),sum(cantidad*precio_unitario) m FROM ventas GROUP BY 1 ORDER BY m ASC,1 LIMIT 1",
            "SELECT id_venta,1,cantidad*precio_unitario m FROM ventas ORDER BY m DESC,id_venta LIMIT 1",
            "SELECT id_venta,1,cantidad*precio_unitario m FROM ventas ORDER BY m ASC,id_venta LIMIT 1"
        };
        for(String sql:consultas) try(var ps=cn.prepareStatement(sql);var rs=ps.executeQuery()) {
            while(rs.next()) {
                String fila=rs.getString(1)+" | Transacciones: "+rs.getLong(2)+" | Monto: Q"
                    +rs.getBigDecimal(3).setScale(2,java.math.RoundingMode.HALF_UP);
                assertTrue(reporte.contains(fila),fila);
            }
        }
    }
    @Test void analisisSinVentasYMayusculas() throws Exception {
        String vacio=new AnalizadorVentas().analizar(cn);
        assertTrue(vacio.contains("Transacciones almacenadas: 0"));assertTrue(vacio.contains("Monto total: Q0.00"));
        var dao=new VentaDAO();dao.guardar(cn,venta("V1",1));
        var otra=new RegistroVenta("V2",java.time.LocalDate.of(2026,10,2),"guatemala","TEC1",
            "teclado",1,new BigDecimal("25.50"),"ana");
        dao.guardar(cn,otra);
        String agrupado=new AnalizadorVentas().analizar(cn);
        assertTrue(agrupado.contains("ANA | Transacciones: 2 | Monto: Q51.00"));
        assertTrue(agrupado.contains("TEC1 - TECLADO | Transacciones: 2 | Monto: Q51.00"));
    }
}
