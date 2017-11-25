# Assignment1
In this assignment we implemented a real-world application that distributively processes a list of amazon reviews, performs sentiment analysis and named entity recognition, and displays the result on a web page.

## Team
We are two students and we implemented the project together.
Our names and ids:

Elad Shamailov 308540202

Mor Bitan 305537383
## Instructions 
There are a few steps we have to follow in order to run the apllication smoothly.
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

## How the program works
* The localapp creates a bucket and two queues: AppToManager and ManagerToApp.
* The localapp uploads the input file of the amazon reviews to the s3
* The localapp sends a message to the queue AppToManager , the message contains the bucketname, key and the location of the file in s3.
we are using json to read and write the information in more convenient way.
* The manager is always waiting for new messages using a fixedThreadPool and a SQSthread that we created. once a new message received ,
The SQSthread detects it and than the manager downloads the message using the SQSthread
* Using the fixedThreadPool, the manager sets on the managerThread , which parses the downloaded messages.

**@TODO: add more detailes about how the program works**


## Instances and performances
The instance we are using in the program is "ami-32d8124a" and the type is "micro".

After inserting the input files , it took our program **@TODO insert time** time to finish the work on them.

The n we used is: **@TODO: insert the n we used** 

## Q&A
**Question:** Did you think for more than 2 minutes about security?

**Answer:** We took security very seriously.We never hard coded the credentials in the program ,we use the EnvironmentVariableCredentialsProvider to get the credentials.
The zip file is encoded with a strong password , and decoded from the manager after it gets the zip.
No one sends the credentials in plain text , not we and no where in the program.
**@TODO:Add if we used custom ami/chain/enviroment varibales**

**Question:** Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? Scalability is very important aspect of the system, is it scalable?

**Answer:**  Yes , we thought about scalability. The manager works with fixedThreadPool , we set a constant numebr of threads: "z" and the manager executes the "ManagerThread" when we recieves new message.
The program will work properly with a large amount of users ,because for each new user, we set it's own two queues. in this way , we can assure that for every client , the received message and the sent message will be the right answer and no problen shoule occur.
We have to put in mind , that a large number of clients will really slow down the program , because of technical issues.
**@TODO:change it , we edited the code**

**Question:** What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!

**Answer:** We used the Visibility Timeout. If a worker gets a message , we do not remove immidietally the message. By using the Visibility Timeout , once a worker receives a message , the Visibility Timeout prevents other workers from receiving and processing the message for some time( we chose 25 seconds, it can be changes in the worker class ).
If the node dies , the message will get back to the queue (we never deleted it) , and another worker will take it.
If the node stalls for a while , the message will get back to the queue , the worker will stop working on it and another worker will handle it.
If there is a broken communication, we will detect a failed ReceiveMessage action , and than we will retry as many times as necessary, using the same receive request attempt ID. multiple retries do not affect the ordering of messages.
If we detect a failed SendMessage action, we will retry sending as many times as necessary, using the same message deduplication ID.

**Question:** Threads in your application, when is it a good idea? When is it bad?

**Answer:** The manager uses a fixedThreadPool with a constant "z" that we choose. we have the class SQSThread , the manager sets on this thread , and his purpose is to wait all the time for new messages from the localapp. once a new message received, the SQSThread detects it , and then the Manager sets on another thread to parse the message. using the thread pool and the ManagerThread class , the manager sets on a thread for every meesage (we have "z" threads available).
In this way , we can process a big amount of messages while not overloading the system.
IF there are more than "z' messages , the program will wait until some thread will finish his job , and than we will process the message.

It's a bad idea to use threads for worker , because a single-thread worker will work faster due to the low amount of time required to process a message.
It's also a bad idea to use threads in localapp, because it can cause us problems with the download of the input files due to cpu stealing time , and the running time won't improve , and maybe even will get worse.

**Question:** Did you run more than one client at the same time? Be sure they work properly, and finish properly, and your results are correct.

**Answer:**  Yes. We tried to run the program on a few clients , and it works properly with correct results.

**Question:** Do you understand how the system works? Do a full run using pen and paper, draw the different parts and the communication that happens between them.

**Answer:**  **@TODO: answer the question**

**Question:** Did you manage the termination process? Be sure all is closed once requested!

**Answer:**  **@TODO: answer the question**

**Question:** Did you take in mind the system limitations that we are using? Be sure to use it to its fullest!

**Answer:**  **@TODO: answer the question**

**Question:** Are all your workers working hard? Or some are slacking? Why?

**Answer:**  **@TODO: answer the question**

**Question:** Is your manager doing more work than he's supposed to? Have you made sure each part of your system has properly defined tasks? Did you mix their tasks? Don't!

**Answer:**  **@TODO: answer the question**

**Question:** are you sure you understand what distributed means? Is there anything in your system awaiting another?

**Answer:**  **@TODO: answer the question**






