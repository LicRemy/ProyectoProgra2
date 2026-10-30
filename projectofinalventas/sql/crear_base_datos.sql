
CREATE DATABASE IF NOT EXISTS analisis_ventas;

USE analisis_ventas;

CREATE TABLE IF NOT EXISTS ventas (
    id_venta VARCHAR(50) PRIMARY KEY,
    fecha DATE NOT NULL,
    sucursal VARCHAR(100) NOT NULL,
    codigo_producto VARCHAR(50) NOT NULL,
    producto VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    vendedor VARCHAR(100) NOT NULL,
    CHECK (cantidad > 0),
    CHECK (precio_unitario > 0)
);
