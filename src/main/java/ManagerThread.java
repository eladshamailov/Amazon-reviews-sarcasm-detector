import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;

import javax.jms.*;
import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagerThread implements Runnable {
    S3Object obj;
    public static AtomicBoolean doWork = new AtomicBoolean(false);
    List<Bucket> bucketList;

    @Override
    public void run() {
//        Manager.credentialsProvider =
//                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
//        Manager.S3 = AmazonS3ClientBuilder.standard()
//                .withCredentials(Manager.credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
        try {
            Gson gson = new Gson();
            Message queueMessage = Manager.queue.poll();
            String text = (((TextMessage) queueMessage).getText());
            Msg msg = gson.fromJson(text, Msg.class);
            if (Manager.terminate == true) {
                if (Manager.uuid == msg.getUuid()) {
                    handleMassage(msg, text);
                    queueMessage.acknowledge();
                } else {
                    queueMessage.acknowledge();
                }
            } else {
                handleMassage(msg, text);
                queueMessage.acknowledge();
            }
        } catch (JMSException e1) {
            e1.printStackTrace();
        }
    }

    private void handleMassage(Msg msg, String text) {
        Gson gson = new Gson();
        switch (msg.getAction()) {
            //terminate
            case 0:
                Terminate(msg.getUuid());
                break;
            // URL
            case 1:
                UrlMsg urlMsg = gson.fromJson(text, UrlMsg.class);
                Manager.files.put(urlMsg.getURL(), 0);
                parseFromS3(urlMsg);
                break;
            case 4:
                WorkersNumMsg workerMsg = gson.fromJson(text, WorkersNumMsg.class);
                if (workerMsg.getNum() > Manager.numWorker.get()) {
                    Manager.numWorker.set(workerMsg.getNum());
                }
                break;
        }
    }

    private void parseFromS3(UrlMsg urlMsg) {
        File file = new File("localFile");
        obj = Manager.S3.getObject(urlMsg.getBucketName(), urlMsg.getKey());
        InputStream reader = new BufferedInputStream(
                obj.getObjectContent());
        OutputStream writer = null;
        try {
            writer = new BufferedOutputStream(new FileOutputStream(file));
            int read = -1;
            while ((read = reader.read()) != -1) {
                writer.write(read);
            }
            writer.flush();
            writer.close();
            reader.close();
            parse(file, urlMsg.getURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Terminate(UUID uuid) {
        Manager.uuid = uuid;
        Manager.terminate = true;
    }

    public static void parse(File file, String url) {
        Gson gson = new Gson();
        BufferedReader reader = null;
        Manager.connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        Connection connection = null;
        try {
            connection = Manager.connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue("ManagerToWorker"));
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                Review review = gson.fromJson(line, Review.class);
                TextMessage message = session.createTextMessage(gson.toJson(review).toString());
                producer.send(message);
                Manager.files.put(url, Manager.files.get(url).intValue() + 1);
                createWorkers();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JMSException e1) {
            e1.printStackTrace();
        }
    }

    private static void createWorkers() {
        Manager.connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        Connection connection = null;
        try {
            connection = Manager.connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue("ManagerToWorker"));
            consumer.receive().
    } catch (JMSException e) {
            e.printStackTrace();
        }
    }