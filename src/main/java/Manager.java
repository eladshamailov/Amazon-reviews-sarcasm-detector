import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.appstream.model.Session;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.jms.JMSException;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.*;
import javax.jms.Message;

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
        Thread t1 = new Thread(sqsthread);
        t1.run();
        Runnable manager = new ManagerThread();
        executorService.execute(manager);
        Runnable WorkerTread=new WorkerThread();
        Thread t2= new Thread(WorkerTread);
        t2.run();
 //       UpToS3();
//        while(true) {
//            executor.execute(sqsthread);
//            while (!SQSthread.doWork.get()) {
//                Thread.currentThread().sleep(2000);
//            }

    }

    //creating a connection to the ec2 and the sqs
    public static void initialize() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
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
    }

    //creating the Queues of the Worker-manager
    public static void createQueue() {
        try {
            SQSConnection connection = connectionFactory.createConnection();
            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
            System.out.println("Creating a new SQS queue called ManagerToWorkers.\n");
            if (!client.queueExists("ManagerToWorkers")) {
                client.createQueue("ManagerToWorkers");
            }
            System.out.println("Creating a new SQS queue called WorkerToManager.\n");
            if (!client.queueExists("WorkerToManager")) {
                client.createQueue("WorkerToManager");
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

//    public static void UpToS3(String directoryName) {
//        try {
//            System.out.println("Uploading a new object to S3 from a file");
//            int i = 0;
//            File dir = new File(directoryName);
//            PutObjectRequest req = new PutObjectRequest();
//            S3.putObject(req);
//            i++;
//        } catch (AmazonServiceException ace) {
//            System.out.println("Uploading");
//            System.out.println("Caught an AmazonClientException," +
//                    " which means the client encountered "
//                    + "a serious internal problem while trying to communicate with " + "S3, "
//                    + "such as not being able to access the network.");
//            System.out.println("Error Message: " + ace.getMessage());
//        }
//    }

}