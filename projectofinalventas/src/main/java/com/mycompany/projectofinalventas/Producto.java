
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
        System.out.println("Cantidad: " + cantidad);
    }
}
