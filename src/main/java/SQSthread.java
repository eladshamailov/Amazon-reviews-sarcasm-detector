import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Map.Entry;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;



public class SQSthread implements Runnable{

    public static List<Message> messages;

    @Override
    public void run() {
        LocalApp.credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        LocalApp.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(LocalApp.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        // Receive messages
        System.out.println("Receiving messages from AppToManager.\n");
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(LocalApp.AppToManager);
        messages=LocalApp.sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            for (Entry<String, String> entry : message.getAttributes().entrySet()) {
                System.out.println("  Attribute");
                System.out.println("    Name:  " + entry.getKey());
                System.out.println("    Value: " + entry.getValue());
            }
        }
        System.out.println();
    }
}
