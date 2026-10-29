
package com.mycompany.projectofinalventas;

public class Venta {

    String nombreProducto;
    int unidades;
    double precioUnitario;
    double total;

    // Constructor
    public Venta(String nombreProducto, int unidades,
                 double precioUnitario) {

        this.nombreProducto = nombreProducto;
        this.unidades = unidades;
        this.precioUnitario = precioUnitario;

        // Calculamos el total de la venta
        this.total = unidades * precioUnitario;
    }

    // Mostrar la informacion de una venta
    public void mostrarVenta() {

        System.out.println("------------------------");
        System.out.println("Producto: " + nombreProducto);
        System.out.println("Unidades vendidas: " + unidades);
        System.out.printf("Precio unitario: Q%.2f%n", precioUnitario);
        System.out.printf("Total: Q%.2f%n", total);
    }
}
