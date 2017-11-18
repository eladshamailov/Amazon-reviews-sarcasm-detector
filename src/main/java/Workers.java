import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Workers {

    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonSQS sqs;
    public static AWSCredentialsProvider credentialsProvider;
    public static List<String> URLlist;
    public static String MangerToWorker;
    public static String WorkerToManager;
    public static boolean terminate = false;

    public static void main(String [] args){
        initialize();
        getMsg();
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

    public static void getMsg() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        System.out.println("Listing all queues in your account.\n");
        MangerToWorker = sqs.listQueues("MangerToWorker").getQueueUrls().get(0);
        WorkerToManager = sqs.listQueues("WorkerToManager").getQueueUrls().get(0);
        while (!terminate) {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(MangerToWorker);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();
            for (final Message message : messages) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        sqs.changeMessageVisibility(MangerToWorker, message.getReceiptHandle(), 30000);
                    }
                }, 25000);

            }

        }
    }
    public static void workOnMsg(Message message){
        ReviewMsg reviewMsg = new Gson().fromJson(message.getBody(), ReviewMsg.class);


    }
}
