package simulacion;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class AgenteDecisor extends Agent {
    record Solicitud(Properties datos, CompletableFuture<String> respuesta) {
    }

    public AgenteDecisor() {
        setEnabledO2ACommunication(true, 0);
    }

    @Override
    protected void setup() {
        CyclicBehaviour coordinacion = new CyclicBehaviour(this) {
            private Solicitud actual;
            private AID especialista;
            private MessageTemplate respuestaEsperada;
            private long limite;

            @Override
            public void action() {
                if (actual == null) {
                    actual = (Solicitud) getO2AObject();
                    if (actual == null) {
                        block();
                    } else {
                        enviar();
                    }
                    return;
                }

                ACLMessage respuesta = receive(respuestaEsperada);
                if (respuesta != null) {
                    recibir(respuesta);
                    return;
                }

                long restante = limite - System.nanoTime();
                if (restante <= 0) {
                    actual.respuesta().completeExceptionally(
                        new IllegalStateException("No hubo respuesta del agente " + especialista.getLocalName() + "."));
                    actual = null;
                } else {
                    block(Math.max(1, TimeUnit.NANOSECONDS.toMillis(restante)));
                }
            }

            private void enviar() {
                Properties datos = actual.datos();
                boolean historico = datos.containsKey("observaciones") && datos.containsKey("entradas");
                boolean riesgoSla = datos.containsKey("media") && datos.containsKey("umbral");
                if (historico == riesgoSla) {
                    actual.respuesta().completeExceptionally(
                        new IllegalArgumentException("La estructura de los datos no identifica un escenario válido."));
                    actual = null;
                    return;
                }

                String destinatario = historico ? Acl.PREDICTIVO : Acl.ESTOCASTICO;
                especialista = new AID(destinatario, AID.ISLOCALNAME);
                String id = UUID.randomUUID().toString();
                ACLMessage peticion = new ACLMessage(ACLMessage.REQUEST);
                peticion.addReceiver(especialista);
                peticion.setOntology(Acl.ONTOLOGIA);
                peticion.setConversationId(id);
                peticion.setReplyWith(id);
                Acl.ponerDatos(peticion, datos);
                respuestaEsperada = MessageTemplate.and(
                    MessageTemplate.MatchSender(especialista),
                    MessageTemplate.and(MessageTemplate.MatchOntology(Acl.ONTOLOGIA),
                        MessageTemplate.and(MessageTemplate.MatchConversationId(id), MessageTemplate.MatchInReplyTo(id))));
                limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                send(peticion);
            }

            private void recibir(ACLMessage respuesta) {
                String ida = "decisor ── REQUEST ──► " + especialista.getLocalName();
                String vuelta = especialista.getLocalName() + " ── " +
                    ACLMessage.getPerformative(respuesta.getPerformative()) + " ──► decisor";
                String texto = ida + "\n" + vuelta + "\n\n" + respuesta.getContent();
                if (respuesta.getPerformative() == ACLMessage.INFORM) {
                    actual.respuesta().complete(texto);
                } else {
                    actual.respuesta().completeExceptionally(new IllegalArgumentException(texto));
                }
                actual = null;
            }
        };
        addBehaviour(coordinacion);
        setO2AManager(coordinacion);
    }
}
