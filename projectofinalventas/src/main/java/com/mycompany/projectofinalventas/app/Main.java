
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.lectores.LectorCSV;
import com.mycompany.projectofinalventas.modelo.RegistroVenta;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== SISTEMA DE ANALISIS DE VENTAS ===");

        // Crear nuestro lector
        LectorCSV lector = new LectorCSV();

        // Leer archivo de prueba
        ArrayList<RegistroVenta> ventas =
                lector.leer("entrada/ventas.csv");

        System.out.println("\nREGISTROS LEIDOS: " + ventas.size());

        // Mostrar los registros obtenidos
        for (RegistroVenta venta : ventas) {

            System.out.println("------------------------");

            System.out.println("ID: " + venta.getIdVenta());
            System.out.println("Fecha: " + venta.getFecha());
            System.out.println("Sucursal: " + venta.getSucursal());
            System.out.println("Producto: " + venta.getProducto());
            System.out.println("Cantidad: " + venta.getCantidad());
            System.out.println("Vendedor: " + venta.getVendedor());

            System.out.println(
                    "Total: Q" + venta.calcularTotal());
        }
    }
}
