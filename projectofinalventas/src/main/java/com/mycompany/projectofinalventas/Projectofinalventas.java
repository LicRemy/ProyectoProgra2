
package com.mycompany.projectofinalventas;

import java.util.ArrayList;
import java.util.Scanner;

public class Projectofinalventas {

    public static void main(String[] args) {
        
        //registra teclado
        Scanner teclado = new Scanner(System.in);

        //array inventario
        ArrayList<Producto> inventario = new ArrayList<>();

        // Productos iniciales de prueba
        inventario.add(new Producto("Teclado", 250.00, 10));
        inventario.add(new Producto("Mouse", 75.50, 5));
        inventario.add(new Producto("Monitor", 1200.00, 3));

        //variable para menu
        int opcion = 0;
        
        //lista de opciones para menu
        while (opcion != 4) {

            System.out.println("\n===== SISTEMA DE VENTAS =====");
            System.out.println("1. Agregar producto");
            System.out.println("2. Mostrar inventario");
            System.out.println("3. Buscar producto");
            System.out.println("4. Salir");

            System.out.print("Seleccione una opcion: ");

            //registra el numero que pulsamos, else en caso no sea valido
            if (teclado.hasNextInt()) {
                opcion = teclado.nextInt();
                teclado.nextLine();
            } else {
                System.out.println("Ingrese un numero valido.");
                teclado.nextLine();
                continue;
            }

            //switch para ir a las opciones del menu
            switch (opcion) {
                
                //caso 1 de agregar producto
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

                    System.out.println("Producto agregado correctamente.");
                    break;

                    //caso 2 de mostrar inventario
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

                    //caso 3 buscador de producto
                case 3:
                    System.out.println("\nBUSCAR PRODUCTO");
                    System.out.print("Ingrese el nombre: ");

                    String busqueda = teclado.nextLine();
                    boolean encontrado = false;

                    for (Producto p : inventario) {
                        if (p.nombre.equalsIgnoreCase(busqueda.trim())) {
                            p.mostrarInformacion();
                            encontrado = true;
                        }
                    }

                    if (!encontrado) {
                        System.out.println("Producto no encontrado.");
                    }
                    break;

                    //caso 4 salir del sistema
                case 4:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opcion no valida.");
                    break;
            }
        }
    }
}
