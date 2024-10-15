package com.demo.confirmlambda;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.Collections;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class App {
    public static void main(String[] args) {
        // Crear un cliente SQS con la región configurada
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.EU_NORTH_1) // Ajustar a tu región
                .build();

        String queueUrl = "https://sqs.eu-north-1.amazonaws.com/241825613750/shopQueue";

        // Crear una solicitud para recibir mensajes
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)  // Recibe un mensaje a la vez para simular el trigger
                .waitTimeSeconds(10)     // Polling largo
                .build();

        // Recibir mensajes
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);
        List<Message> messages = receiveMessageResponse.messages();

        for (Message message : messages) {
            // Simula la ejecución de tu función Lambda local
            processMessage(message.body());

            // Borrar el mensaje después de procesarlo
            /* 
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
            */
        }

        sqsClient.close();
    }

    private static void processMessage(String messageBody) {
        // Lógica para procesar el mensaje o invocar la Lambda local
        System.out.println("Procesando mensaje: " + messageBody);

        // Crear un evento SQS simulado con el mensaje recibido
        SQSEvent sqsEvent = new SQSEvent();
        SQSMessage sqsMessage = new SQSMessage();
        sqsMessage.setBody(messageBody);
        sqsEvent.setRecords(Collections.singletonList(sqsMessage));  // Solo un mensaje en la lista

        // Instanciar y ejecutar la función Lambda
        ConfirmLambda confirmLambda = new ConfirmLambda();
        confirmLambda.handleRequest(sqsEvent, null);
    }
}
