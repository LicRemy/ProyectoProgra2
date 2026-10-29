
package com.mycompany.projectofinalventas.app;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        System.out.println("SISTEMA DE ANALISIS DE VENTAS");

        // Registro de prueba, no datos definitivos
        RegistroVenta venta = new RegistroVenta(
                "V001",
                LocalDate.of(2026, 10, 29),
                "Chiquimula",
                "TEC001",
                "Teclado",
                2,
                new BigDecimal("250.00"),
                "Carlos"
        );

        System.out.println("ID: " + venta.getIdVenta());
        System.out.println("Producto: " + venta.getProducto());
        System.out.println("Cantidad: " + venta.getCantidad());
        System.out.println("Total: Q" + venta.calcularTotal());
    }
}
