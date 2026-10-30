# Sistema de Integración y Análisis de Datos de Ventas

Proyecto final de Programación II. Aplicación de consola, Java 17 o superior, Maven 3.9 y PostgreSQL. Integrantes: Jonathan Daniel Alberto Aguilar Ramírez y Luis Alberto Matta Rubio.

## Construir y ejecutar

Todos los comandos de esta sección se ejecutan **dentro de `projectofinalventas`**, donde está el POM.

```powershell
mvn clean test
mvn package
mvn exec:java
```

Si Maven no está en PATH, NetBeans incluye uno. En este equipo:

```powershell
& 'C:\Program Files\netbeans\java\maven\bin\mvn.cmd' clean test
& 'C:\Program Files\netbeans\java\maven\bin\mvn.cmd' exec:java
```

Java 17 es la versión mínima (LTS); el código y los tests no requieren Java 26. `mvn package` genera el JAR en `target` y copia bibliotecas a `target/dependency`. También puede ejecutarse sin Maven después de empaquetar:

```powershell
java -jar target/projectofinalventas-1.0-SNAPSHOT.jar
```

El manifiesto encuentra las bibliotecas en `target/dependency`. La entrega necesita tanto el JAR como esa carpeta, la configuración privada y las entradas.

## NetBeans

1. File → Open Project → elegir la carpeta `projectofinalventas`.
2. Tools → Java Platforms → registrar un JDK 17 o superior; elegirlo en Properties → Build.
3. Usar Reload Project si la vista conserva errores anteriores y después Clean and Build.
4. La clase principal configurada es `com.mycompany.projectofinalventas.app.Main`.
5. Configurar PostgreSQL como se indica abajo y usar Run Project. El directorio de trabajo debe ser `projectofinalventas`.
6. Revisar los dos TXT generados en esa carpeta. Run File sobre `PruebaConexion` verifica solamente conectividad; `PruebaAnalisis` consulta indicadores sin insertar.
7. El punto de entrada antiguo `Projectofinalventas` delega en `Main`; ya no utiliza el prototipo de inventario.

La compilación y la ejecución se verifican por Maven/Java; los pasos de la interfaz de NetBeans se documentan para reproducirlos, no sustituyen las pruebas.

## PostgreSQL y credenciales

Crear la base `analisis_ventas` desde pgAdmin. Con Query Tool conectado a esa base, ejecutar `sql/crear_base_datos.sql`. Es SQL de PostgreSQL: no utiliza `USE` ni `CREATE DATABASE IF NOT EXISTS`.

Copiar `config/db.properties.example` a `config/db.properties` y completar usuario y contraseña **localmente**. La copia privada está ignorada por Git. Alternativas: variables `DB_URL`, `DB_USER`, `DB_PASSWORD` (tienen prioridad) o `-Ddb.config=otra/ruta.properties`. Nunca poner una contraseña en un comando, README, captura o commit.

`CREATE TABLE IF NOT EXISTS` no migra una tabla existente. El script nuevo verifica ID canónico, textos no vacíos, fechas, cantidades y precios positivos. No se modificó el esquema ni el contenido personal durante la auditoría. Revisar restricciones antiguas antes de una migración manual.

**Acción humana pendiente:** el archivo privado estuvo registrado en el historial anterior. Se retiró del índice conservándolo en disco. Revisar la exposición y rotar la contraseña si fue publicada; ignorar un archivo no borra commits antiguos. No se reescribió ni publicó el historial.

## Entradas y contrato

Sin argumentos, el programa descubre todos los archivos `.csv`, `.json` y `.xml` directamente dentro de `entrada`, ordenados por nombre. No recorre subcarpetas. Añadir archivos con la misma estructura no requiere cambiar código.

```powershell
mvn "-Dentrada=otra_carpeta" "-Dsalida=salida_nueva" exec:java
mvn '-Dexec.args=entrada/ventas.csv entrada/ventas.json entrada/ventas.xml' exec:java
mvn "-DsoloLectura=true" exec:java
```

Los argumentos explícitos son rutas individuales y establecen el orden de preferencia para duplicados. Para rutas con espacios se necesitan comillas dentro de `exec.args`. El modo `soloLectura` impide cambios en tablas permanentes mediante transacciones PostgreSQL de solo lectura: sirve para verificar IDs existentes; los nuevos generarán incidencias SQL y no se insertarán.

Los ocho campos obligatorios son:

| Campo | Regla |
| --- | --- |
| id_venta | No vacío, máximo 50 caracteres |
| fecha | Fecha real ISO `yyyy-MM-dd`, años 0001–9999 |
| sucursal | No vacía, máximo 100 |
| codigo_producto | No vacío, máximo 50 |
| producto | No vacío, máximo 150 |
| cantidad | Entero de 32 bits, mayor que cero |
| precio_unitario | Positivo, máximo 9999999999.99 y hasta 2 decimales significativos |
| vendedor | No vacío, máximo 100 |

UTF-8; decimal con punto. No se inventan campos, fechas ni cantidades. Los precios incompatibles con `NUMERIC(12,2)` se rechazan para evitar redondearlos silenciosamente.

- CSV: encabezados con esos ocho nombres, cualquier orden y mayúsculas/minúsculas. Admite BOM UTF-8, líneas vacías, comas dentro de comillas, comillas dobles escapadas y campos multilínea (Apache Commons CSV). El número mostrado es el de registro lógico CSV, incluido el encabezado; no siempre coincide con la línea física.
- JSON: arreglo de objetos con esos campos. Valores de texto o numéricos; null equivale a campo vacío. Rechaza campos desconocidos, claves duplicadas, booleanos y objetos anidados. No admite un envoltorio `{"ventas": ...}`, porque no aparece en las entradas originales.
- XML: raíz `ventas`, hijos directos `venta` y los ocho campos como elementos de texto. Se aceptan mayúsculas/minúsculas en etiquetas. No admite campos repetidos, desconocidos o anidados ni atributos en ventas individuales. Se bloquean DTD y entidades externas.
- Un archivo físicamente vacío o un documento mal formado es un fallo de archivo. Un CSV con solo encabezados, `[]` y `<ventas/>` son colecciones válidas de cero registros. Si falla la estructura completa, no se aceptan fragmentos parciales de ese archivo.
- Campos vacíos o valores inválidos rechazan solo el registro; los demás continúan.

No se presuponen alias ni estructuras privadas del docente. Para una nueva estructura real, adaptar su lector y agregar una prueba antes de aceptarla.

## Normalización y duplicados

Se recortan y colapsan espacios Unicode. ID, sucursal y código se convierten a mayúsculas con Locale.ROOT. Producto y vendedor conservan su presentación; las consultas SQL agrupan sin distinguir mayúsculas. No se eliminan tildes ni se fusionan nombres distintos.

La deduplicación usa ID normalizado. Se conserva el **primer registro válido** en el orden de archivos indicado. Un duplicado idéntico y uno con contenido contradictorio generan mensajes distintos con ambos orígenes. No se considera duplicado un registro inválido previamente rechazado.

JDBC usa PreparedStatement y try-with-resources. La clave primaria y `ON CONFLICT DO NOTHING` evitan nuevas filas con el mismo ID. Si PostgreSQL ya contiene ese ID con datos diferentes, se conserva la venta almacenada y se registra conflicto; no se hace UPDATE. Las tablas antiguas con IDs sin normalizar requieren revisión; la aplicación también busca con `upper(btrim(id_venta))`.

## Contadores e indicadores

Las estadísticas de ejecución están separadas de los acumulados SQL:

```text
recibidos = validos + rechazados
validos = unicos + duplicados
unicos = insertados + existentesBD + conflictosBD + fallosBD
porcentajeRechazados = rechazados / recibidos * 100
```

Si recibidos es cero, el porcentaje es cero. Un archivo ilegible se cuenta en archivosFallidos; no se inventa cuántos registros contenía. Un registro rechazado puede tener varios motivos pero se cuenta una sola vez. Duplicados y conflictos de persistencia son incidencias independientes de los rechazos de validación.

SQL calcula cantidad almacenada, SUM(cantidad * precio_unitario), AVG por venta, agrupaciones por mes/sucursal/producto/vendedor, extremos y top 3 vendedores. Los empates se resuelven por etiqueta para obtener resultados reproducibles. Los nombres se agrupan con UPPER y los montos se muestran en quetzales. El análisis usa una instantánea REPEATABLE READ de solo lectura.

## Reportes y conclusiones

`reporte_analisis.txt` contiene estadísticas de ejecución y consultas acumuladas. `errores_procesamiento.txt` identifica archivo, registro, ID cuando está disponible y motivo. Se escriben en UTF-8 mediante archivos temporales y reemplazo atómico cuando el sistema lo permite.

Si falla SQL, se conserva el registro de incidencias y el análisis queda marcado **INCOMPLETO**. Solo se anuncia éxito después de escribir ambos archivos. Los seis apartados de conclusiones personales siguen pendientes y deben redactarlos los dos estudiantes a partir de datos comprobados. No se generan conclusiones ficticias.

## Pruebas

```powershell
mvn clean test
mvn "-Dpg.integration=true" test
```

La primera orden ejecuta pruebas sin necesitar PostgreSQL y omite explícitamente las de integración. La segunda exige PostgreSQL configurado: no marca como aprobada una conexión ausente.

Las pruebas de integración crean `pg_temp.ventas`, restringen el search_path a `pg_temp`, verifican que sea temporal y usan ROLLBACK. Comparan una huella de `public.ventas` antes y después. Nunca insertan ni eliminan ventas personales. El usuario JDBC necesita permiso TEMP y SELECT sobre public.ventas para esa verificación.

Los casos están en `src/test/java` y ejemplos adicionales en `src/test/resources/casos`. No copiar casos deliberadamente inválidos a entrada salvo para ensayarlos. Evidencias y límites de verificación: [AUDITORIA.md](AUDITORIA.md) en la raíz del repositorio.

## Documentación y defensa

En la raíz: `GUIA_DEFENSA.md`, `AUDITORIA.md`, `docs/RESUMEN_COMMIT.md` y `output/pdf/documento_explicativo.pdf`. El enunciado local fue encontrado y leído; sus puntajes contradictorios deben consultarse con el docente. Los casos privados no están disponibles y no se afirma haberlos superado.

La fuente editable del documento es `docs/DOCUMENTO_EXPLICATIVO.md`. Para regenerar el PDF se necesita Python con ReportLab y ejecutar `python docs/generar_documento.py` desde la raíz del repositorio. Python no es necesario para ejecutar la aplicación Java.

El historial existente se conserva. No se hicieron commits de autoría inventada ni push. La participación progresiva de ambos integrantes debe ser real y demostrable.
