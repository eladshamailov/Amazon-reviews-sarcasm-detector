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
        URLlist=Manager.sqs.listQueues().getQueueUrls();
        for (String queueUrl :URLlist ) {
            System.out.println("  QueueUrl: " + queueUrl);
            System.out.println();
            // Receive messages
            while (run) {
                System.out.println("Receiving messages from AppToManager.\n");
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                List<Message> tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();

                int k=0;
                for (Message m : tmp) {
                    add(m);
                    tmp.remove(k);
                    k++;
                    if(tmp.size()==0){
                        break;
                    }
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
            if(tmp.size()==0){
                shutdown();
            }
            }
            try {
                System.out.println("the Thread in SqS:"+Thread.currentThread().getName());
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("end function");
        System.out.println("end function");
        doWork.set(true);


    }


    public static void  add(Message m) {
        messages.add(m);
        count.getAndIncrement();
        System.out.println("counter is: "+count);
        synchronized (messages) {
            messages.notifyAll();
        }
    }
}
