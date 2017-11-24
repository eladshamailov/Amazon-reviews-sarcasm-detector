import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;


import javax.jms.*;

public class SQSthread implements Runnable {
    public static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void run() {
        Manager.credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        Manager.connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        try {

            SQSConnection connection = Manager.connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue("AppToManager"));
            connection.start();
            Message message;
               while (true) {
                   while ((message = consumer.receive()) == null) {
                       System.out.println("There is no new msg");
                       Thread.sleep(1000);
                       if (Thread.interrupted()) {
                           return;
                       }
                   }
                   Manager.queue.add(message);
               }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


//        System.out.println("Listing all queues in your account.\n");
//        URLlist = Manager.sqs.listQueues().getQueueUrls();
//        for (String queueUrl : URLlist) {
//            System.out.println("  QueueUrl: " + queueUrl);
//            System.out.println();
//            // Receive messages
//            while (run) {
//                Message receivedMessage = consumer.receive(1000);
//
//                // Cast the received message as TextMessage and display the text
//                if (receivedMessage != null) {
//                    System.out.println("Received: " + ((TextMessage) receivedMessage).getText());
//                }
//                System.out.println("Receiving messages from AppToManager.\n");
//                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
//                List<Message> tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
//                while (tmp ==null){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    tmp= Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
//                }
//               while (tmp.size() > 0) {
//                    int k = 0;
//                    for (Message m : tmp) {
//                        Manager.add(m);
//                        tmp.remove(k);
//                        k++;
//                        if (tmp.size() == 0) {
//                            break;
//                        }
//                    }
//                    tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
//                }
//                for (Message message : messages) {
//                    System.out.println("  Message");
//                    System.out.println("    MessageId:     " + message.getMessageId());
//                    System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//                    System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//                    System.out.println("    Body:          " + message.getBody());
//                    for (Entry<String, String> entry : message.getAttributes().entrySet()) {
//                        System.out.println("  Attribute");
//                        System.out.println("    Name:  " + entry.getKey());
//                        System.out.println("    Value: " + entry.getValue());
//                    }
//                }
//                System.out.println();
//                if (messages.size() > 0) {
//                    System.out.println("end function");
//                    doWork.set(true);
//                }
//                if (tmp.size() == 0) {
//                    try {
//                        synchronized (messages) {
//                            messages.wait();
//                        }
//                    } catch (InterruptedException e) {
//                            e.printStackTrace();
//                    }
//                }
//            }



