package simulacion;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Properties;

abstract class AgenteEspecialista extends Agent {
    @Override
    protected void setup() {
        MessageTemplate filtro = MessageTemplate.and(
            MessageTemplate.MatchOntology(Acl.ONTOLOGIA), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage peticion = receive(filtro);
                if (peticion == null) {
                    block();
                    return;
                }

                ACLMessage respuesta = peticion.createReply();
                try {
                    respuesta.setContent(calcular(Acl.leerDatos(peticion)));
                    respuesta.setPerformative(ACLMessage.INFORM);
                } catch (IllegalArgumentException error) {
                    respuesta.setContent(error.getMessage());
                    respuesta.setPerformative(ACLMessage.FAILURE);
                }
                send(respuesta);
            }
        });
    }

    protected abstract String calcular(Properties datos);
}
