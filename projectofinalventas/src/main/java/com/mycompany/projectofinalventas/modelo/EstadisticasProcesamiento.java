
package com.mycompany.projectofinalventas.modelo;

public class EstadisticasProcesamiento {

    public int recibidos;
    public int validos;
    public int rechazados;
    public int duplicados;
    public int unicos;

    public int insertados;
    public int existentesBD;
    public int fallosBD;
    public int archivosFallidos;

    public double porcentajeRechazados() {

        if (recibidos == 0) {
            return 0;
        }

        return (rechazados * 100.0) / recibidos;
    }
}
