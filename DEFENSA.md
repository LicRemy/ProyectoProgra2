# Defensa del proyecto

Esta guía sirve para recorrer el código y experimentar. Cada integrante debe poder explicar y modificar el programa sin depender de respuestas memorizadas.

## Qué hace cada paquete y clase

| Paquete | Clase | Responsabilidad |
| --- | --- | --- |
| app | Main | Elegir entradas y salida, coordinar procesamiento y generación de reportes |
| app | ProcesadorVentas | Descubrir archivos, reunir ventas, separar contadores y persistir cada venta única |
| app | PruebaConexion | Comprobar una conexión real; no inserta |
| app | PruebaAnalisis | Mostrar consultas acumuladas; no inserta |
| lectores | LectorCSV | Leer CSV con reglas de comillas y encabezados mediante Commons CSV |
| lectores | LectorJSON | Leer un arreglo JSON con Jackson y comprobar la estructura |
| lectores | LectorXML | Leer ventas XML con DOM y bloquear referencias externas |
| lectores | ConversorVenta | Convertir los ocho campos en tipos Java y aplicar reglas compartidas |
| modelo | RegistroVenta | Transportar venta, origen, total monetario y comparación de contenido |
| modelo | EstadisticasProcesamiento | Contadores, porcentaje y comprobación de sus igualdades |
| validacion | ValidadorVenta | Normalizar y validar sin inventar información |
| validacion | DeduplicadorVentas | Conservar primer ID válido e identificar repeticiones y contradicciones |
| datos | ConexionBD | Leer configuración privada y abrir una conexión PostgreSQL |
| datos | VentaDAO | Buscar e insertar mediante PreparedStatement, sin sobrescribir conflictos |
| analisis | AnalizadorVentas | Ejecutar las consultas SQL para consola y reportes |
| reportes | GeneradorReportes | Combinar estadísticas y consultas y guardar los dos TXT |
| paquete raíz | Projectofinalventas | Compatibilidad: delegar el inicio antiguo a Main |
| paquete raíz | Producto y Venta | Clases conservadas del prototipo anterior; no participan en la integración |
| pruebas | ProcesamientoTest | Casos de lectores, conversión, validación, duplicados y errores |
| pruebas | PostgresTest | Persistencia, restricciones y reportes en tabla temporal PostgreSQL |

## Recorrido de una venta

Abrir entrada/ventas.csv y localizar V001. LectorCSV reconoce el encabezado y extrae ocho campos. ConversorVenta convierte fecha a LocalDate, cantidad a int y precio a BigDecimal. ValidadorVenta limpia espacios y pasa el ID a mayúsculas. Si una regla falla, se guarda un mensaje y no se produce una venta aceptada.

Después se comparan IDs. El JSON incluye v001: normalizado es V001 y coincide con el CSV. DeduplicadorVentas conserva el primero y guarda ambos orígenes en la incidencia. VentaDAO consulta PostgreSQL: si no existe, intenta insertar; si existe y coincide, lo cuenta como existente; si difiere, registra conflicto y conserva lo almacenado.

AnalizadorVentas consulta la tabla consolidada. GeneradorReportes incorpora resultados y estadísticas. Verificar este recorrido usando un punto de interrupción en ConversorVenta.agregar y otro en VentaDAO.guardar.

## Cómo funciona JDBC

Una Connection es una sesión con PostgreSQL. PreparedStatement contiene SQL con marcadores ? y recibe valores con setString, setInt, setDate y setBigDecimal. Los valores no se concatenan al SQL. executeUpdate devuelve cuántas filas se insertaron; executeQuery produce un ResultSet para recorrer resultados.

try-with-resources cierra estos objetos aunque haya una excepción. En la ejecución normal cada inserción usa autocommit: un fallo individual no revierte otras ventas ya guardadas. Una SQLException se registra con su SQLState y el ID afectado; la siguiente venta continúa.

Las pruebas prestan una misma conexión al DAO y al analizador, crean una tabla temporal y usan ROLLBACK. Así se prueba PostgreSQL real sin escribir ventas personales. El modo soloLectura impide cambios en tablas permanentes y se utiliza para revisar reportes contra datos existentes.

## Duplicados y decisiones

El criterio mínimo es id_venta, no el nombre del producto. Los IDs se normalizan antes de comparar. Un mismo ID con datos distintos no debe mezclarse ni corregirse sin criterio: el programa registra la contradicción y conserva el primero válido o la fila previamente almacenada.

La clave primaria protege la tabla. ON CONFLICT evita duplicación en una reejecución. La comparación de precio usa BigDecimal.compareTo: 250 y 250.00 tienen el mismo valor económico. El conjunto de IDs se reinicia en cada llamada de deduplicación.

Ejercicio: añadir a una copia temporal del JSON una venta con el mismo ID y otra cantidad. Explicar por qué se registra contradicción y por qué la cantidad almacenada no cambia.

## Cómo se calculan los indicadores

Cada venta tiene monto = cantidad × precio unitario. SUM suma esos montos; AVG calcula el promedio **por transacción**, no el precio promedio de productos. COUNT cuenta ventas, no unidades vendidas.

GROUP BY reúne por mes, sucursal, producto o vendedor. UPPER impide que Carlos y carlos se dividan en dos grupos. ORDER BY monto DESC y LIMIT 3 construyen el top 3. Los extremos usan ASC o DESC y LIMIT 1; una etiqueta desempata.

Rechazados / recibidos × 100 usa exclusivamente los registros de archivos legibles de la ejecución actual. Con las entradas originales hay 11 recibidos y 6 rechazados: 54,55 %. Un duplicado válido no es un rechazo. Las cuatro ventas almacenadas suman Q11350,00 y su promedio es Q2837,50 en la evidencia auditada.

Ejercicio: comprobar las fórmulas con una consulta SQL independiente y distinguir estadísticas del procesamiento de las acumuladas en la tabla.

## Cómo modificar una validación

Supongamos que el profesor cambia el máximo de caracteres de sucursal. Buscar la regla en ValidadorVenta, cambiarla y ajustar el VARCHAR y la restricción del script para una base nueva. Una tabla existente requiere una migración revisada. Añadir una prueba con el valor límite y otra que lo supere. Ejecutar mvn test antes de procesar datos reales.

No esconder errores reemplazando cantidades negativas por valores positivos. No redondear precios silenciosamente: una normalización de espacios es diferente de alterar un importe.

## Cómo localizar y corregir un error

1. Reproducirlo con el archivo más pequeño posible y guardar el mensaje.
2. Identificar la etapa: estructura, conversión, validación, duplicados, JDBC o reporte.
3. Leer el archivo/registro y el motivo en errores_procesamiento.txt.
4. Escribir una prueba que reproduzca el problema; realizar el cambio pequeño y repetirla.
5. Ejecutar el resto de pruebas para evitar regresiones.

Ejemplos reales: un encabezado CSV desplazado por una línea vacía se contaba como venta; se resuelve reconociendo el primer registro lógico. Un XML con una línea antes de la declaración es mal formado; corregir su origen. Un POM con dos groupId dentro de dependency no carga; cada biblioteca necesita su bloque. SQLState 23514 indica una restricción; 08 suele indicar conectividad; 28 autenticación. Nunca compartir contraseñas ni todo el archivo privado para depurar.
