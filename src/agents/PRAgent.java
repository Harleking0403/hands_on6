package agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import regression.PR;
import java.util.Locale;

public class PRAgent extends Agent {
    @Override
    protected void setup() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    double[] x = {108, 115, 106, 97, 95, 91, 97, 83, 83, 78, 54, 67, 56, 53, 61, 115, 81, 78, 30, 45, 99, 32, 25, 28, 90, 89};
                    double[] y = {95, 96, 95, 97, 93, 94, 95, 93, 92, 86, 73, 80, 65, 69, 77, 96, 87, 89, 60, 63, 95, 61, 55, 56, 94, 93};
                    PR modelo = new PR(x, y);
                    System.out.println("[PR] Registros: " + x.length);
                    System.out.println("[PR] " + modelo.obtenerEcuacion());
                    double[] tamaños = {35, 65, 100};
                    for (double tamaño : tamaños) {
                        System.out.printf(Locale.US,
                            "[PR] BatchSize=%.0f -> MachineEfficiency=%.4f%n",
                            tamaño, modelo.predecir(tamaño));
                    }
                    System.out.printf(Locale.US, "[PR] R2=%.6f%n", modelo.calcularR2());
                } catch (IllegalArgumentException e) {
                    System.err.println("[PR] Error: " + e.getMessage());
                } finally {
                    doDelete();
                }
            }
        });
    }
}
