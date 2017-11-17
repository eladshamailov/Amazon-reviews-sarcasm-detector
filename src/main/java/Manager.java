import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.appstream.model.Session;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Manager {
    public static ConcurrentHashMap<String, Integer> files;
    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonSQS sqs;
    public static boolean terminate = false;
    public static String MangerToWorker;
    public static String WorkerToManager;
    public static AWSCredentialsProvider credentialsProvider;
    static ExecutorService executorService;
    public static boolean isTerminate=false;

    public static void main(String[] args)  {
        initialize();
        //create the threadPool
        ExecutorService executor = Executors.newFixedThreadPool(100);
        //create the job to execute
            Runnable sqsthread = new SQSthread();
            executor.execute(sqsthread);
            deleteMess();

//        for (int i = 0; i < SQSthread.messages.size(); i++) {
//            Runnable manager=new ManagerThread();
//            executor.execute(manager);
//        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public static void initialize() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("connect to aws & ec2");
        System.out.println("===========================================\n");
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");

    }

    public static void deleteMess(){
        System.out.println("Deleting a message.\n");
        String messageRecieptHandle = SQSthread.messages.poll().getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(LocalApp.AppToManager, messageRecieptHandle));
    }

    public static void createQueue() {
        credentialsProvider =
                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");

        System.out.println("Creating a new SQS queue called MangerToApp.\n");
        CreateQueueRequest createQueueRequest1 = new CreateQueueRequest
                ("MangerToWorker" + UUID.randomUUID());
        MangerToWorker = sqs.createQueue(createQueueRequest1).getQueueUrl();
        System.out.println("Creating a new SQS queue called AppToManager.\n");
        CreateQueueRequest createQueueRequest2 = new CreateQueueRequest
                ("WorkerToManager" + UUID.randomUUID());
        WorkerToManager = sqs.createQueue(createQueueRequest2).getQueueUrl();
        //list queue
        System.out.println("Listing all queues in your account.\n");
        for (String queue : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queue);
        }
        System.out.println();
    }


//    public static void parse(File file) throws IOException, ParseException, org.json.simple.parser.ParseException {
//        credentialsProvider =
//                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
//        sqs = AmazonSQSClientBuilder.standard()
//                .withCredentials(credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
//        System.out.println("===========================================");
//        System.out.println("Getting Started with Amazon SQS");
//        System.out.println("===========================================\n");
//        JSONParser parser = new JSONParser();
//        JSONObject json;
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        String line = reader.readLine();
//        int i=0;
//        while (line != null) {
//            json = (JSONObject) parser.parse(line);
//            sqs.sendMessage(new SendMessageRequest(MangerToWorker,json.toString()));
//            //יש ליצור עובד חדש
//            int x=files.get(SQSthread.messages.get(i));
//            files.replace(SQSthread.messages.get(i).toString(),x,x++);
//            i++;
//            try {
//                line = reader.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(json.toJSONString());
//        }
//        reader.close();
//    }
}
