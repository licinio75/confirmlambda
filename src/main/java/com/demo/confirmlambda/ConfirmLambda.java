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
        LambdaLogger logger = context.getLogger();  // Get the logger

        // Log to verify that the Lambda was invoked
        logger.log("Lambda was invoked with " + event.getRecords().size() + " records.");

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Log for each message received
                logger.log("Processing message: " + message.getBody());

                // Deserialize the SQS message into PedidoSQSMessageDTO
                PedidoSQSMessageDTO pedido = objectMapper.readValue(message.getBody(), PedidoSQSMessageDTO.class);

                // Log to verify the message was deserialized correctly
                logger.log("Deserialized order: " + pedido.toString());

                // Send a confirmation email
                sendConfirmationEmail(pedido, logger);
            } catch (Exception e) {
                // Log errors
                logger.log("Error processing the message: " + e.getMessage());
            }
        }
        return null;
    }

    private void sendConfirmationEmail(PedidoSQSMessageDTO pedido, LambdaLogger logger) {
        String subject = "Purchase Confirmation - Order " + pedido.getPedidoId();
        String bodyText = generateEmailBody(pedido);

        // Log before sending the email
        logger.log("Sending email to: " + pedido.getUsuarioEmail());

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
            // Log after sending the email
            logger.log("Email successfully sent to " + pedido.getUsuarioEmail());
        } catch (Exception e) {
            // Log if there's an error sending the email
            logger.log("Error sending email to " + pedido.getUsuarioEmail() + ": " + e.getMessage());
        }
    }

    private String generateEmailBody(PedidoSQSMessageDTO pedido) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(pedido.getUsuarioNombre()).append(",\n\n");
        body.append("Thank you for your purchase. Here are the details of your order:\n\n");
        body.append("Order ID: ").append(pedido.getPedidoId()).append("\n");

        body.append("Products:\n");
        for (ItemPedidoDTO item : pedido.getItems()) {
            body.append("- ").append(item.getNombreProducto())
                .append(" (").append(item.getCantidad()).append(" x $")
                .append(item.getPrecioUnitario()).append("): $")
                .append(item.getPrecioTotal()).append("\n");
        }

        body.append("\nTotal purchase amount: $").append(pedido.getPrecioTotal()).append("\n\n");
        body.append("Thank you for shopping with us.\n");
        body.append("Best regards,\nYour Store");

        return body.toString();
    }
}
