package simulacion;

import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Properties;

final class Acl {
    static final String ONTOLOGIA = "simulacion-hands-on-7";
    static final String PREDICTIVO = "predictivo";
    static final String ESTOCASTICO = "estocastico";

    private Acl() {
    }

    static void ponerDatos(ACLMessage mensaje, Properties datos) {
        StringWriter contenido = new StringWriter();
        try {
            datos.store(contenido, null);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        mensaje.setContent(contenido.toString());
    }

    static Properties leerDatos(ACLMessage mensaje) {
        Properties datos = new Properties();
        try {
            datos.load(new StringReader(mensaje.getContent()));
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        return datos;
    }
}
