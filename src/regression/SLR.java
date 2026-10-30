package regression;

public class SLR {

    private final double[] x;
    private final double[] y;
    private double beta0;
    private double beta1;

    public SLR(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length
                || x.length < 2) {
            throw new IllegalArgumentException("Dataset inválido.");
        }

        for (double v : x) MLR.comprobarFinito(v);
        for (double v : y) MLR.comprobarFinito(v);
        this.x = x.clone();
        this.y = y.clone();
        calcularBetas();
    }

    private void calcularBetas() {
        int n = x.length;
        double sumaX = 0, sumaY = 0;

        for (int i = 0; i < n; i++) {
            sumaX += x[i];
            sumaY += y[i];
        }

        double mediaX = sumaX / n;
        double mediaY = sumaY / n;
        double numerador = 0, denominador = 0;

        for (int i = 0; i < n; i++) {
            numerador += (x[i] - mediaX) * (y[i] - mediaY);
            denominador += Math.pow(x[i] - mediaX, 2);
        }

        if (denominador == 0) {
            throw new IllegalArgumentException(
                "Todos los valores de x son iguales."
            );
        }

        beta1 = numerador / denominador;
        beta0 = mediaY - beta1 * mediaX;
    }

    public double predecir(double valorX) {
        MLR.comprobarFinito(valorX);
        return beta0 + beta1 * valorX;
    }

    public double getBeta0() {
        return beta0;
    }

    public double getBeta1() {
        return beta1;
    }

    public double calcularR2() {
        double mediaY = 0;

        for (double valor : y) {
            mediaY += valor;
        }
        mediaY /= y.length;

        double error = 0, variacion = 0;

        for (int i = 0; i < y.length; i++) {
            error += Math.pow(y[i] - predecir(x[i]), 2);
            variacion += Math.pow(y[i] - mediaY, 2);
        }

        if (variacion == 0) {
            return error == 0 ? 1.0 : Double.NaN;
        }

        return 1.0 - error / variacion;
    }

    public String obtenerEcuacion() {
        return String.format(
            java.util.Locale.US,
            "y = %.4f %s %.4f*x",
            beta0, beta1 < 0 ? "-" : "+", Math.abs(beta1)
        );
    }
}
