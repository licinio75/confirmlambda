package com.demo.confirmlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.demo.confirmlambda.dto.PedidoSQSMessageDTO;
import com.demo.confirmlambda.dto.ItemPedidoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class ConfirmLambda implements RequestHandler<SQSEvent, Void> {

    private final SesClient sesClient = SesClient.builder().build();
    private final String senderEmail = System.getenv("SENDER_EMAIL");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();  // Obtener el logger

        // Log para verificar que la Lambda fue invocada
        logger.log("Lambda fue invocada con " + event.getRecords().size() + " registros.");

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Log para cada mensaje recibido
                logger.log("Procesando mensaje: " + message.getBody());

                // Deserializar el mensaje del SQS a PedidoSQSMessageDTO
                PedidoSQSMessageDTO pedido = objectMapper.readValue(message.getBody(), PedidoSQSMessageDTO.class);

                // Log para verificar que el mensaje fue deserializado correctamente
                logger.log("Pedido deserializado: " + pedido.toString());

                // Enviar un correo electrónico de confirmación
                enviarEmailConfirmacion(pedido, logger);
            } catch (Exception e) {
                // Log de errores
                logger.log("Error procesando el mensaje: " + e.getMessage());
            }
        }
        return null;
    }

    private void enviarEmailConfirmacion(PedidoSQSMessageDTO pedido, LambdaLogger logger) {
        String subject = "Confirmación de compra - Pedido " + pedido.getPedidoId();
        String bodyText = generarCuerpoEmail(pedido);

        // Log antes de enviar el correo
        logger.log("Enviando correo a: " + pedido.getUsuarioEmail());

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

        try {
            sesClient.sendEmail(request);
            // Log después de enviar el correo
            logger.log("Correo enviado exitosamente a " + pedido.getUsuarioEmail());
        } catch (Exception e) {
            // Log si ocurre un error al enviar el correo
            logger.log("Error enviando correo a " + pedido.getUsuarioEmail() + ": " + e.getMessage());
        }
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
