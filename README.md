# Amazon reviews sarcasm detector
In this assignment we implemented a real-world application that distributively processes a list of amazon reviews,
performs sentiment analysis and named entity recognition,and displays the result on a web page.

## Team
We are two students and we implemented the project together.
Our names:

Elad Shamailov
Mor Bitan

## Instructions 
There are a few steps we have to follow in order to run the application smoothly.
First , we need to Download the input files and place them in:
```
/users/studs/bsc/2015/eladsham/workspace/Dsp181/
```
The input files we used to test the application are listed below:

* [0689835604](https://www.cs.bgu.ac.il/~dsp181/wiki.files/0689835604)
* [B000EVOSE4](https://www.cs.bgu.ac.il/~dsp181/wiki.files/B000EVOSE4)
* [B001DZTJRQ](https://www.cs.bgu.ac.il/~dsp181/wiki.files/B001DZTJRQ)
* [B0047E0EII](https://www.cs.bgu.ac.il/~dsp181/wiki.files/B0047E0EII)
* [B01LYRCIPG](https://www.cs.bgu.ac.il/~dsp181/wiki.files/B01LYRCIPG)

Next, we have to upload the following zip files to our bucket:

1.manager.zip

2.worker.zip

Now, we need to run this line in the terminal with the names of our input files and the n we chose.
```
java -jar Assignment1.jar inputFileName1… inputFileNameN outputFileName1… outputFileNameN n
```
 if we want to terminate the manager:
 ```
 java  -jar Assignment1.jar inputFileName1… inputFileNameN outputFileName1… outputFileNameNn terminate
```

## How the program works:
Local Application:
* The first localapp creates the bucket if it doesn't excit and two queues: AppToManager and ManagerToApp+uuid.
* Each of the next localapps creates only ManagerToApp+uuid (the uuid will separated between the localApps .
* The localapp uploads the input file of the amazon reviews to the s3.
* The localapp sends a message to the queue AppToManager
(this queue used to send messages between the localApps to the manager.
there are 3 types of message that the local Apps can  send to the manager:
 1. Url message
 2. Terminate message
 3. WorkerNum message

 1. url massage- contains : action = define which message we are sending.
                            uuid= define which local app send the message.
                            bucketName+ key+ URL = the manager will used them to download the file.
 2. terminate message- contain: action=define which message we are sending.
                                uuid= define which local app send the message.
 3. workerNum message- this message define how many workers will create after X message.
                        contains: action+ uuid(as before)
                                   the number of workers.
 * All messages are converted to Gson format, which helps us get the data in a simple way.
 * After sending all the mesages the local app is waiting to get the output-message.
 * When the local app gets this message, it downloads the output files from s3, and create an html file.

Manager:
*open 2 queues workerToManger queue and MnagerToWorker queue.
*There are 3 types of threads that works on the Manager:
1. SQS Thread- this thread is "listening" to the "AppToManager" queue in busy-wait form. This way we can always get a new messages.
when the manager gets a new message this thread put it in a local queue, and continue to "listen" to the queue.
(there is only one thread like this).
2. Manager Thread- this thread responsible for all actions required doing on a received message:
   - download the file from s3
   - sorting message types
   - put the name of the file and the number of messages that it contains in a map.
   - parse the message to an easier format
   - creates a new smaller messages to send to the workers.
    this thread also creates workers, and sent the messages.
*the manager can send 2 types of message:
1. review message to the workers.
2.output message to the local application

***important****
there are more than one thread of this kind- we create a thread pool with a limitation of 100 thread.
In cases where the load on the manager is large, the program will not collapse but will work slower.

*The messages the manager transfer to the workers are Review Messages:
Review Messages: contains action, ArrayList<Review>,title and fileName;
3. Worker Thread- this thread is "listening" to the " workerToManager" queue in busy-wait form.
  is responsible to create the output message and the output file and also upload it to thr s3.
* after the manager  gets all review respose from the workers it shut them down, and stops all his threads.

Workers:
* Open by the manager thread, perform actions on the message review, and create a review response that had:
the original review,
a List of <String>- name entities,
sentiment (number) and if the review is or not sarcastic.
* The workers sends the review response to the manager through the workerToManager queue.
* The workers, manager and local application Working in full cooperation
and waiting for the other to finish his work so they can continue their work.

## Instances and performances
The instance we are using in the program is "ami-32d8124a" and the type is "micro" for the manager,
and ami-bf4193c7 type "medium"for the workers.
After inserting the input files , it took our program with n=15 about five minutes to work on all programs.

The n we used is: 15.
Because there are 5 files, each file has about 49 review messages.
So we received a 49*5= 245 messages to be performed.
There is a limit to the number of instances that can be opened- 20 at most,
so 245/15 = 16 instances of workers that will open.

## Q&A

**Question:** Did you think for more than 2 minutes about security?
**Answer:** We took security very seriously. We never hard coded the credentials in the program ,
we are using the EnvironmentVariableCredentialsProvider to get the credentials in the localApp.
To run the instances remotely , we use InstanceProfileCredentialsProvider and IAM roles ,
specific role for the manager and specific role for the workers. In this way ,
we never expose our credentials , because we never send them in plain text or in any file.
The zip file is encoded with a strong password , and decoded from the manager after it gets the zip.

**Question:** Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? Scalability is very important aspect of the system, is it scalable?
**Answer:**  Yes , we thought about scalability.
The manager works with fixedThreadPool , we set a constant numebr of threads: "100"
the manager executes with it only the "ManagerThread".
The program will work properly with a large amount of users ,
because it will not collapse the amount of thread is limited so it will just slow down the progam.

**Question:** What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!
**Answer:** We used SQSConnectionFactory that in its connection creation,
you can define how you want the queue to work with the messages.
we used "CLIENT_ACKNOWLEDGE",meaning that when a message read from the queue, its not deleted until we wrote: message.acknowledge.
like that we assure that every message that hasn't being taking care of automatically will return to the queue,
and every message that have been acknowledged will be deleted.(we put acknowledgement just after the workers send the review response)
If The manager will stop work for a few seconds the local- app and the workers depends on it so they will continue to wait till they get there messages.

**Question:** Threads in your application, when is it a good idea? When is it bad?
**Answer:**  As we explained above, the manager uses  3 types of threads.
This is a good idea because we are creating two threads that will listen constantly to the localApp-manger queue and the worker-manger queue.
This is how we will know when there is a new message to treat, and also an review response to send.
Also there is a thread poll of manager threads- so the significant part - handling the message and the creation of workers can be done by a number threads.
This way, when there are more clients, there messages will be share by the threads and they get there messages in a short time comparing if we did it without threads.
Notice that there is a  one thread only who listens to local-manager queue. This is because if we split this work to more threads, we may not be able to read the entire message or transfer it as required.
It's a bad idea to use threads for worker , because a single-thread worker will work faster due to the low amount of time required to process a message.
Also, there is no need to use thread in the workers, any worker gets only one message, so if we want the messages to being care of more fast we need to open more workers.
Thread in the workers will give us nothing because all the action must occur in a certain order.
It's also a bad idea to use threads in localapp, because it can cause us problems with the download of the input files due to cpu stealing time , and the running time won't improve , and maybe even will get worse.

**Question:** Did you run more than one client at the same time? Be sure they work properly, and finish properly, and your results are correct.
**Answer:**  Yes. We run a few clients , each client as a uuid that separate it from the others, so the manager will know which local-app send him the messages.
This way when the manager gets a terminate message he knows to which local app he need to keep listening, moreover he knows how to send the output message so the right app will get its messages.

**Question:** Do you understand how the system works? Do a full run using pen and paper, draw the different parts and the communication that happens between them.
**Answer:**  yes, we do understand how the system works. Attached a pic of the communication between all the parts.

<p align="center">
  <img src="https://github.com/eladshamailov/Assignment1/blob/master/programFlow.jpg?raw=true" width="600" height = "450"/>
</p>

**Question:** Did you manage the termination process? Be sure all is closed once requested!
**Answer:**  yes, when we give the app a terminate it sends it to the manager.
If the manager gets a terminate message he change a boolean variable to "true"
so he knows that when all the files are ready, he need to shut down all the workers and then stops all his threads.
After getting all the output messages and creating html files,
the local app :
1. if it sends the terminate message it will terminate the manager and after that, close all the connections and stops.
2. else it only close the connections and stops.

**Question:** Did you take in mind the system limitations that we are using? Be sure to use it to its fullest!
**Answer:**  **yes. the system can't produced more than 20 instances,
so we limited the number of active workers to be 19.
Moreover if more than 1 app will try to create the manager, it will not succeed,
it will get a message that say :" the manager is already active".

**Question:** Are all your workers working hard? Or some are slacking? Why?
**Answer:**  No. not all the workers work the same, some of them works on a big amount of messages and some of them works on less.
We think it's because the workers are not created at the same time, the first worker works alone until the 29 message.
Because of that those who are created first works harder.

**Question:** Is your manager doing more work than he's supposed to? Have you made sure each part of your system has properly defined tasks? Did you mix their tasks? Don't!
**Answer:**The manager is responsible to:
1. creating the manager-workers queues.
2. listen to the app-manager queue and get the messages.
3. define which message was sent, work on each message and send a review message to the workers.
4. listen to the worker-manager queue and get the messages, send a output message to tha local-app,creating a output file and uploading it to the S3.
5. terminate all the workers
6. stops all the threads

*All the tasks above, the manager is supposed to perform,
he is not supposed to worry about whether the review is sarcastic or not,
and that's why the workers do this task.

**Question:** are you sure you understand what distributed means? Is there anything in your system awaiting another?
**Answer:** A distributed system: (by WIKIPEDIA: "is a model in which components located on networked computers communicate and coordinate their actions by passing messages")
as we can see in our program The local application create a instance that running on other computer and communicate with it with messages.
The manager is a computer that belongs to a computer network on amazon.
The manager uploads more computers from amazon network- the workers, each worker works on a different computer and they communicate only with the manager with messages.
All the computers work together on the same mission, so the mission distributed between all of them.
The local-app is waiting to the manager to create all its output files.
The manager is waiting to the workers to create all the review response for the files that he sent.
All the computers are interdependent.
