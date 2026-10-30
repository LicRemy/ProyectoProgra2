# Auditoría y estado de entrega

Fecha: 30/10/2026, America/Guatemala. Repositorio local auditado: C:\Users\jonat\ProyectoProgra2. Proyecto Maven: projectofinalventas. Se preservaron paquetes, base personal e historial. No se ejecutó commit ni push.

## Enunciado y alcance

Se encontró y leyó el enunciado original en C:\Users\jonat\OneDrive\Escritorio\PROYECTO DE CURSO.html. Los requisitos funcionales de la misión coinciden con él. El enunciado contiene discrepancias: proyecto 80 + defensa 20, rúbrica de producto 65 y defensa desglosada 35. Consultar el puntaje real con el docente. No están disponibles los archivos privados de evaluación.

## Diagnóstico inicial, antes de modificar

| Área | Estado inicial y evidencia | Corrección |
| --- | --- | --- |
| Compilación | 17 fuentes; clean test producía BUILD SUCCESS sin pruebas | JUnit, plugins fijados y destino Java 17 |
| CSV | split por coma; omitía línea física 1 vacía y rechazaba el encabezado | Commons CSV; encabezado lógico y campos entrecomillados |
| JSON/XML | Conversión duplicada, capturas genéricas, precisión y estructura poco controladas | Conversor común, excepciones específicas, XML seguro y JSON decimal |
| Estadísticas | 12 recibidos, 7 rechazados, 58,33 %; un encabezado era venta | 11 recibidos, 6 rechazados, 54,55 %; fallos de archivo separados |
| Entradas adicionales | Tres nombres fijados en Main | Descubrimiento por extensión u orden explícito de argumentos |
| Duplicados | Set persistente entre llamadas; contradicciones silenciosas, sin origen | Estado por llamada, contenido comparable y ambos orígenes |
| JDBC | Inserciones preparadas e idempotentes ya funcionaban | Distinguir idéntico/contradictorio y validar límites de BD |
| SQL | CREATE DATABASE IF NOT EXISTS y USE, sintaxis de MySQL | Script PostgreSQL ejecutable y restricciones de integridad |
| Análisis | Indicadores presentes pero SQL repetido en dos clases | Una fuente de consultas para consola y TXT |
| Reportes | Evidencia antigua y conclusiones genéricas pendientes | Archivos reproducibles, estado incompleto ante fallo SQL y seis apartados personales |
| Credenciales | db.properties registrado; gitignore apuntaba a otra ubicación | Retirado del índice, copia local conservada, ejemplo sin secretos |
| Documentación | README básico, sin pruebas ni documento breve/guía | README, guía, documento PDF de tres páginas, auditoría y resumen para commit |

La compilación inicial por sí sola no demostraba robustez ni el recorrido completo. Se encontraron implementaciones funcionales de JDBC, validación básica, deduplicación y consultas, que se conservaron y corrigieron.

## Archivos modificados y finalidad

- pom.xml: Java 17, Commons CSV, JUnit 5, plugins de compilación/pruebas/ejecución/empaquetado y dependencias copiadas para ejecutar el JAR.
- lectores/LectorCSV.java, LectorJSON.java, LectorXML.java: lectura robusta y contrato explícito; lectores comparten ConversorVenta.java, añadido para convertir y validar consistentemente.
- modelo/RegistroVenta.java: trazabilidad y comparación económica del contenido. modelo/EstadisticasProcesamiento.java: contadores separados e igualdades comprobables.
- validacion/ValidadorVenta.java: normalización Unicode, campos obligatorios, rango de fecha, longitudes y precio exacto. DeduplicadorVentas.java: ID canónico, reinicio por llamada y conflictos.
- datos/ConexionBD.java: configuración UTF-8, entorno, tiempos de espera y modo de solo lectura. VentaDAO.java: búsqueda normalizada, resultados insertada/existente/conflicto, parámetros JDBC y conexión prestada para ensayos aislados.
- app/Main.java: coordinación breve y opciones de rutas. app/ProcesadorVentas.java, añadido: descubrir, leer, contar y persistir. Projectofinalventas.java: delegación compatible; retirados los productos de demostración codificados del inicio antiguo.
- analisis/AnalizadorVentas.java: única fuente SQL, agrupación sin diferencias de mayúsculas e instantánea consistente. reportes/GeneradorReportes.java: métricas explícitas, archivos completos o marcados INCOMPLETO y conclusiones pendientes por autor.
- sql/crear_base_datos.sql: sintaxis PostgreSQL y validaciones en nuevas tablas. No se aplicó ninguna migración sobre public.ventas.
- config/db.properties.example y .gitignore: ejemplo portable y protección de la configuración privada. db.properties sigue físicamente presente, pero su eliminación del seguimiento está preparada en el índice.
- src/test/java y src/test/resources: casos JUnit y copias de muestras aisladas de entrada; añadir archivos de negocio no rompe las expectativas de las pruebas.
- reporte_analisis.txt y errores_procesamiento.txt: regenerados desde los archivos actuales contra datos reales con modo de solo lectura.
- README.md, GUIA_DEFENSA.md, este archivo, docs/RESUMEN_COMMIT.md, docs/DOCUMENTO_EXPLICATIVO.md, docs/generar_documento.py y output/pdf/documento_explicativo.pdf: documentación y fuente reproducible del documento breve.

## Verificación reproducible

El resumen observado de comandos, pruebas y huellas de los reportes se conserva en docs/EVIDENCIA_PRUEBAS.txt.

Se ejecutaron compilaciones y pruebas con Java 26 apuntando a release 17, y una compilación limpia y ejecución con Temurin JDK 17.0.20.1 portátil. El archivo del JDK se verificó con SHA-256 contra los metadatos oficiales de Adoptium. No se instaló ni reemplazó el Java del usuario.

Comando de la suite completa desde projectofinalventas: mvn -Dpg.integration=true clean package. Resultado verificado: 56 pruebas, 0 fallos, 0 errores y 0 omitidas; 52 casos de procesamiento y 4 PostgreSQL. El comando sin pg.integration omite explícitamente las cuatro pruebas que dependen de PostgreSQL.

Los 52 casos cubren venta válida, ocho campos obligatorios, fechas imposibles, cantidades negativas/cero/decimales/fuera de rango, precios inválidos y fuera del rango NUMERIC, normalización Unicode, longitudes, CSV con BOM/comillas/comas/saltos, encabezado reordenado o incorrecto, campos faltantes, tres archivos vacíos, colecciones vacías válidas, JSON mal formado/claves repetidas, XML mal formado/XXE, continuidad, duplicados CSV-JSON y CSV-XML, contradicción, reinicio del deduplicador, descubrimiento, porcentaje y reporte incompleto por fallo SQL.

Las cuatro pruebas PostgreSQL comprueban: (1) reejecución sin duplicados ni sobrescritura, (2) rechazo SQL de cantidad cero, (3) recorrido de los tres archivos a objeto, validación, JDBC, SQL y reporte reproducible, comparando todas las agrupaciones/extremos con consultas independientes, y (4) tabla vacía y agrupación sin diferencias de mayúsculas.

Aislamiento: CREATE TEMPORARY TABLE ventas, search_path limitado a pg_temp, comprobación de relpersistence = t y ROLLBACK. Una huella de public.ventas se compara antes y después. Ninguna prueba modifica ventas personales. Si falta PostgreSQL o sus permisos, la ejecución explícita de integración falla y no debe presentarse como aprobada.

El JAR se ejecutó directamente en JDK 17 con -DsoloLectura=true y su carpeta de dependencias. Procesó tres archivos, generó ambos TXT y reconoció las cuatro ventas ya almacenadas. No se comprobó interactivamente la interfaz NetBeans; el README explica los pasos.

## Resultados medidos

| Métrica | Valor |
| --- | ---: |
| Archivos procesados / fallidos | 3 / 0 |
| Registros recibidos | 11 |
| Válidos antes de duplicados | 5 |
| Rechazados por validación | 6 |
| Duplicados entre archivos | 1 |
| Aceptados únicos | 4 |
| Nuevas inserciones en la revisión de solo lectura | 0 |
| Ya existentes idénticos | 4 |
| Conflictos y fallos JDBC | 0 |
| Rechazados / recibidos | 54,55 % |
| Ventas acumuladas | 4 |
| Monto acumulado | Q11350,00 |
| Promedio por transacción | Q2837,50 |
| Vendedor con mayor monto | Carlos: Q9500,00 |
| Transacción de mayor / menor monto | V007: Q9000,00 / V005: Q350,00 |

Los seis rechazos y el duplicado producen siete incidencias, no siete rechazos. Un archivo ilegible no se cuenta como una venta. Los datos acumulados pueden cambiar si el equipo ejecuta nuevas inserciones legítimas; regenerar el reporte al preparar la entrega.

## Checklist frente al enunciado

| Requisito | Estado |
| --- | --- |
| Leer CSV, JSON y XML programáticamente y crear objetos | Verificado con JUnit e integración |
| Ocho campos obligatorios y fechas/números válidos | Verificado |
| Normalización consistente y sin inventar datos | Verificado |
| Duplicados entre archivos y reejecución | Verificado en PostgreSQL temporal |
| Excepciones específicas y continuación cuando es posible | Verificado con documentos y registros defectuosos |
| Recursos cerrados y PreparedStatement | Revisado y ejercitado en integración |
| JDBC PostgreSQL y restricciones SQL | Verificado en tabla temporal con el script |
| Nuevos archivos sin reescribir Main | Descubrimiento y argumentos verificados |
| Cantidades recibidas/aceptadas/rechazadas/duplicadas | Verificado y definido matemáticamente |
| Total y promedio monetario por transacción | Comparados con SQL |
| Mes, sucursal, producto y vendedor | Comparados con SQL |
| Extremos de producto/transacción y top 3 | Comparados con SQL |
| Porcentaje de rechazo | 6/11 = 54,55 %, verificado |
| Ambos TXT reproducibles y legibles | Verificado; fallos dejan estado INCOMPLETO |
| Tres conclusiones por cada estudiante | PENDIENTE HUMANO: seis espacios identificados |
| Proyecto portable y ejecutable | Compilación, pruebas y JAR verificados con Java 17 |
| README, guía y documento de 2–4 páginas | Entregados; PDF de tres páginas renderizado e inspeccionado |
| Historial Git progresivo de ambos | Historial preservado; participación real de ambos pendiente de acreditar |
| Ejecución interactiva en NetBeans | Pasos documentados; comprobación visual pendiente |
| Casos privados del profesor | NO DISPONIBLES; no se afirma aprobación |

## Riesgos y acciones humanas pendientes

1. Escribir seis conclusiones propias, tres por integrante, apoyadas en los indicadores reales. Los marcadores no satisfacen la interpretación final.
2. Revisar y, si corresponde, rotar la contraseña que pudo quedar publicada en commits previos. Eliminar del índice no borra historial. No hacer push de secretos.
3. El historial visible utiliza LicRemy como autor; no acredita por sí solo la participación de los dos estudiantes. Conservar contribuciones reales, sin falsificar commits.
4. No se migró la tabla personal: IF NOT EXISTS no añade restricciones a tablas antiguas. Revisar restricciones/IDs históricos con el equipo antes de modificar el esquema.
5. Los lectores cargan JSON/XML en memoria y el DAO abre conexiones por venta: es una decisión sencilla para el volumen académico, no una solución para millones de registros.
6. El contrato no acepta estructuras privadas no documentadas; los tests no garantizan pasar casos del docente no disponibles.
7. La interfaz NetBeans y la comprensión individual deben comprobarse por los integrantes. Resolver la inconsistencia de puntajes con el profesor.

## Fuentes técnicas consultadas

- Apache Commons CSV: https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html
- PostgreSQL CREATE TABLE y tablas temporales: https://www.postgresql.org/docs/current/sql-createtable.html
- javac y compilación para versiones previas: https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html

El resumen listo para registrar está en docs/RESUMEN_COMMIT.md. No se declara cerrada la entrega académica mientras falten conclusiones, revisión de credenciales y defensa de ambos integrantes.
