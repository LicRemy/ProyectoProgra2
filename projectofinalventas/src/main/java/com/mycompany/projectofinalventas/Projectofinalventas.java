
package com.mycompany.projectofinalventas;

import java.util.ArrayList;

public class Projectofinalventas {

    public static void main(String[] args) {

        // Creamos una lista para guardar productos
        ArrayList<Producto> inventario = new ArrayList<>();

        // Creamos tres productos
        Producto producto1 = new Producto("Teclado", 250.00, 10);
        Producto producto2 = new Producto("Mouse", 75.50, 5);
        Producto producto3 = new Producto("Monitor", 1200.00, 3);
        Producto producto4 = new Producto("Gomitas", 6900.00, 67);

        // Agregamos los productos a la lista
        inventario.add(producto1);
        inventario.add(producto2);
        inventario.add(producto3);
        inventario.add(producto4);

        // Mostramos todos los productos
        System.out.println("=== INVENTARIO DE LA TIENDA ===");

        for (Producto producto : inventario) {
            producto.mostrarInformacion();
            System.out.println("--------------------");
        }

        System.out.println("Total de productos diferentes: "
                + inventario.size());
    }
}
