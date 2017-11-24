//import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
//import com.amazon.sqs.javamessaging.ProviderConfiguration;
//import com.amazon.sqs.javamessaging.SQSConnection;
//import com.amazon.sqs.javamessaging.SQSConnectionFactory;
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.profile.ProfileCredentialsProvider;
//import com.amazonaws.services.appstream.model.Session;
//import com.amazonaws.services.ec2.AmazonEC2;
//import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.sqs.AmazonSQS;
//import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
//import com.amazonaws.services.sqs.model.*;
//import com.google.gson.Gson;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//
//import javax.jms.JMSException;
//import java.io.*;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//
//import java.text.ParseException;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.atomic.AtomicInteger;
//import javax.jms.*;
//import javax.jms.Message;
//
//public class Manager {
//    public static ConcurrentHashMap<String, Integer> files=new ConcurrentHashMap<>();
//    public static AmazonS3 S3;
//    public static AmazonEC2 ec2;
//    public static SQSConnectionFactory connectionFactory;
//    public static boolean terminate = false;
//    public static AWSCredentialsProvider credentialsProvider;
//    static ExecutorService executorService;
//    public static boolean isTerminate=false;
//    public static UUID uuid;
//    public static AtomicInteger numWorker= new AtomicInteger(0);
//    public static ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
//
//
//    public static void main(String[] args) throws InterruptedException {
//        initialize();
//        createQueue();
//       //create the threadPool
//        executorService= Executors.newFixedThreadPool(100);
//        //create the job to execute
//        Runnable sqsthread = new SQSthread();
//        Thread t1=new Thread(sqsthread);
//        Runnable manager = new ManagerThread();
//        executorService.execute(manager);
//
////
////        while(true) {
////            executor.execute(sqsthread);
////            while (!SQSthread.doWork.get()) {
////                Thread.currentThread().sleep(2000);
////            }
////            System.out.println("return to main thread after sqsthread is sleeping");
////            for (int i = 0; i < SQSthread.messages.size(); i++) {
////
////                while (!ManagerThread.doWork.get()) {
////                    Thread.currentThread().sleep(2000);
////                }
////            }
////            System.out.println("return to main thread after all msg was sent");
////            sqs = AmazonSQSClientBuilder.standard()
////                    .withCredentials(credentialsProvider)
////                    .withRegion("us-west-2")
////                    .build();
////            System.out.println("Sending a new message to ApptoMamager\n");
////            TerminateMsg terminateMsg = new TerminateMsg();
////            Gson gson = new Gson();
////            sqs.sendMessage(new SendMessageRequest(sqs.listQueues().getQueueUrls().get(0), gson.toJson(terminateMsg).toString()));
////            System.out.println("after sending a msg");
////        }
////       //  executor.shutdown();
////        //while (!executor.isTerminated()) {
////        //}
//    }
//
//    //creating a connection to the ec2 and the sqs
//    public static void initialize() {
//        credentialsProvider = new AWSStaticCredentialsProvider
//                (new ProfileCredentialsProvider().getCredentials());
//        System.out.println("===========================================");
//        System.out.println("connect to aws & ec2");
//        System.out.println("===========================================\n");
//        ec2 = AmazonEC2ClientBuilder.standard()
//                .withCredentials(credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
//        System.out.println("===========================================");
//        System.out.println("Getting Started with Amazon SQS");
//        System.out.println("===========================================\n");
//        connectionFactory=new SQSConnectionFactory(
//                new ProviderConfiguration(),
//                AmazonSQSClientBuilder.standard()
//                        .withRegion("us-west-2")
//                        .withCredentials(Manager.credentialsProvider)
//        );
//    }
//
//    //creating the Queues of the Worker-manager
//    public static void createQueue() {
//        try {
//            SQSConnection connection = connectionFactory.createConnection();
//            AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
//            System.out.println("Creating a new SQS queue called ManagerToWorkers.\n");
//            if(!client.queueExists("ManagerToWorkers")) {
//                client.createQueue("ManagerToWorkers");
//            }
//            System.out.println("Creating a new SQS queue called WorkerToManager.\n");
//            if(!client.queueExists("WorkerToManager")) {
//                client.createQueue("WorkerToManager");
//            }
//
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
////    //delete a msg from the sqs Queue
////    public static void deleteMess(){
////
////
////        System.out.println("Deleting a message.\n");
////        Message tmp []=new Message[SQSthread.messages.size()];
////        SQSthread.messages.toArray(tmp);
////        String messageRecieptHandle = (tmp[SQSthread.count.get()-1]).getReceiptHandle();
////        sqs.deleteMessage(new DeleteMessageRequest(SQSthread.URLlist.get(0), messageRecieptHandle));
////
////    }
//
////    public static void parse(File file) throws IOException, ParseException, org.json.simple.parser.ParseException {
////        credentialsProvider =
////                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
////        sqs = AmazonSQSClientBuilder.standard()
////                .withCredentials(credentialsProvider)
////                .withRegion("us-west-2")
////                .build();
////        System.out.println("===========================================");
////        System.out.println("Getting Started with Amazon SQS");
////        System.out.println("===========================================\n");
////        JSONParser parser = new JSONParser();
////        JSONObject json;
////        BufferedReader reader = new BufferedReader(new FileReader(file));
////        String line = reader.readLine();
////        int i=0;
////        int x=files.get(SQSthread.messages.peek().toString());
////        while (line != null) {
////            json = (JSONObject) parser.parse(line);
////            //upload the msg to the MangerToWorker Queue
////            sqs.sendMessage(new SendMessageRequest(MangerToWorker,json.toString()));
////            //
////            files.replace(SQSthread.messages.peek().toString(),x,x++);
////            i++;
////            try {
////                line = reader.readLine();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////            System.out.println(json.toJSONString());
////        }
////        reader.close();
////    }
//
//
//
//
//
//    //מתישהו בדיקת סרקזם
//
//    //    public static boolean Srcasem(ReviewRespons reviewRespons,Review review){
////        if(review.getRating()-reviewRespons.getSentiment()>2)
////            return true;
////        return false;
//}
//}