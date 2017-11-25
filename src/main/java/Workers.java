import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.gson.Gson;

import javax.jms.*;
import java.util.*;

public class Workers {

    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static SQSConnectionFactory connectionFactory;
    public static AWSCredentialsProvider credentialsProvider;
    public static List<String> URLlist;
    public static boolean terminate = false;
    public static SentimentAnalysis sentimentAnalysis;
    public static NamedEntityRecognition namedEntityRecognition;

    public static void main(String [] args){
        initialize();
        while(!terminate) {
            getMsg();
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
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(credentialsProvider)
        );
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        sentimentAnalysis=new SentimentAnalysis();
        namedEntityRecognition=new NamedEntityRecognition();
    }

    public static void getMsg() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(Manager.credentialsProvider)
        );
        try {

            SQSConnection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue("ManagerToWorker"));
            connection.start();
            Message message;
            message = consumer.receive();
            workOnMsg(message);
           // deleteMess(message);
            } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void workOnMsg(Message message){
      try {
          Gson gson = new Gson();
          String text = (((TextMessage) message).getText());
          Msg msg = gson.fromJson(text, Msg.class);
          if(msg.getAction()==2) {
              ReviewMsg reviewMsg = new Gson().fromJson(text, ReviewMsg.class);
              String fileName= reviewMsg.getFileName();
              ArrayList<Review> reviews = reviewMsg.getReviews();
              for (int i = 0; i < reviews.size(); i++) {
                  int reviewGrade = sentimentAnalysis.findSentiment(reviews.get(i).getText());
                  ArrayList<String> a = namedEntityRecognition.printEntities(reviews.get(i).getText());
                  reviews.get(i).setFileName(fileName);
                  ReviewResponse reviewResponse = new ReviewResponse(reviews.get(i), a, reviewGrade, Math.abs(reviews.get(i).getRating() - reviewGrade) > 2);
                  sendMessage(reviewResponse);
              }
          }
          else {
              terminate=true;
          }
          message.acknowledge();

      } catch (JMSException e) {
          e.printStackTrace();
      }

    }

    public static void sendMessage(ReviewResponse reviewResponse) {
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion("us-west-2")
                        .withCredentials(credentialsProvider)
        );
        Connection connection = null;
        try {
            Gson gson=new Gson();
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue("WorkerToManager"));
            TextMessage message = session.createTextMessage(gson.toJson(reviewResponse).toString());
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}


