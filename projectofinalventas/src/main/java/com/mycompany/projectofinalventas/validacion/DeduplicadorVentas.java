
package com.mycompany.projectofinalventas.validacion;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DeduplicadorVentas {

    private final Set<String> identificadores = new HashSet<>();

    private int duplicados = 0;

    public List<RegistroVenta> eliminarDuplicados(
            List<RegistroVenta> ventas,
            List<String> errores) {

        List<RegistroVenta> unicas = new ArrayList<>();

        for (RegistroVenta venta : ventas) {

            String id = venta.getIdVenta()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (identificadores.add(id)) {

                unicas.add(venta);

            } else {

                duplicados++;

                errores.add(
                        "Venta duplicada. ID: " + id);
            }
        }

        return unicas;
    }

    public int getDuplicados() {
        return duplicados;
    }
}
