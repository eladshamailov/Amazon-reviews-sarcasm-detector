import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagerThread implements Runnable{
    S3Object obj;
    public static AtomicBoolean doWork=new AtomicBoolean(false);

    List<Bucket> bucketList;
    @Override
    public void run() {
        Manager.credentialsProvider =
                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        Manager.S3 = AmazonS3ClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        while(SQSthread.messages.size()>0) {
            int count = 0;
            Msg msg = new Gson().fromJson(SQSthread.messages.peek().getBody(), Msg.class);
            if (msg.getAction() == Actions.fromInt("URL")) {
                File file = new File("localFile");
                UrlMsg urlMsg = new Gson().fromJson(SQSthread.messages.peek().getBody(), UrlMsg.class);
                obj = Manager.S3.getObject(urlMsg.getBucketName(), urlMsg.getKey());
                InputStream reader = new BufferedInputStream(
                        obj.getObjectContent());
                OutputStream writer = null;
                try {
                    writer = new BufferedOutputStream(new FileOutputStream(file));
                    int read = -1;

                    while ((read = reader.read()) != -1) {
                        writer.write(read);
                    }
                    writer.flush();
                    writer.close();
                    reader.close();
                    Manager.parse(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (msg.getAction() == Actions.fromInt("TERMINATE")) {
                 //create a function that terminate the manager
                }
            }
            SQSthread.messages.poll();
        }
        doWork.set(true);
    }
}
