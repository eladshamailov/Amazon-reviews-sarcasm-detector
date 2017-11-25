//import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
//import com.amazon.sqs.javamessaging.ProviderConfiguration;
//import com.amazon.sqs.javamessaging.SQSConnection;
//import com.amazon.sqs.javamessaging.SQSConnectionFactory;
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.profile.ProfileCredentialsProvider;
//import com.amazonaws.services.ec2.AmazonEC2;
//import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.sqs.AmazonSQS;
//import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
//import com.amazonaws.services.sqs.model.DeleteMessageRequest;
//import com.amazonaws.services.sqs.model.Message;
//import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
//import com.amazonaws.services.sqs.model.SendMessageRequest;
//import com.google.gson.Gson;
//
//import javax.jms.*;
//import java.util.*;
//
//public class Workers {
//
//    public static AmazonS3 S3;
//    public static AmazonEC2 ec2;
//    public static SQSConnectionFactory connectionFactory;
//    public static AWSCredentialsProvider credentialsProvider;
//    public static List<String> URLlist;
//    public static boolean terminate = false;
//    public static SentimentAnalysis sentimentAnalysis;
//    public static NamedEntityRecognition namedEntityRecognition;
//
//    public static void main(String [] args){
//        initialize();
//        getMsg();
//    }
//    public static void initialize() {
//        credentialsProvider = new AWSStaticCredentialsProvider
//                (new ProfileCredentialsProvider().getCredentials());
//        ec2 = AmazonEC2ClientBuilder.standard()
//                .withCredentials(credentialsProvider)
//                .withRegion("us-west-2")
//                .build();
//        System.out.println("===========================================");
//        System.out.println("connect to aws & ec2");
//        System.out.println("===========================================\n");
//        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
//                new ProviderConfiguration(),
//                AmazonSQSClientBuilder.standard()
//                        .withRegion("us-west-2")
//                        .withCredentials(credentialsProvider)
//        );
//        System.out.println("===========================================");
//        System.out.println("Getting Started with Amazon SQS");
//        System.out.println("===========================================\n");
//        sentimentAnalysis=new SentimentAnalysis();
//        namedEntityRecognition=new NamedEntityRecognition();
//    }
//
//    public static void getMsg() {
//
//        MangerToWorker = sqs.listQueues("MangerToWorker").getQueueUrls().get(0);    //changed from get(2)
//        WorkerToManager = sqs.listQueues("WorkerToManager").getQueueUrls().get(0);  // changed from get(3)
//        while (!terminate) {
//            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(MangerToWorker);
//            List<Message> messages = sqs.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();
//            for (final Message message : messages) {
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    public void run() {
//                        sqs.changeMessageVisibility(MangerToWorker, message.getReceiptHandle(), 30000);
//                    }
//                }, 25000);
//
//            workOnMsg(message);
//           // deleteMess(message);
//            }
//
//        }
//    }
//    public static void workOnMsg(Message message){
//        Review review = new Gson().fromJson(message.getBody(), Review.class);
//            int reviewGrade= sentimentAnalysis.findSentiment(review.getText());
//            ArrayList<String> a=namedEntityRecognition.printEntities(review.getText());
//            ReviewResponse reviewResponse=new ReviewResponse(review,a,reviewGrade,Math.abs(review.getRating()-reviewGrade)>2);
//            sendMessage(reviewResponse);
//
//    }
//
//    public static void sendMessage(ReviewResponse reviewResponse) {
//        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
//                new ProviderConfiguration(),
//                AmazonSQSClientBuilder.standard()
//                        .withRegion("us-west-2")
//                        .withCredentials(credentialsProvider)
//        );
//        Connection connection = null;
//        try {
//            Gson gson=new Gson();
//            connection = connectionFactory.createConnection();
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//            MessageProducer producer = session.createProducer(session.createQueue("WorkerToManager"));
//            TextMessage message = session.createTextMessage(gson.toJson(reviewResponse).toString());
//            producer.send(message);
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//}
//
//
