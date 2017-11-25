import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.google.gson.Gson;


import javax.jms.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagerThread implements Runnable {
    private static int numActiveWorker = 0;
    S3Object obj = new S3Object();
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
            if (queueMessage != null) {
                System.out.println("got message from C Q " + Thread.currentThread().getName());
                String text = (((TextMessage) queueMessage).getText());
                System.out.println("message text: " + Thread.currentThread().getName() + " " + text);
                Msg msg = gson.fromJson(text, Msg.class);
                if (Manager.terminate == true) {
                    System.out.println("in terminate true");
                    System.out.println("Manager UUID: " + Manager.uuid);
                    System.out.println("msg UUID:" + msg.getUuid());
                    System.out.println("\n does the uuid's == ? " + (Manager.uuid == msg.getUuid()));
                    System.out.println("\n does the uuid's equals ? " + (Manager.uuid.equals(msg.getUuid())));
                    if (Manager.uuid.equals(msg.getUuid())) {
                        handleMassage(msg, text);
                    }
                } else {
                    System.out.println("in terminate false");
                    handleMassage(msg, text);
                }
            }
        } catch (
                JMSException e1)

        {
            e1.printStackTrace();
        }

    }

    private void handleMassage(Msg msg, String text) {
        Gson gson = new Gson();
        System.out.println(" in handle msg, msg action: " + msg.getAction());
        switch (msg.getAction()) {
            //terminate
            case 0:
                Terminate(msg.getUuid());
                break;
            // URL
            case 1:
                System.out.println("in case 1 with msg: " + msg.getUuid());
                UrlMsg urlMsg = gson.fromJson(text, UrlMsg.class);
                Manager.files.put(urlMsg.toString(), 0);
                System.out.println("files size = " + Manager.files.size());
                parseFromS3(urlMsg);
                break;
            //WorkerMsg
            case 4:
                WorkersNumMsg workerMsg = gson.fromJson(text, WorkersNumMsg.class);
                if (workerMsg.getNum() > Manager.numWorker.get()) {
                    Manager.numWorker.set(workerMsg.getNum());
                }
                break;
        }
    }

    private void parseFromS3(UrlMsg urlMsg) {
        System.out.println("in parse from s3 with msg: " + urlMsg.uuid + "Thread :" + Thread.currentThread().getName());
        File file = new File("MNGR"+urlMsg.getKey());
        String bucketName1 = urlMsg.bucketName;
        String key1 = urlMsg.key;
        obj = Manager.S3.getObject(bucketName1, key1);
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
            parse(file, urlMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Terminate(UUID uuid) {
        Manager.uuid = uuid;
        Manager.terminate = true;
    }

    public static void parse(File file, UrlMsg url) {
        System.out.println("in parse with msg:" + url.uuid);
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
//            System.out.println(line +"\n");
            while (line != null) {
                ReviewMsg reviewMsg = gson.fromJson(line, ReviewMsg.class);
                reviewMsg.setFileName(url.toString());
                reviewMsg.setAction(2);
                TextMessage message = session.createTextMessage(gson.toJson(reviewMsg));
                producer.send(message);
                Manager.files.put(url.toString(), Manager.files.get(url.toString()).intValue() + 1);
                createWorkers();
                line = reader.readLine();
//                System.out.println(line +"\n");
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
        SQSConnection connection = null;
        try {
            connection = Manager.connectionFactory.createConnection();
            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
            GetQueueAttributesRequest attReq = new GetQueueAttributesRequest();
            attReq.setQueueUrl("ManagerToWorker");
            ArrayList<String> attr = new ArrayList<>();
            attr.add("ApproximateNumberOfMessages");
            attReq.setAttributeNames(attr);
            GetQueueAttributesResult response = client.getAmazonSQSClient().getQueueAttributes(attReq);
            String messagesNum = response.getAttributes().get("ApproximateNumberOfMessages");
            if (numActiveWorker == 0 || (Integer.parseInt(messagesNum) - numActiveWorker) >= Manager.numWorker.get()) {
                int x = (Integer.parseInt(messagesNum) - numActiveWorker) - Manager.numWorker.get();
                for (int i = 0; i < x; i++) {
                    //Workers worker=new Workers();
                }
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}