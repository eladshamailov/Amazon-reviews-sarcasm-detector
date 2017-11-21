# Assignment1
In this assignment we implemented a real-world application that distributively processes a list of amazon reviews, performs sentiment analysis and named entity recognition, and displays the result on a web page.

## instructions 
There are a few steps we have to follow in order to run the apllication smoothly.
First , we need to Download the input files and place them in:
```
/users/studs/bsc/2015/eladsham/workspace/Dsp181/
```
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
