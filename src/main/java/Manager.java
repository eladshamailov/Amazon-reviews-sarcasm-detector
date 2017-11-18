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
    public static ConcurrentHashMap<String, Integer> files=new ConcurrentHashMap<>();
    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonSQS sqs;
    public static boolean terminate = false;
    public static String MangerToWorker;
    public static String WorkerToManager;
    public static AWSCredentialsProvider credentialsProvider;
    static ExecutorService executorService;
    public static boolean isTerminate=false;

    public static void main(String[] args) throws InterruptedException {
        initialize();
        createQueue();
        //create the threadPool
        ExecutorService executor = Executors.newFixedThreadPool(100);
        //create the job to execute
        Runnable sqsthread = new SQSthread();
        System.out.println("the Thread:"+Thread.currentThread().getName());
        executor.execute(sqsthread);
        System.out.println("the Thread:"+Thread.currentThread().getName());
        while (!SQSthread.doWork.get()){
            Thread.currentThread().sleep(10000);
            System.out.println("waiting");
        }
        System.out.println("the thread: "+Thread.currentThread().getName());
        for (int i = 0; i < SQSthread.messages.size(); i++) {
            Runnable manager=new ManagerThread();
            executor.execute(manager);
            while (!ManagerThread.doWork.get()){
                Thread.currentThread().sleep(1000);
                System.out.println("waiting");
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    //creating a connection to the ec2 snd the sqs

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

    //creating the Queues of the Worker-manager
    public static void createQueue() {
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

    //adding a message to the local Queue+ call deleteMess function
    public static void  add(Message m) {
        SQSthread.messages.add(m);
        SQSthread.count.getAndIncrement();
        files.put(m.getBody().toString(),0);
        System.out.println("counter is: "+SQSthread.count);
        synchronized (SQSthread.messages) {
            SQSthread.messages.notifyAll();
        }
        deleteMess();
    }

    //delete a msg from the sqs Queue
    public static void deleteMess(){
//        sqs = AmazonSQSClientBuilder.standard()
//                .withCredentials(Manager.credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
        System.out.println("Deleting a message.\n");
        Message tmp []=new Message[SQSthread.messages.size()];
        SQSthread.messages.toArray(tmp);
        String messageRecieptHandle = (tmp[SQSthread.count.get()-1]).getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(SQSthread.URLlist.get(0), messageRecieptHandle));

    }

    public static void parse(File file) throws IOException, ParseException, org.json.simple.parser.ParseException {
        credentialsProvider =
                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        JSONParser parser = new JSONParser();
        JSONObject json;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        int i=0;
        while (line != null) {
            json = (JSONObject) parser.parse(line);
            //upload the msg to the MangerToWorker Queue
            sqs.sendMessage(new SendMessageRequest(MangerToWorker,json.toString()));
            //יש ליצור עובד חדש
            int x=files.get(SQSthread.messages.peek());
            files.replace(SQSthread.messages.poll().toString(),x,x++);
            i++;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(json.toJSONString());
        }
        reader.close();
    }
}