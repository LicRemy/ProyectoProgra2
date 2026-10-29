
package com.mycompany.projectofinalventas;

public class Producto {

    String nombre;
    double precio;
    int cantidad;

    public Producto(String nombre, double precio, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public void mostrarInformacion() {
        System.out.println("Producto: " + nombre);
        System.out.println("Precio: Q" + precio);
        System.out.println("Cantidad disponible: " + cantidad);
    }

    // Comprueba si tenemos suficientes productos
    public boolean hayExistencias(int unidades) {
        return unidades > 0 && unidades <= cantidad;
    }

    // Descuenta productos y devuelve el total
    public double vender(int unidades) {

        if (hayExistencias(unidades)) {

            double total = precio * unidades;

            cantidad = cantidad - unidades;

            return total;
        }

        return 0;
    }
}
