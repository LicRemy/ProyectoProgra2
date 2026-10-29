
package com.mycompany.projectofinalventas;

import java.util.ArrayList;
import java.util.Scanner;

public class Projectofinalventas {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);
        ArrayList<Producto> inventario = new ArrayList<>();
        ArrayList<Venta> historial = new ArrayList<>();

        // Productos iniciales
        inventario.add(new Producto("Teclado", 250.00, 10));
        inventario.add(new Producto("Mouse", 75.50, 5));
        inventario.add(new Producto("Monitor", 1200.00, 3));

        int opcion = 0;

        while (opcion != 6) {

            System.out.println("\n===== SISTEMA DE VENTAS =====");
            System.out.println("1. Agregar producto");
            System.out.println("2. Mostrar inventario");
            System.out.println("3. Buscar producto");
            System.out.println("4. Registrar venta");
            System.out.println("5. Historial y reporte de ingresos");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opcion: ");

            if (teclado.hasNextInt()) {
                opcion = teclado.nextInt();
                teclado.nextLine();
            } else {
                System.out.println("Ingrese un numero valido.");
                teclado.nextLine();
                continue;
            }

            switch (opcion) {

                case 1:
                    System.out.println("\nAGREGAR PRODUCTO");

                    System.out.print("Nombre: ");
                    String nombre = teclado.nextLine();

                    System.out.print("Precio: ");
                    if (!teclado.hasNextDouble()) {
                        System.out.println("Precio invalido.");
                        teclado.nextLine();
                        break;
                    }
                    double precio = teclado.nextDouble();

                    System.out.print("Cantidad: ");
                    if (!teclado.hasNextInt()) {
                        System.out.println("Cantidad invalida.");
                        teclado.nextLine();
                        break;
                    }
                    int cantidad = teclado.nextInt();
                    teclado.nextLine();

                    if (nombre.trim().isEmpty()
                            || precio < 0 || cantidad < 0) {
                        System.out.println("Datos invalidos.");
                        break;
                    }

                    Producto nuevo = new Producto(
                            nombre, precio, cantidad);

                    inventario.add(nuevo);

                    System.out.println("Producto agregado.");
                    break;

                case 2:
                    System.out.println("\nINVENTARIO");

                    if (inventario.isEmpty()) {
                        System.out.println("Inventario vacio.");
                    } else {
                        for (Producto p : inventario) {
                            p.mostrarInformacion();
                            System.out.println("----------------");
                        }
                    }
                    break;

                case 3:
                    System.out.println("\nBUSCAR PRODUCTO");
                    System.out.print("Nombre: ");

                    String busqueda = teclado.nextLine();
                    boolean encontrado = false;

                    for (Producto p : inventario) {
                        if (p.nombre.equalsIgnoreCase(
                                busqueda.trim())) {

                            p.mostrarInformacion();
                            encontrado = true;
                        }
                    }

                    if (!encontrado) {
                        System.out.println("Producto no encontrado.");
                    }
                    break;

                case 4:
                    System.out.println("\nREGISTRAR VENTA");

                    System.out.print("Producto a vender: ");
                    String productoVenta = teclado.nextLine();

                    Producto seleccionado = null;

                    // Buscar el producto
                    for (Producto p : inventario) {
                        if (p.nombre.equalsIgnoreCase(
                                productoVenta.trim())) {
                            seleccionado = p;
                            break;
                        }
                    }

                    if (seleccionado == null) {
                        System.out.println("Producto no encontrado.");
                        break;
                    }

                    // Mostrar informacion
                    seleccionado.mostrarInformacion();

                    System.out.print("Unidades a vender: ");

                    if (!teclado.hasNextInt()) {
                        System.out.println("Cantidad invalida.");
                        teclado.nextLine();
                        break;
                    }

                    int unidades = teclado.nextInt();
                    teclado.nextLine();

                    // Verificar existencias
                    if (!seleccionado.hayExistencias(unidades)) {
                        System.out.println(
                                "Cantidad invalida o existencias insuficientes.");
                        break;
                    }

                    // Registrar venta
                    double total = seleccionado.vender(unidades);
                    
                    Venta nuevaVenta = new Venta(
                        seleccionado.nombre,
                        unidades,
                        seleccionado.precio
                    );

                    historial.add(nuevaVenta);

                    System.out.println("\n===== FACTURA =====");
                    System.out.println("Producto: "
                            + seleccionado.nombre);
                    System.out.println("Unidades: " + unidades);
                    System.out.printf("Total: Q%.2f%n", total);
                    System.out.println("Venta realizada correctamente.");
                    System.out.println("Existencias restantes: "
                            + seleccionado.cantidad);
                    break;

                     
                    case 5:
    System.out.println("\n===== HISTORIAL DE VENTAS =====");

    if (historial.isEmpty()) {

        System.out.println("Todavia no se han realizado ventas.");

    } else {

        double ingresosTotales = 0;

        for (Venta v : historial) {

            v.mostrarVenta();

            ingresosTotales = ingresosTotales + v.total;
        }

        System.out.println("\n===== REPORTE GENERAL =====");
        System.out.println("Numero de ventas: "
                + historial.size());

        System.out.printf("Ingresos totales: Q%.2f%n",
                ingresosTotales);
    }
    break;


                case 6:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opcion no valida.");
                    break;
            }
        }
    }
}
