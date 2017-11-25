import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;

import javax.jms.*;
import java.io.*;
import java.util.concurrent.*;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Manager {
    public static ConcurrentHashMap<String, Integer> files = new ConcurrentHashMap<>();
    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static SQSConnectionFactory connectionFactory;
    public static boolean terminate = false;
    public static AWSCredentialsProvider credentialsProvider;
    static ExecutorService executorService;
    public static boolean isTerminate = false;
    public static UUID uuid;
    public static AtomicInteger numWorker = new AtomicInteger(0);
    public static ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();


    public static void main(String[] args) throws InterruptedException {
        initialize();
        createQueue();
        //create the threadPool
        executorService = Executors.newFixedThreadPool(100);
        //create the job to execute
        Runnable sqsthread = new SQSthread();
        Thread t1 = new Thread(sqsthread , "SqsThread");
        t1.start();
        System.out.println("\n run t1 \n");
        Runnable WorkerTread = new WorkerThread();
        Thread t2 = new Thread(WorkerTread , "workThread");
        t2.start();
        System.out.println("\n run t2 \n");
        work();
        while(!isTerminate) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            //terminateAllWorkers();
            t1.interrupt();
            t2.interrupt();
            executorService.shutdownNow();
            deleteTheQueue();
            // UpToS3();
    }

    private static void deleteTheQueue() {
        connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        try {
            SQSConnection connection = connectionFactory.createConnection();
            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
            client.getAmazonSQSClient().deleteQueue("ManagerToWorker");
            client.getAmazonSQSClient().deleteQueue("WorkerToManager");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static void work() {
        System.out.println("\n run in work \n");
        //Liad - added Q size > 0
        while (!terminate || !files.isEmpty() || queue.size() > 0) {
            int k = 0;
            if (queue.size() > 100) {
                k = 100;
            } else {
                k = queue.size();
            }
            for (int i = 0; i < k; i++) {
                Runnable manager = new ManagerThread();
                executorService.execute(manager);
            }
            if(k==0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        isTerminate=true;

    }



    //creating a connection to the ec2 and the sqs
    public static void initialize() {
        credentialsProvider = new AWSStaticCredentialsProvider(new EnvironmentVariableCredentialsProvider().getCredentials());

        System.out.println("===========================================");
        System.out.println("connect to aws & ec2");
        System.out.println("===========================================\n");
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        System.out.println("========================");
        System.out.println("connect to S3");
        S3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
    }

    //creating the Queues of the Worker-manager
    public static void createQueue() {
        try {
            SQSConnection connection = connectionFactory.createConnection();
            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
            System.out.println("Creating a new SQS queue called ManagerToWorker.\n");
            if (!client.queueExists("ManagerToWorker")) {
                client.createQueue("ManagerToWorker");
            }
            System.out.println("Creating a new SQS queue called WorkerToManager.\n");
            if (!client.queueExists("WorkerToManager")) {
                client.createQueue("WorkerToManager");
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void UpToS3(String fileName) {
        try {
            Connection connection = null;
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            System.out.println("Uploading a new object to S3 from a file");
            String[] spilted = fileName.split("\\s");
            File file = new File(spilted[1]+"1");
            PutObjectRequest req = new PutObjectRequest(spilted[0], spilted[1], fileName);
            S3.putObject(req);
            Gson gson=new Gson();
            OutputMsg outputMsg = new OutputMsg((S3.getUrl(spilted[0], spilted[1])).toString(), spilted[1], UUID.fromString(spilted[3]));
            MessageProducer producer = session.createProducer(session.createQueue("ManagerToApp"+spilted[3]));
            producer.send(session.createTextMessage(gson.toJson(outputMsg)));
            files.remove(spilted[1]);
        } catch (AmazonServiceException ace) {
            System.out.println("Uploading");
            System.out.println("Caught an AmazonClientException," +
                    " which means the client encountered "
                    + "a serious internal problem while trying to communicate with " + "S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


}