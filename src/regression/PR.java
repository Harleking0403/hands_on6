package regression;

import java.util.Locale;

public final class PR {
    private final MLR modelo;
    public PR(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 3) {
            throw new IllegalArgumentException("Se necesitan al menos 3 pares de datos.");
        }
        double[][] entradas = new double[x.length][2];
        for (int i = 0; i < x.length; i++) {
            MLR.comprobarFinito(x[i]);
            entradas[i][0] = x[i];
            entradas[i][1] = x[i] * x[i];
        }
        modelo = new MLR(entradas, y);
    }
    public double predecir(double x) { return modelo.predecir(x, x*x); }
    public double[] getBetas() { return modelo.getBetas(); }
    public double calcularR2() { return modelo.calcularR2(); }
    public String obtenerEcuacion() {
        double[] b = modelo.getBetas();
        return String.format(Locale.US,"y = %.4f %s %.4f*x %s %.4f*x^2",
                b[0],b[1]<0?"-":"+",Math.abs(b[1]), b[2]<0?"-":"+",Math.abs(b[2]));
    }
}
