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

public class WorkerThread implements Runnable {
    public void run() {
        Manager.credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        Manager.connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        createFiles();
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
    public void createFiles(){
        for (int i = 0; i <Manager.files.size() ; i++) {
            File file= new File(Manager.files.keySet().toArray()[i].toString());
        }
    }
    private void createOutputMsg(Message message) {
        try {
            ReviewResponse r = new Gson().fromJson(((TextMessage) message).getText(), ReviewResponse.class);
            Manager.files.put(r.getReview().getFileName(), Manager.files.get(r.getReview().getFileName()) - 1);
            BufferedWriter bw = new BufferedWriter(new FileWriter(r.getReview().getFileName(), true));
            bw.write(r.toString());
            bw.newLine();
            bw.flush();
            bw.close();
            if(Manager.files.get(r.getReview().getFileName())==0){
              //  Manager.UpToS3();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
