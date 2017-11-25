import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class WorkerThread implements Runnable {
    public void run() {
        System.out.println("in worker thread");
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
            MessageConsumer consumer = session.createConsumer(session.createQueue("WorkerToManager"));
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
                createOutputMsg(message);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createOutputMsg(Message message) {
        try {

            ReviewResponse r = new Gson().fromJson(((TextMessage) message).getText(), ReviewResponse.class);
            String fileName=r.getReview().getFileName();
            String[] spilted = fileName.split("\\s");
            if((Manager.files.get(spilted[1])) != null){
                synchronized (Manager.files) {
                    Manager.files.put((spilted[1]), (Manager.files.get(spilted[1])) - 1);
                }
                FileWriter fw = new FileWriter(spilted[1]+"1", true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(r.toString());
                bw.newLine();
                bw.flush();
                bw.close();
                System.out.println("the counter: "+Manager.files.get(spilted[1]));
                if (Manager.files.get(spilted[1]) == 0) {
                    Manager.UpToS3(fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
