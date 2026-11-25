# Hands-On 7 · Sistema multiagente de simulación en JADE

Aplicación Java con tres agentes y diálogos de **JOptionPane** para editar datos, ejecutar una simulación y ver su resultado.

## Ejecutar

Requiere JDK 17 o superior, Maven 3.9 y un escritorio gráfico. Abre la terminal dentro de la carpeta `Hands-On-7` y ejecuta:

```sh
mvn clean package
java -jar target/hands-on-7.jar
```

También puedes abrir directamente el JAR ya compilado con `java -jar Hands-On-7.jar`.

## Probar los ejemplos

| Opción | Datos de ejemplo | Resultado |
| --- | --- | --- |
| Datos históricos → Lineal simple | Una variable X y una Y | ŷ = 1 + 2·x, con cinco predicciones |
| Datos históricos → Lineal múltiple | Dos variables X y una Y | ŷ = 8 + 2·x1 + 3·x2, con cinco predicciones |
| Datos históricos → Polinomial | Una variable X y una Y, con grado 2 | ŷ = 2 + 3·x + x², con cinco predicciones |
| Falla SLA | Media de 100 ms, umbral de 250 ms, 100,000 simulaciones y semilla 7 | Probabilidad de que la latencia exceda 250 ms |

Las observaciones llevan el valor Y en la última columna. Las filas a predecir contienen únicamente las X. Todos los ejemplos se pueden editar.

## Funcionamiento

- **Decisor.** Recibe los datos capturados y los envía al especialista según su estructura. Muestra la respuesta y el recorrido del mensaje.
- **Predictivo.** Usa una regresión simple si hay una X, múltiple si hay varias X o polinomial si hay una sola X y se especifica un grado mayor que 1. Ajusta los coeficientes mediante mínimos cuadrados con descomposición QR y devuelve la ecuación y las predicciones.
- **Estocástico.** Usa la transformada inversa de una distribución exponencial. Genera cada latencia como `T = -media × ln(1 - U)`, con `U` uniforme entre 0 y 1, y devuelve el porcentaje de latencias superiores al umbral. Los parámetros precargados representan un escenario de demostración editable.

Los agentes se comunican por nombre local (`AID`) con mensajes `ACLMessage.REQUEST` e `INFORM`. El sistema se ejecuta en un contenedor JADE local sin registrar ni buscar servicios en el Directory Facilitator.

[Documentación de JADE](https://jade.tilab.com/doc/api/jade/lang/acl/ACLMessage.html)
