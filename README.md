# Sistema de Integración y Análisis de Datos de Ventas

**Proyecto Final de Programación II**

## 1. Descripción

Aplicación de consola desarrollada en Java para integrar registros de ventas provenientes de archivos CSV, JSON y XML.

El sistema permite convertir los registros a objetos Java, validar y normalizar sus datos, detectar identificadores duplicados, almacenar ventas mediante JDBC en PostgreSQL y producir análisis estadísticos y reportes de texto.

## 2. Tecnologías utilizadas

- Java
- Apache NetBeans
- Maven
- PostgreSQL
- pgAdmin
- PostgreSQL JDBC Driver
- Jackson para procesamiento JSON
- Herramientas XML incluidas en Java
- Git y GitHub

## 3. Estructura del proyecto

- `modelo`: clases que representan los registros y las estadísticas.
- `lectores`: procesamiento de archivos CSV, JSON y XML.
- `validacion`: reglas de validación, normalización y detección de duplicados.
- `datos`: conexión JDBC y operaciones de base de datos.
- `analisis`: consultas y cálculos estadísticos.
- `reportes`: generación de archivos de salida.
- `app`: inicio y coordinación del programa.
- `sql`: script de creación de la tabla.
- `entrada`: archivos de prueba y de procesamiento.
- `config`: ejemplo de configuración para PostgreSQL.

## 4. Configuración de PostgreSQL

1. Iniciar PostgreSQL y abrir pgAdmin.
2. Crear una base de datos llamada `analisis_ventas`.
3. Abrir Query Tool conectado a esa base de datos.
4. Ejecutar `sql/crear_base_datos.sql`.
5. Copiar `config/db.properties.example` a `config/db.properties`.
6. Configurar el usuario, contraseña, servidor y puerto de PostgreSQL.

El archivo `config/db.properties` contiene credenciales locales y no debe subirse a GitHub.

## 5. Ejecución

1. Abrir el proyecto Maven en NetBeans.
2. Verificar que las dependencias estén disponibles.
3. Ejecutar Clean and Build.
4. Colocar los archivos de entrada en la carpeta `entrada`.
5. Ejecutar `com.mycompany.projectofinalventas.app.Main`.
6. Revisar los resultados en consola y los archivos TXT generados.

La ejecución presupone que el directorio de trabajo corresponde a la raíz del proyecto.

## 6. Reglas de procesamiento

Cada registro debe contener identificador de venta, fecha, sucursal, código de producto, nombre de producto, cantidad, precio unitario y vendedor.

Se rechazan registros con campos obligatorios vacíos, fechas inválidas, cantidades no positivas o precios no positivos.

Se normalizan espacios sobrantes e identificadores, sucursales y códigos de producto a mayúsculas.

Los duplicados entre los archivos de una misma ejecución se detectan por `id_venta`. PostgreSQL utiliza una clave primaria para evitar nuevas inserciones de IDs existentes.

## 7. Análisis disponibles

El programa calcula total vendido, promedio por transacción, ventas por mes, sucursal, producto y vendedor, productos y transacciones con montos extremos y los tres vendedores con mayor monto vendido.

Los indicadores económicos se obtienen mediante consultas SQL sobre PostgreSQL.

## 8. Reportes

- `reporte_analisis.txt`: estadísticas del procesamiento y análisis de ventas almacenadas.
- `errores_procesamiento.txt`: incidencias de validación, lectura y registros duplicados.

## 9. Decisiones técnicas

Se utilizó una arquitectura por paquetes y responsabilidades para facilitar la comprensión y el mantenimiento.

La conexión a PostgreSQL se realiza mediante JDBC y `PreparedStatement`.

Los recursos de entrada/salida y las conexiones de base de datos se cierran mediante `try-with-resources`.

Se utiliza `BigDecimal` para los valores monetarios.

## 10. Pruebas y limitaciones

Antes de la entrega deben verificarse los lectores con los archivos oficiales, los casos límite, el conteo exacto de registros, las ejecuciones repetidas y la coincidencia entre consultas SQL y reportes.

Las versiones iniciales de los lectores fueron desarrolladas usando archivos de prueba con estructuras conocidas. Su compatibilidad con variantes adicionales debe comprobarse y documentarse.

## 11. Integrantes

- Integrante 1: Jonathan Daniel Alberto Aguilar Ramírez
- Integrante 2: Luis Alberto Matta Rubio