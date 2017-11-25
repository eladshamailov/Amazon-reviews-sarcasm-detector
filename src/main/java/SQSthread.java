
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import java.util.concurrent.atomic.AtomicInteger;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;


import javax.jms.*;

public class SQSthread implements Runnable {
    public static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void run() {
        System.out.println("\n is run sqs t \n");
        Manager.credentialsProvider = new AWSStaticCredentialsProvider(new EnvironmentVariableCredentialsProvider().getCredentials());

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
                   System.out.println("added message to Q yay!");
               }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}





