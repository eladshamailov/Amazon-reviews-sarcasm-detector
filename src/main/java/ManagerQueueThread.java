import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

import java.util.List;

public class ManagerQueueThread implements Runnable {


    @Override
    public void run() {
        Manager.sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        //long pooling from local
        SetQueueAttributesRequest set_attrs_request = new SetQueueAttributesRequest()
                .withQueueUrl(LocalApp.AppToManager)
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
        Manager.sqs.setQueueAttributes(set_attrs_request);
        ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
                .withQueueUrl(LocalApp.AppToManager)
                .withWaitTimeSeconds(20);
        Manager.sqs.receiveMessage(receive_request);

    }
}
