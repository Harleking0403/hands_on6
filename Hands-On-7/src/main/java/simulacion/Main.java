package simulacion;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Esta aplicación requiere un escritorio para abrir JOptionPane.");
            System.exit(1);
        }

        Runtime runtime = Runtime.instance();
        AgentContainer contenedor = null;
        int estado = 0;

        try {
            Profile perfil = new ProfileImpl();
            perfil.setParameter(Profile.MAIN_HOST, "127.0.0.1");
            perfil.setParameter(Profile.MAIN_PORT, puertoLocal());
            perfil.setParameter(Profile.NO_MTP, "true");
            contenedor = runtime.createMainContainer(perfil);
            if (contenedor == null) {
                throw new IllegalStateException("No se pudo iniciar la plataforma JADE.");
            }

            contenedor.createNewAgent(Acl.PREDICTIVO, AgentePredictivo.class.getName(), null).start();
            contenedor.createNewAgent(Acl.ESTOCASTICO, AgenteEstocastico.class.getName(), null).start();
            AgentController decisor = contenedor.acceptNewAgent("decisor", new AgenteDecisor());
            decisor.start();
            Interfaz.ejecutar(decisor);
        } catch (Exception error) {
            System.err.println(error);
            Interfaz.mostrarError(error.getMessage());
            estado = 1;
        } finally {
            if (contenedor != null) {
                try {
                    contenedor.kill();
                } catch (ControllerException error) {
                    System.err.println(error);
                    estado = 1;
                }
            }
            runtime.shutDown();
        }
        System.exit(estado);
    }

    private static String puertoLocal() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return Integer.toString(socket.getLocalPort());
        }
    }
}
