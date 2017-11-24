import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;

import java.util.*;

public class Workers {

    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonSQS sqs;
    public static AWSCredentialsProvider credentialsProvider;
    public static List<String> URLlist;
    public static String MangerToWorker;
    public static String WorkerToManager;
    public static boolean terminate = false;
    public static SentimentAnalysis sentimentAnalysis;
    public static NamedEntityRecognition namedEntityRecognition;

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
        sentimentAnalysis=new SentimentAnalysis();
        namedEntityRecognition=new NamedEntityRecognition();
    }

    public static void getMsg() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        System.out.println("Listing all queues in your account.\n");
        for (String queue : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queue);
        }
        MangerToWorker = sqs.listQueues("MangerToWorker").getQueueUrls().get(0);    //changed from get(2)
        WorkerToManager = sqs.listQueues("WorkerToManager").getQueueUrls().get(0);  // changed from get(3)
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

            workOnMsg(message);
            deleteMess(message);
            }

        }
    }
    public static void workOnMsg(Message message){
        ReviewMsg reviewMsg = new Gson().fromJson(message.getBody(), ReviewMsg.class);
        for(Review review: reviewMsg.getReviews()){
            int reviewGrade= sentimentAnalysis.findSentiment(review.getText());
            ArrayList<String> a=namedEntityRecognition.printEntities(review.getText());
            HashMap<Review,List<String>>  reviewMap=new HashMap<>();
            reviewMap.put(review,a);
            ReviewResponse reviewResponse=new ReviewResponse(reviewMap,reviewGrade);
            OutputMsg outputMsg=new OutputMsg(reviewResponse,reviewGrade);
            sendMessage(outputMsg);
            //deleteMess(message);
        }

    }
    public static void sendMessage(OutputMsg outputMsg) {
        credentialsProvider=new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
            System.out.println("Sending a message to WorkerToManager\n");
            Gson gson=new Gson();
            sqs.sendMessage(new SendMessageRequest(WorkerToManager, gson.toJson(outputMsg).toString()));
    }

    public static void deleteMess(Message message){
//        sqs = AmazonSQSClientBuilder.standard()
//                .withCredentials(Manager.credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
        System.out.println("Deleting a message.\n");
        sqs.deleteMessage(new DeleteMessageRequest(MangerToWorker, message.getReceiptHandle()));
    }

}


