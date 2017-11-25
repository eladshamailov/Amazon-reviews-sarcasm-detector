import org.apache.commons.codec.binary.Base64;

public class Script {

    String managerScript;

    Script(String publicKey, String privateKey){
        StringBuilder builder = new StringBuilder();
//        builder.append("#!/bin/sh\n");
//        builder.append("BIN_DIR=/tmp\n");
//        builder.append("mkdir -p $BIN_DIR/dependencies\n");
//        builder.append("cd $BIN_DIR/dependencies\n");
//        builder.append(" echo 1 > file \n");
//        builder.append("AWS_ACCESS_KEY_ID=" + publicKey+"\n");
//        builder.append("echo 2 > file \n");
//        builder.append("AWS_SECRET_ACCESS_KEY=" + privateKey+"\n");
//        builder.append("echo 3 > file \n");
//        builder.append("AWS_DEFAULT_REGION=us-west-2\n");
//        builder.append("echo 4 > file \n");
//        builder.append("export AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_DEFAULT_REGION\n");
//        builder.append("echo 5 > file \n");
//        builder.append("wget https://s3-us-west-2.amazonaws.com/akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/ManagerDep.zip\n");
//        builder.append("echo 6 > file \n");
//        builder.append("unzip ManagerDep.zip\n");
//        builder.append("echo 7 > file \n");
//        builder.append("mkdir -p $BIN_DIR/running\n");
//        builder.append("cd $BIN_DIR/running\n");
//        builder.append("mkdir -p $BIN_DIR/running/lib\n");
//        builder.append("cp ../dependencies/*.jar lib/\n");
//        builder.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/Assignment.jar Assignment1.jar\n");
//        builder.append("echo accessKey=$AWS_ACCESS_KEY_ID > credentials.file\n");
//        builder.append("echo secretKey=$AWS_SECRET_ACCESS_KEY >> credentials.file\n");
//        builder.append("sudo java -jar Assignment1.jar\n");

        builder.append("#!/bin/sh\n");
      //  builder.append("BIN_DIR=/tmp\n");
       // builder.append("mkdir -p $BIN_DIR/dependencies\n");
       // builder.append("cd $BIN_DIR/dependencies\n");
       // builder.append(" echo 1 > file \n");
       // builder.append("AWS_ACCESS_KEY_ID=" + publicKey+"\n");
       // builder.append("echo 2 > file \n");
        //builder.append("AWS_SECRET_ACCESS_KEY=" + privateKey+"\n");
        //builder.append("echo 3 > file \n");
//        builder.append("AWS_DEFAULT_REGION=us-west-2\n");
//        builder.append("echo 4 > file \n");
//        builder.append("export AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_DEFAULT_REGION\n");
//        builder.append("echo 5 > file \n");
        builder.append("wget https://s3-us-west-2.amazonaws.com/akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/ManagerDep.zip\n");
//        builder.append("echo 6 > file \n");
        builder.append("unzip ManagerDep.zip\n");
//        builder.append("echo 7 > file \n");
//        builder.append("mkdir -p $BIN_DIR/running\n");
//        builder.append("cd $BIN_DIR/running\n");
//        builder.append("mkdir -p $BIN_DIR/running/lib\n");
//        builder.append("cp ../dependencies/*.jar lib/\n");
        builder.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/Assignment1.jar Assignment1.jar\n");
//        builder.append("echo accessKey=$AWS_ACCESS_KEY_ID > credentials.file\n");
//        builder.append("echo secretKey=$AWS_SECRET_ACCESS_KEY >> credentials.file\n");
        builder.append("sudo java -jar Assignment1.jar\n");



        managerScript = new String(Base64.encodeBase64(builder.toString().getBytes()));

    }

    public String getManagerScript() {
        return managerScript;
    }
}
