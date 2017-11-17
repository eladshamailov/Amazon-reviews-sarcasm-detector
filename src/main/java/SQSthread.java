import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;



public class SQSthread implements Runnable{

    public static ConcurrentLinkedQueue<Message> messages=new ConcurrentLinkedQueue<>();
    private boolean run=true;
    public void shutdown(){
        run=false;
    }

    @Override
    public void run() {
        Manager.credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        Manager.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        System.out.println("Listing all queues in your account.\n");
        for (String queueUrl : Manager.sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queueUrl);
            System.out.println();
            // Receive messages
            while (run) {
                System.out.println("Receiving messages from AppToManager.\n");
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                List<Message> tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
                for (Message m : tmp) {
                    add(m);
                }
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
    }
    public static void  add(Message m) {
        messages.add(m);
        synchronized (messages) {
            messages.notifyAll();
        }
    }
}
