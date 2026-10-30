# Resumen listo para un commit

Título sugerido: Auditar lectores, asegurar persistencia y completar pruebas y documentación

- Corregir CSV con encabezados, comillas, BOM y campos multilínea mediante Apache Commons CSV.
- Centralizar conversión y validación; preservar trazabilidad de archivo y registro.
- Corregir contadores y separar fallos de archivo, rechazos, duplicados y conflictos JDBC.
- Descubrir entradas nuevas sin modificar Main; conservar el punto de entrada anterior como delegación.
- Detectar IDs contradictorios sin sobrescribir datos.
- Reemplazar SQL MySQL por esquema PostgreSQL con restricciones.
- Unificar consultas SQL de consola y TXT; generar reportes reproducibles y señalar fallos.
- Fijar Java 17, plugins Maven y empaquetado con dependencias.
- Añadir JUnit y pruebas con tabla temporal PostgreSQL y rollback.
- Actualizar README, auditoría, guía de defensa y documento explicativo.
- Retirar del índice la configuración privada y añadir un ejemplo sin secretos.

No se ejecutó commit ni push. Revisar git diff y git diff --cached antes de registrar cambios. La eliminación del índice afecta solo al seguimiento de config/db.properties: su copia local se conserva. El historial previo puede contener credenciales; resolver exposición mediante cambio de contraseña y coordinación del equipo, sin falsificar autorías.
