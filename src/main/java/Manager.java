
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.appstream.model.Session;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
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
        List<Message> messages = Manager.sqs.receiveMessage(LocalApp.AppToManager).getMessages();
        int i=0;
        while( i < messages.size() ) {
        files.put(messages.get(i).toString(),0);
        i++;
        }
        for (int j = 0; j <files.size() ; j++) {
            //יצירת תרד חדש שמבצע הורדה מה S3 ופארס ושליחה לתור חדש.
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
    public static void reciveMess(){
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");

    }
//    public static void receiveMess(Session session, MessageConsumer consumer){
//        try {
//            while(!isTerminate ) {
//                System.out.println( "Waiting for messages");
//                // Wait 1 minute for a message
//                Message message = consumer.receive(TimeUnit.MINUTES.toMillis(1));
//                if( message == null ) {
//                    System.out.println( "Shutting down after 1 minute of silence" );
//                    break;
//                }
//                ExampleCommon.handleMessage(message);
//                message.acknowledge();
//                System.out.println( "Acknowledged message " + message.getJMSMessageID() );
//            }
//        } catch (JMSException e) {
//            System.err.println( "Error receiving from SQS: " + e.getMessage() );
//            e.printStackTrace();
//        }
//    }
//}
//        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
//
//    }

    public static void downloadS3Files() {

    }

    public static void createQueue() {
        AWSCredentialsProvider credentialsProvider =
                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
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


    public static void parse(File file) throws IOException, ParseException, org.json.simple.parser.ParseException {
        //תקשורת עם התורים שיצרנו לעובדים
        JSONParser parser = new JSONParser();
        JSONObject json;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            json = (JSONObject) parser.parse(line);
            //הכנסת הגיסון כהודעה לתוך התור
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
//    public void addTask(JSONObject obj){
//
//    }
//
//    public static void main (String [] args) throws IOException, ParseException {
//        parse();
//    }
//}
