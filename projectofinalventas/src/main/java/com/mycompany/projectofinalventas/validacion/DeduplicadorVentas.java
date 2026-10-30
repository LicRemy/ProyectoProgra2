package com.mycompany.projectofinalventas.validacion;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeduplicadorVentas {
    private int duplicados;
    public List<RegistroVenta> eliminarDuplicados(List<RegistroVenta> ventas, List<String> errores) {
        Map<String, RegistroVenta> vistas = new LinkedHashMap<>();
        List<RegistroVenta> unicas = new ArrayList<>();
        duplicados = 0; // El estado corresponde solo a esta llamada.
        ValidadorVenta validador = new ValidadorVenta();
        for (RegistroVenta original : ventas) {
            RegistroVenta venta = validador.normalizar(original);
            RegistroVenta anterior = vistas.putIfAbsent(venta.getIdVenta(), venta);
            if (anterior == null) unicas.add(venta);
            else {
                duplicados++;
                errores.add(venta.getOrigen() + " ID=" + venta.getIdVenta()
                        + (anterior.mismosDatos(venta) ? ": duplicado identico" : ": duplicado CONTRADICTORIO")
                        + "; se conserva el primero de " + anterior.getOrigen());
            }
        }
        return unicas;
    }
    public int getDuplicados() { return duplicados; }
}
