import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;

import java.io.*;
import java.text.ParseException;
import java.util.List;

public class ManagerThread implements Runnable{
    S3Object obj;
    @Override
    public void run() {
        Manager.credentialsProvider =
                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        Manager.S3 = AmazonS3ClientBuilder.standard()
                .withCredentials(Manager.credentialsProvider)
                .withRegion("us-west-2")
                .build();

        for (int i = 0; i <SQSthread.messages.size(); i++) {
            int count = 0;
            File file= null;
            obj= LocalApp.S3.getObject(LocalApp.bucketName, LocalApp.keys.elementAt(i));
            InputStream in = obj.getObjectContent();
            byte[] buf = new byte[1024];
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                count=in.read(buf);
            while(count!= -1)
            {
                if( Thread.interrupted() )
                {
                    throw new InterruptedException();

                }
                out.write(buf, 0, count);
            }
            out.close();
            in.close();
            Manager.parse(file);
        }   catch (InterruptedException e) {
        e.printStackTrace(); }
            catch (FileNotFoundException e) {
            e.printStackTrace();
            }
            catch (IOException e) {
            e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }
        }

    }
}
