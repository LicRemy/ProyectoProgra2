
-- PostgreSQL: crear analisis_ventas desde pgAdmin y ejecutar conectado a ella.
-- No elimina datos ni altera tablas existentes; IF NOT EXISTS no es una migracion.

CREATE TABLE IF NOT EXISTS ventas (
    id_venta VARCHAR(50) PRIMARY KEY CHECK (btrim(id_venta) <> '' AND id_venta = upper(btrim(id_venta))),
    fecha DATE NOT NULL,
    sucursal VARCHAR(100) NOT NULL,
    codigo_producto VARCHAR(50) NOT NULL,
    producto VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    vendedor VARCHAR(100) NOT NULL,
    CHECK (cantidad > 0),
    CHECK (precio_unitario > 0),
    CHECK (fecha >= DATE '0001-01-01' AND fecha <= DATE '9999-12-31'),
    CHECK (btrim(sucursal) <> ''),
    CHECK (btrim(codigo_producto) <> ''),
    CHECK (btrim(producto) <> ''),
    CHECK (btrim(vendedor) <> '')
);
