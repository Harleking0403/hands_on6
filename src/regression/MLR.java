
package regression;

import java.util.Locale;

public final class MLR {
    private final double[][] x;
    private final double[] y;
    private final double[] beta;

    public MLR(double[][] x, double[] y) {
        if (x == null || y == null || x.length < 2 || x.length != y.length
                || x[0] == null || x[0].length == 0) {
            throw new IllegalArgumentException("Dataset inválido.");
        }
        int p = x[0].length;
        if (x.length <= p) {
            throw new IllegalArgumentException("Se necesitan más observaciones que predictores.");
        }
        this.x = new double[x.length][p];
        for (int i = 0; i < x.length; i++) {
            if (x[i] == null || x[i].length != p) {
                throw new IllegalArgumentException("Cada fila debe tener " + p + " predictores.");
            }
            for (int j = 0; j < p; j++) {
                comprobarFinito(x[i][j]);
                this.x[i][j] = x[i][j];
            }
        }
        this.y = y.clone();
        for (double v : this.y) comprobarFinito(v);
        this.beta = ajustar();
    }

    private double[] ajustar() {
        int n = x.length, p = x[0].length;
        double[] mediasX = new double[p], escalas = new double[p];
        double mediaY = 0;
        for (double v : y) mediaY += v / n;
        for (int j = 0; j < p; j++) {
            for (int i = 0; i < n; i++) mediasX[j] += x[i][j] / n;
            for (int i = 0; i < n; i++) {
                double d = x[i][j] - mediasX[j];
                escalas[j] += d * d;
            }
            escalas[j] = Math.sqrt(escalas[j]);
            if (!(escalas[j] > 0) || !Double.isFinite(escalas[j])) {
                throw new IllegalArgumentException("Un predictor es constante o inválido: columna " + (j + 1));
            }
        }
        double[][] q = new double[p][n];
        double[][] r = new double[p][p];
        for (int k = 0; k < p; k++) {
            double[] v = new double[n];
            for (int i = 0; i < n; i++) v[i] = (x[i][k] - mediasX[k]) / escalas[k];
            for (int paso = 0; paso < 2; paso++) {
                for (int j = 0; j < k; j++) {
                    double proyeccion = 0;
                    for (int i = 0; i < n; i++) proyeccion += q[j][i] * v[i];
                    r[j][k] += proyeccion;
                    for (int i = 0; i < n; i++) v[i] -= proyeccion * q[j][i];
                }
            }
            double magnitud = 0;
            for (double e : v) magnitud += e * e;
            r[k][k] = Math.sqrt(magnitud);
            if (r[k][k] < 1e-10 || !Double.isFinite(r[k][k])) {
                throw new IllegalArgumentException("Predictores dependientes: no existe solución única.");
            }
            for (int i = 0; i < n; i++) q[k][i] = v[i] / r[k][k];
        }

        double[] z = new double[p], t = new double[p];
        for (int k = 0; k < p; k++) {
            for (int i = 0; i < n; i++) z[k] += q[k][i] * (y[i] - mediaY);
        }
        for (int k = p - 1; k >= 0; k--) {
            double v = z[k];
            for (int j = k + 1; j < p; j++) v -= r[k][j] * t[j];
            t[k] = v / r[k][k];
        }
        double[] b = new double[p + 1];
        b[0] = mediaY;
        for (int j = 0; j < p; j++) {
            b[j + 1] = t[j] / escalas[j];
            b[0] -= b[j + 1] * mediasX[j];
        }
        for (double v : b) comprobarFinito(v);
        return b;
    }

    public static void comprobarFinito(double v) {
        if (!Double.isFinite(v)) throw new IllegalArgumentException("Los valores deben ser números finitos.");
    }

    public double predecir(double... valores) {
        if (valores == null || valores.length != beta.length - 1) {
            throw new IllegalArgumentException("Cantidad incorrecta de predictores.");
        }
        double resultado = beta[0];
        for (int j = 0; j < valores.length; j++) {
            comprobarFinito(valores[j]);
            resultado += beta[j + 1] * valores[j];
        }
        return resultado;
    }

    public double[] getBetas() { return beta.clone(); }

    public double calcularR2() {
        double media = 0;
        for (double v : y) media += v / y.length;
        double error = 0, total = 0;
        for (int i = 0; i < y.length; i++) {
            double d = y[i] - predecir(x[i]);
            error += d * d;
            double t = y[i] - media;
            total += t * t;
        }
        return total == 0 ? (error < 1e-10 ? 1.0 : Double.NaN) : 1.0 - error / total;
    }

    public String obtenerEcuacion(String... nombres) {
        if (nombres == null || nombres.length != beta.length - 1) {
            throw new IllegalArgumentException("Se necesita un nombre por cada predictor.");
        }
        StringBuilder sb = new StringBuilder(String.format(Locale.US,"y = %.4f",beta[0]));
        for (int i = 1; i < beta.length; i++) {
            sb.append(String.format(Locale.US," %s %.4f*%s", beta[i]<0?"-":"+",Math.abs(beta[i]), nombres[i-1]));
        }
        return sb.toString();
    }
}
