package simulacion;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Properties;

public final class AgentePredictivo extends AgenteEspecialista {
    @Override
    protected String calcular(Properties datos) {
        double[][] observaciones = leerTabla(datos.getProperty("observaciones", ""), "Observaciones");
        double[][] entradas = leerTabla(datos.getProperty("entradas", ""), "Entradas a predecir");
        int variables = observaciones[0].length - 1;
        if (variables < 1 || entradas[0].length != variables || entradas.length < 5) {
            throw new IllegalArgumentException(
                "Cada observación requiere sus variables X y un valor Y. Ingresa al menos cinco filas de predicción con solo las X.");
        }

        String campoGrado = datos.getProperty("grado", "").strip();
        int grado;
        try {
            grado = campoGrado.isEmpty() ? 1 : Integer.parseInt(campoGrado);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Escribe el grado como número entero.", error);
        }
        if (grado < 1 || grado > 5 || (variables > 1 && grado > 1)) {
            throw new IllegalArgumentException("El grado debe ir de 1 a 5. Con varias variables solo se admite el grado 1.");
        }

        int terminos = grado > 1 ? grado + 1 : variables + 1;
        if (observaciones.length < terminos) {
            throw new IllegalArgumentException("Se necesitan al menos " + terminos + " observaciones para ese modelo.");
        }

        double[][] diseno = new double[observaciones.length][terminos];
        double[] objetivos = new double[observaciones.length];
        for (int i = 0; i < observaciones.length; i++) {
            diseno[i] = terminos(observaciones[i], grado, terminos);
            objetivos[i] = observaciones[i][variables];
        }

        double[] coeficientes = ajustar(diseno, objetivos);
        String modelo;
        if (grado > 1) {
            modelo = "Regresión polinomial (PR, grado " + grado + ")";
        } else if (variables == 1) {
            modelo = "Regresión lineal simple (SLR)";
        } else {
            modelo = "Regresión lineal múltiple (MLR)";
        }

        StringBuilder salida = new StringBuilder(modelo).append("\nŷ = ").append(numero(coeficientes[0]));
        for (int i = 1; i < coeficientes.length; i++) {
            String variable = variables == 1 ? "x" : "x" + i;
            if (grado > 1 && i > 1) {
                variable += "^" + i;
            }
            salida.append(coeficientes[i] < 0 ? " − " : " + ")
                .append(numero(Math.abs(coeficientes[i]))).append("·").append(variable);
        }

        salida.append("\n\nPredicciones\n");
        for (int i = 0; i < entradas.length; i++) {
            double[] columnas = terminos(entradas[i], grado, terminos);
            double prediccion = 0;
            for (int j = 0; j < coeficientes.length; j++) {
                prediccion += coeficientes[j] * columnas[j];
            }
            salida.append(i + 1).append(". (");
            for (int j = 0; j < variables; j++) {
                if (j > 0) {
                    salida.append(", ");
                }
                salida.append(numero(entradas[i][j]));
            }
            salida.append(") → ").append(numero(prediccion)).append("\n");
        }
        return salida.toString().stripTrailing();
    }

    private static double[] ajustar(double[][] diseno, double[] objetivos) {
        double[] escalas = new double[diseno[0].length];
        for (int j = 0; j < escalas.length; j++) {
            for (double[] fila : diseno) {
                escalas[j] = Math.max(escalas[j], Math.abs(fila[j]));
            }
            if (!Double.isFinite(escalas[j]) || escalas[j] == 0) {
                throw new IllegalArgumentException("Hay variables sin variación útil o valores demasiado grandes.");
            }
            for (double[] fila : diseno) {
                fila[j] /= escalas[j];
            }
        }

        DecompositionSolver solver = new QRDecomposition(new Array2DRowRealMatrix(diseno, false), 1e-12).getSolver();
        if (!solver.isNonSingular()) {
            throw new IllegalArgumentException("No se puede ajustar la regresión. Revisa las variables constantes o dependientes.");
        }

        double[] coeficientes = solver.solve(new ArrayRealVector(objetivos, false)).toArray();
        for (int j = 0; j < coeficientes.length; j++) {
            coeficientes[j] /= escalas[j];
        }
        return coeficientes;
    }

    private static double[] terminos(double[] fila, int grado, int cantidad) {
        double[] resultado = new double[cantidad];
        resultado[0] = 1;
        for (int j = 1; j < cantidad; j++) {
            resultado[j] = grado > 1 ? Math.pow(fila[0], j) : fila[j - 1];
        }
        return resultado;
    }

    private static double[][] leerTabla(String texto, String nombre) {
        if (texto.isBlank()) {
            throw new IllegalArgumentException(nombre + ": agrega al menos una fila.");
        }

        String[] filas = texto.strip().split("\\R");
        double[][] tabla = new double[filas.length][];
        for (int i = 0; i < filas.length; i++) {
            String[] celdas = filas[i].split(",", -1);
            if (i > 0 && celdas.length != tabla[0].length) {
                throw new IllegalArgumentException(nombre + ": todas las filas deben tener el mismo número de columnas.");
            }
            tabla[i] = new double[celdas.length];
            for (int j = 0; j < celdas.length; j++) {
                try {
                    double valor = Double.parseDouble(celdas[j].strip());
                    if (!Double.isFinite(valor)) {
                        throw new NumberFormatException();
                    }
                    tabla[i][j] = valor;
                } catch (NumberFormatException error) {
                    throw new IllegalArgumentException(nombre + ": número inválido en fila " + (i + 1) +
                        ", columna " + (j + 1) + ".", error);
                }
            }
        }
        return tabla;
    }

    private static String numero(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("Los valores exceden el rango numérico del modelo.");
        }
        BigDecimal redondeado = BigDecimal.valueOf(valor).round(new MathContext(7)).stripTrailingZeros();
        int exponente = redondeado.precision() - redondeado.scale() - 1;
        return exponente >= -4 && exponente <= 7 ? redondeado.toPlainString() : redondeado.toString();
    }
}
