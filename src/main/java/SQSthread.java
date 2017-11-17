import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;



public class SQSthread implements Runnable{
    public static List<String> URLlist;
    public static ConcurrentLinkedQueue<Message> messages=new ConcurrentLinkedQueue<>();
    private boolean run=true;
    public static AtomicInteger count=new AtomicInteger(0);
    public static AtomicBoolean doWork=new AtomicBoolean(false);

    public void shutdown(){
        run=false;
    }

    @Override
    public void run() {
        System.out.println("In run");
        Manager.credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        Manager.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        System.out.println("Listing all queues in your account.\n");
        URLlist = Manager.sqs.listQueues().getQueueUrls();
        for (String queueUrl : URLlist) {
            System.out.println("  QueueUrl: " + queueUrl);
            System.out.println();
            // Receive messages
            while (run) {
                System.out.println("Receiving messages from AppToManager.\n");
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(10);
                List<Message> tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
                    for (Message m : tmp) {
                        Manager.add(m);
                    }
                    for (Message m:tmp){
                        Manager.sqs.deleteMessage(new DeleteMessageRequest(queueUrl,m.getReceiptHandle()));
                    }
                for (Message message : messages) {
                    System.out.println("  Message");
                    System.out.println("    MessageId:     " + message.getMessageId());
                    System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
                    System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
                    System.out.println("    Body:          " + message.getBody());
                    for (Entry<String, String> entry : message.getAttributes().entrySet()) {
                        System.out.println("  Attribute");
                        System.out.println("    Name:  " + entry.getKey());
                        System.out.println("    Value: " + entry.getValue());
                    }
                }
                System.out.println();
                if(messages.size()>0){
                    doWork.set(true);
                }
                if (tmp.size()==0) {
                    try {
                        System.out.println("the Thread that is waiting:" + Thread.currentThread().getName());
                        synchronized (messages){
                            messages.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("end function");
                //doWork.set(true);

            }
        }
    }



}
