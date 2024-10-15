package com.demo.confirmlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.demo.confirmlambda.dto.PedidoSQSMessageDTO;
import com.demo.confirmlambda.dto.ItemPedidoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class ConfirmLambda implements RequestHandler<SQSEvent, Void> {

    private final SesClient sesClient = SesClient.builder().build();
    private final String senderEmail = System.getenv("SENDER_EMAIL"); // Establece el email del remitente en variables de entorno
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Deserializar el mensaje del SQS a PedidoSQSMessageDTO
                PedidoSQSMessageDTO pedido = objectMapper.readValue(message.getBody(), PedidoSQSMessageDTO.class);

                // Enviar un correo electrónico de confirmación
                enviarEmailConfirmacion(pedido);
            } catch (Exception e) {
                context.getLogger().log("Error procesando el mensaje: " + e.getMessage());
            }
        }
        return null;
    }

    private void enviarEmailConfirmacion(PedidoSQSMessageDTO pedido) {
        String subject = "Confirmación de compra - Pedido " + pedido.getPedidoId();
        String bodyText = generarCuerpoEmail(pedido);

        SendEmailRequest request = SendEmailRequest.builder()
            .destination(Destination.builder().toAddresses(pedido.getUsuarioEmail()).build())
            .message(Message.builder()
                .subject(Content.builder().data(subject).build())
                .body(Body.builder()
                    .text(Content.builder().data(bodyText).build())
                    .build())
                .build())
            .source(senderEmail)
            .build();

        sesClient.sendEmail(request);
    }

    private String generarCuerpoEmail(PedidoSQSMessageDTO pedido) {
        StringBuilder body = new StringBuilder();
        body.append("Hola ").append(pedido.getUsuarioNombre()).append(",\n\n");
        body.append("Gracias por tu compra. Aquí están los detalles de tu pedido:\n\n");
        body.append("ID del Pedido: ").append(pedido.getPedidoId()).append("\n");

        body.append("Productos:\n");
        for (ItemPedidoDTO item : pedido.getItems()) {
            body.append("- ").append(item.getNombreProducto())
                .append(" (").append(item.getCantidad()).append(" x $")
                .append(item.getPrecioUnitario()).append("): $")
                .append(item.getPrecioTotal()).append("\n");
        }

        body.append("\nTotal de la compra: $").append(pedido.getPrecioTotal()).append("\n\n");
        body.append("Gracias por comprar con nosotros.\n");
        body.append("Saludos,\nTu Tienda");

        return body.toString();
    }
}
