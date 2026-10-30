package com.mycompany.projectofinalventas.modelo;

public class EstadisticasProcesamiento {
    public int recibidos;
    public int validos;
    public int rechazados;
    public int duplicados;
    public int unicos;
    public int insertados;
    public int existentesBD;
    public int conflictosBD;
    public int fallosBD;
    public int archivosFallidos;
    public int archivosProcesados;

    /** Solo rechazos de registros / registros de archivos legibles. */
    public double porcentajeRechazados() {
        return recibidos == 0 ? 0 : rechazados * 100.0 / recibidos;
    }

    public void comprobar() {
        if (recibidos != validos + rechazados || validos != unicos + duplicados
                || unicos != insertados + existentesBD + conflictosBD + fallosBD) {
            throw new IllegalStateException("Contadores de procesamiento inconsistentes");
        }
    }
}
