
package com.mycompany.projectofinalventas;


public class Producto {
    //variables del producto
    String nombre;
    double precio;
    int cantidad;

    //orden de atributos de producto
    public Producto(String nombre, double precio, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    
    //Comando mostrarInformacion() que enseña el producto guardado
    public void mostrarInformacion() {
        System.out.println("Producto: " + nombre);
        System.out.println("Precio: Q" + precio);
        System.out.println("Cantidad: " + cantidad);
    }
}
