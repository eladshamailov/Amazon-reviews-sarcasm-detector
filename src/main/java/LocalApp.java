import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.gamelift.model.DescribeInstancesRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

public class LocalApp {
    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonSQS sqs;
    public static RunInstancesRequest request;
    public static List<Instance> instances;
    public static String listOfin;
    public static String bucketName;
    public static AWSCredentialsProvider credentialsProvider;
    public static Vector<String> keys=new Vector<String>() ;
    public static String MangerToApp;
    public static String AppToManager;
    public static boolean Terminate=  false;

    public static void main(String[] args) throws Exception {
        init();
        startS3("C:\\Users\\Mor\\IdeaProjects\\Assignment1");
        UpToS3("C:/Users/Mor/IdeaProjects/docs");
        createQueue();
        sendMesage();
        while (!Terminate) {
            getOutput();
            createHTML();
        }
    }
    //initilize and creating instance
    public static void init() {
        credentialsProvider = new AWSStaticCredentialsProvider
                (new ProfileCredentialsProvider().getCredentials());
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
       Script s=new Script(credentialsProvider.getCredentials().getAWSAccessKeyId(),credentialsProvider.getCredentials().getAWSSecretKey());
       System.out.println("the script for the Manager: "+s);
       if (!isActive()) {
            try {
                request = new RunInstancesRequest("ami-e535c59d", 1, 1);
                request.setInstanceType(InstanceType.T2Micro.toString());
                request.setUserData(s.getManagerScript());
                request.withKeyName("morKP");
                request.withSecurityGroups("mor");
                instances = ec2.runInstances(request).getReservation().getInstances();
                System.out.println("create instances: " + instances);

            } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
            }
        }
    }

    public static boolean isActive() {
        DescribeInstancesRequest dis = new DescribeInstancesRequest();
        listOfin = dis.getInstanceId();
        DescribeInstancesResult disresult = ec2.describeInstances();
        List<Reservation> list = disresult.getReservations();
        System.out.println("-------------- status of instances -------------");
        if (list.size() > 0) {
            for (Reservation res : list) {
                List<Instance> instancelist = res.getInstances();
                for (Instance instance : instancelist) {
                    if (instance.getState().getName().equals("running")) {
                        System.out.println("The Manager is allready active");
                        return true;
                    }
                    List<Tag> t1 = instance.getTags();
                    for (Tag teg : t1) {
                        System.out.println("Instance Name   : " + teg.getValue());
                    }
                }
            }
        }
        return false;
    }

    public static void startS3(String directoryName) {
//        credentialsProvider =
//                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        S3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        bucketName = credentialsProvider.getCredentials()
                .getAWSAccessKeyId() + "-" + directoryName.replace
                ('\\', '-').replace('/', '-').replace
                (':', '-');
        bucketName = bucketName.toLowerCase();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon S3");
        System.out.println("===========================================\n");
        try {
            System.out.println("Creating bucket " + bucketName + "\n");
            S3.createBucket(bucketName);
            /*
             * List the buckets in your account
             */
            System.out.println("Listing buckets");
            for (Bucket bucket : S3.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }
            System.out.println();
        } catch (AmazonServiceException ace) {
            System.out.println("Start s3");
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static void UpToS3(String directoryName) {
        try {
            System.out.println("Uploading a new object to S3 from a file");
            int i = 0;
            File dir = new File(directoryName);
            for (File file : dir.listFiles()) {
                keys.add(file.getName().replace
                        ('\\', '-').replace('/', '-').
                        replace(':', '-'));
                PutObjectRequest req = new PutObjectRequest(bucketName, keys.elementAt(i), file);
                S3.putObject(req);
                i++;
            }
        } catch (AmazonServiceException ace) {
            System.out.println("Uploading");
            System.out.println("Caught an AmazonClientException," +
                    " which means the client encountered "
                    + "a serious internal problem while trying to communicate with " + "S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

//create the queues for manager- localApp communication
    public static void createQueue() {
//        credentialsProvider =
//                new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        System.out.println("start a connection with the sqs");
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");

        System.out.println("Creating a new SQS queue called MangerToApp.\n");
        CreateQueueRequest createQueueRequest1 = new CreateQueueRequest
                ("MangerToApp" + UUID.randomUUID());
        MangerToApp = sqs.createQueue(createQueueRequest1).getQueueUrl();
        System.out.println("Creating a new SQS queue called AppToManager.\n");
        CreateQueueRequest createQueueRequest2 = new CreateQueueRequest
                ("AppToManager" + UUID.randomUUID());
        AppToManager = sqs.createQueue(createQueueRequest2).getQueueUrl();
        //list queue
        System.out.println("Listing all queues in your account.\n");
        for (String queue : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queue);
        }
        System.out.println();
    }
    //send a URLmsg
    public static void sendMesage() {
        credentialsProvider=new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        for (int i = 0; i < keys.size(); i++) {
            System.out.println("Sending a " + i + "message to ApptoMamager\n");
            System.out.println("the key:"+keys.elementAt(i).toString());
            UrlMsg urlMsg = new UrlMsg(bucketName,keys.elementAt(i),S3.getUrl(bucketName,keys.elementAt(i)).toString());
            Gson gson=new Gson();
            sqs.sendMessage(new SendMessageRequest(AppToManager, gson.toJson(urlMsg).toString()));
        }
    }
    //get an output from the queue
    public static void getOutput(){
        sqs.listQueues("ManagerToWorker").getQueueUrls().get(1);
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(MangerToApp);
        List<Message> tmp = Manager.sqs.receiveMessage(receiveMessageRequest).getMessages();
        OutputMsg outputMsg=tmp.get(0);
        if(tmp.size()>0){
            createHTML();
        }
    }

    public static void createHTML(){ }

    public static void Terminate(){
        TerminateMsg terminateMsg = new TerminateMsg();
        Gson gson=new Gson();
        sqs.sendMessage(new SendMessageRequest(AppToManager, gson.toJson(terminateMsg).toString()));
    }

}



