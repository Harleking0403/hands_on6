package simulacion;

import java.util.Locale;
import java.util.Properties;
import java.util.Random;

public final class AgenteEstocastico extends AgenteEspecialista {
    @Override
    protected String calcular(Properties datos) {
        double media;
        double umbral;
        int iteraciones;
        long semilla;
        try {
            media = Double.parseDouble(datos.getProperty("media", ""));
            umbral = Double.parseDouble(datos.getProperty("umbral", ""));
            iteraciones = Integer.parseInt(datos.getProperty("iteraciones", ""));
            semilla = Long.parseLong(datos.getProperty("semilla", ""));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                "Usa punto decimal para la media y el umbral, y números enteros para las simulaciones y la semilla.", error);
        }

        if (!Double.isFinite(media) || media <= 0 || !Double.isFinite(umbral) || umbral < 0) {
            throw new IllegalArgumentException("La media debe ser positiva y el umbral debe ser mayor o igual a cero.");
        }
        if (iteraciones < 1 || iteraciones > 1_000_000) {
            throw new IllegalArgumentException("Ingresa entre 1 y 1,000,000 de simulaciones.");
        }

        Random azar = new Random(semilla);
        int fallas = 0;
        double umbralNormalizado = umbral / media;
        for (int i = 0; i < iteraciones; i++) {
            double latenciaNormalizada = -Math.log1p(-azar.nextDouble());
            if (latenciaNormalizada > umbralNormalizado) {
                fallas++;
            }
        }

        return String.format(Locale.ROOT,
            "Transformada inversa · Escenario de falla SLA%n" +
            "Latencia exponencial con media de %s ms%n" +
            "Se cuenta una falla cuando la latencia supera %s ms.%n%n" +
            "Fallas: %d de %d simulaciones%n" +
            "Probabilidad estimada de falla SLA: %.2f%%%n" +
            "Probabilidad teórica del modelo: %.2f%%%n" +
            "Semilla: %d",
            datos.getProperty("media"), datos.getProperty("umbral"), fallas, iteraciones, 100.0 * fallas / iteraciones,
            100.0 * Math.exp(-umbralNormalizado), semilla);
    }
}
