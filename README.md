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

* @TODO: add more detailes about how the program works *


## Instances and performances
The instance we are using in the program is "ami-e535c59d" and the type is "micro".

After inserting the input files , it took our program ### TODO time to finish the work on them

