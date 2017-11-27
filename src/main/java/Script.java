import org.apache.commons.codec.binary.Base64;

public class Script {

    private String managerScript;
    private String workersScript;


    Script(){

    }

    public void setManagerScript() {
        StringBuilder managerBuild = new StringBuilder();
        managerBuild.append("#!/bin/sh\n");
        managerBuild.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/ManagerDep.zip ManagerDep.zip\n");
        managerBuild.append("unzip ManagerDep.zip\n");
        managerBuild.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/manager.jar manager.jar\n");
        managerBuild.append("java -jar manager.jar\n");
        this.managerScript = new String(Base64.encodeBase64(managerBuild.toString().getBytes()));
    }



    public void setWorkersScript() {
        StringBuilder workersBuild = new StringBuilder();
        workersBuild.append("#!/bin/sh\n");
        workersBuild.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/WorkersDep.zip WorkersDep.zip\n");
        workersBuild.append("unzip WorkersDep.zip\n");
        workersBuild.append("aws s3 cp s3://akiaisatt3u2kglwoipq-c--users-mor-ideaprojects-assignment1/workers.jar workers.jar\n");
        workersBuild.append("java -jar workers.jar\n");
        this.workersScript = new String(Base64.encodeBase64(workersBuild.toString().getBytes()));
    }

    public String getManagerScript() {

        return managerScript;
    }

    public String getWorkersScript() {

        return workersScript;
    }
}
