package simulacion;

import jade.wrapper.AgentController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

final class Interfaz {
    private static final String TITULO = "Hands-On 7 · Simulación multiagente";

    private Interfaz() {
    }

    static void ejecutar(AgentController decisor) throws Exception {
        while (true) {
            FutureTask<Properties> captura = new FutureTask<>(Interfaz::capturar);
            SwingUtilities.invokeAndWait(captura);
            Properties datos = captura.get();
            if (datos == null) {
                return;
            }

            CompletableFuture<String> respuesta = new CompletableFuture<>();
            decisor.putO2AObject(new AgenteDecisor.Solicitud(datos, respuesta), false);

            try {
                String resultado = respuesta.get(12, TimeUnit.SECONDS);
                SwingUtilities.invokeAndWait(() -> mostrarResultado(resultado));
            } catch (ExecutionException error) {
                mostrarError(error.getCause().getMessage());
            }
        }
    }

    private static Properties capturar() {
        String[] opciones = {"Datos históricos", "Falla SLA", "Salir"};
        while (true) {
            int seleccion = JOptionPane.showOptionDialog(null, "¿Qué datos quieres analizar?", TITULO,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (seleccion != 0 && seleccion != 1) {
                return null;
            }

            Properties datos = seleccion == 0 ? capturarHistorico() : capturarSla();
            if (datos != null) {
                return datos;
            }
        }
    }

    private static Properties capturarHistorico() {
        String[] ejemplos = {"Lineal simple", "Lineal múltiple", "Polinomial"};
        JComboBox<String> selector = new JComboBox<>(ejemplos);
        JTextArea observaciones = area(6, 36);
        JTextArea entradas = area(5, 36);
        JTextField grado = new JTextField(8);

        Runnable cargarEjemplo = () -> {
            switch (selector.getSelectedIndex()) {
                case 0 -> {
                    observaciones.setText("1,3\n2,5\n3,7\n4,9\n5,11");
                    entradas.setText("6\n7\n8\n9\n10");
                    grado.setText("");
                }
                case 1 -> {
                    observaciones.setText("1,1,13\n2,1,15\n1,2,16\n3,2,20\n2,4,24\n4,3,25");
                    entradas.setText("2,3\n3,4\n4,5\n5,6\n6,7");
                    grado.setText("");
                }
                case 2 -> {
                    observaciones.setText("0,2\n1,6\n2,12\n3,20\n4,30\n5,42");
                    entradas.setText("6\n7\n8\n9\n10");
                    grado.setText("2");
                }
                default -> throw new IllegalStateException("Ejemplo desconocido.");
            }
        };

        selector.addActionListener(evento -> cargarEjemplo.run());
        cargarEjemplo.run();

        JPanel formulario = panel();
        agregar(formulario, "Ejemplo editable", selector);
        agregar(formulario, "Observaciones (X1, X2, …, Y)", new JScrollPane(observaciones));
        agregar(formulario, "Entradas a predecir (mínimo 5 filas, solo las X)", new JScrollPane(entradas));
        agregar(formulario, "Grado polinomial (2 a 5, opcional)", grado);
        formulario.add(new JLabel("Una fila por línea. Separa columnas con coma y usa punto decimal."));

        if (!aceptar(formulario, "Datos históricos")) {
            return null;
        }
        Properties datos = new Properties();
        datos.setProperty("observaciones", observaciones.getText());
        datos.setProperty("entradas", entradas.getText());
        datos.setProperty("grado", grado.getText());
        return datos;
    }

    private static Properties capturarSla() {
        JTextField media = new JTextField("100", 16);
        JTextField umbral = new JTextField("250", 16);
        JTextField iteraciones = new JTextField("100000", 16);
        JTextField semilla = new JTextField("7", 16);

        JPanel formulario = panel();
        formulario.add(new JLabel("Simulación de latencia con distribución exponencial."));
        formulario.add(Box.createVerticalStrut(12));
        agregar(formulario, "Latencia media (ms)", media);
        agregar(formulario, "Umbral del SLA (ms)", umbral);
        agregar(formulario, "Número de simulaciones", iteraciones);
        agregar(formulario, "Semilla aleatoria", semilla);

        if (!aceptar(formulario, "Escenario de falla SLA")) {
            return null;
        }
        Properties datos = new Properties();
        datos.setProperty("media", media.getText().strip());
        datos.setProperty("umbral", umbral.getText().strip());
        datos.setProperty("iteraciones", iteraciones.getText().strip());
        datos.setProperty("semilla", semilla.getText().strip());
        return datos;
    }

    private static boolean aceptar(JPanel formulario, String titulo) {
        String[] opciones = {"Simular", "Volver"};
        return JOptionPane.showOptionDialog(null, formulario, titulo, JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]) == 0;
    }

    private static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return panel;
    }

    private static void agregar(JPanel panel, String titulo, Component campo) {
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setLabelFor(campo);
        panel.add(etiqueta);
        if (campo instanceof javax.swing.JComponent componente) {
            componente.setAlignmentX(Component.LEFT_ALIGNMENT);
            componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
        }
        panel.add(campo);
        panel.add(Box.createVerticalStrut(10));
    }

    private static JTextArea area(int filas, int columnas) {
        JTextArea area = new JTextArea(filas, columnas);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return area;
    }

    private static void mostrarResultado(String resultado) {
        JTextArea salida = area(Math.min(17, 2 + (int) resultado.lines().count()), 64);
        salida.setText(resultado);
        salida.setEditable(false);
        salida.setLineWrap(true);
        salida.setWrapStyleWord(true);
        salida.setCaretPosition(0);
        JOptionPane.showMessageDialog(null, new JScrollPane(salida), "Resultado de la simulación",
            JOptionPane.PLAIN_MESSAGE);
    }

    static void mostrarError(String texto) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextArea salida = area(5, 54);
            salida.setText(texto == null ? "Ocurrió un error al iniciar la simulación." : texto);
            salida.setEditable(false);
            salida.setLineWrap(true);
            salida.setWrapStyleWord(true);
            salida.setCaretPosition(0);
            JOptionPane.showMessageDialog(null, new JScrollPane(salida), "No se pudo simular",
                JOptionPane.ERROR_MESSAGE);
        });
    }
}
