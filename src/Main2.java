import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Main2 {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat simpleDateFormatWithDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    public static ArrayList<Object[]> pingLogs = new ArrayList<>(10000);
    public static long startTimeOfPinging;
    public static boolean continueRunningWhile = true;
    public static int timeOutForPingMs = 200;
    public static int waitBetweenPings = 500;
    public static String pathToFileLastDisconnect = "D:\\intellijWorkspace2\\PingMonitoring\\lastDisconnect.txt";
    public static long lastDisconnectedFromFile = -1;
    public static File lastDisconnectFile;

    public static void main(String[] args) throws InterruptedException {
        String address;
        InetAddress inet;
        String hostToPing = "www.google.com";

        try {
            System.out.println("Getting IP of " + hostToPing);
            address = InetAddress.getByName(hostToPing).getHostAddress();
            inet = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Failed. Check your internet connection. Error: " + e);
            System.exit(1);
            return;
        }

        lastDisconnectFile = new File(pathToFileLastDisconnect);
        if (!lastDisconnectFile.exists()) {
            try {
                lastDisconnectFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Unable to create a new file: " + e);
                System.exit(1);
                return;
            }
        }
        String fileContent;
        try {
            fileContent = Files.readString(Path.of(lastDisconnectFile.getPath()));
        } catch (IOException e) {
            System.out.println("Unable to read the file: " + e);
            System.exit(1);
            return;
        }

        try {
            lastDisconnectedFromFile = Long.parseLong(fileContent);
        }catch (Exception e){
            System.out.println("File '"+lastDisconnectFile.getAbsolutePath()+"' does not contain a number");
        }


        SignalHandler handler = createSignalHandler();
        Signal.handle(new Signal("INT"), handler);

        startTimeOfPinging = System.currentTimeMillis();
        System.out.println("Pinging " + hostToPing + " [" + address + "]");
        while (continueRunningWhile) {
            long whileStart = System.currentTimeMillis();
            Object[] pinginfo = Pinger.doOnePing(inet, timeOutForPingMs);
            boolean reached = (boolean) pinginfo[0];
            long latency = (long) pinginfo[1];
            long when = (long) pinginfo[2];
            if (pinginfo.length == 4) {
                IOException e = (IOException) pinginfo[3];
                System.out.println(e);
            } else if (!reached) {
                System.out.println(simpleDateFormat.format(when) + " Did not reach " + address);
            } else {
                System.out.println(simpleDateFormat.format(when) + " Reply from " + address + ": time=" + latency + "ms");
            }
            pingLogs.add(pinginfo);
            long loopTime = (System.currentTimeMillis() - whileStart);
            //System.out.println(", looptime="+loopTime);
            long timeToSleep = waitBetweenPings - loopTime;
            timeToSleep = timeToSleep < 0 ? 0 : timeToSleep;
            Thread.sleep(timeToSleep);
        }

        while (true) {
            //wait for daemon to finish
            Thread.sleep(1);
        }
    }

    private static SignalHandler createSignalHandler() {
        return signal -> {
            continueRunningWhile = false;
            System.out.println();
            System.out.println("Analyzing...");
            long timeNow = System.currentTimeMillis();

            long soucetVsehoLatencyReached = 0;
            long numberOfReached = 0;
            long numberOfNotReachedNoError = 0;
            long numberOfNotReachedError = 0;
            long minPing = 999999;
            long maxPing = -999999;
            int disconnectInARowCounter = 0;
            ArrayList<Long> disconnectTimeInfo = new ArrayList<>(1000);

            for (Object[] pinginfo : pingLogs) {
                boolean reached = (boolean) pinginfo[0];
                long latency = (long) pinginfo[1];
                long startTime = (long) pinginfo[2];

                //average, min, max
                if (reached) {
                    soucetVsehoLatencyReached += latency;
                    numberOfReached++;
                    if (latency < minPing) {
                        minPing = latency;
                    }
                    if (latency > maxPing) {
                        maxPing = latency;
                    }
                    disconnectInARowCounter = 0;
                }
                if (!reached && pinginfo.length == 4) {
                    //error
                    numberOfNotReachedError++;
                    disconnectInARowCounter = 0;
                } else if (!reached && pinginfo.length == 3) {
                    //no error
                    numberOfNotReachedNoError++;
                    disconnectInARowCounter++;

                }
                if (disconnectInARowCounter == 3) {
                    disconnectTimeInfo.add(startTime);
                }
            }
            long avg = (long) (soucetVsehoLatencyReached / (double) numberOfReached);

            System.out.println("Ping started at " + simpleDateFormat.format(startTimeOfPinging) + ", running for " + (long) ((timeNow - startTimeOfPinging) / 60000d) + " minutes");
            System.out.println("Sent     = " + (numberOfReached + numberOfNotReachedNoError) + " (all=" + (pingLogs.size()) + ")");
            System.out.println("Received = " + numberOfReached);
            System.out.println("Lost     = " + numberOfNotReachedNoError);
            System.out.println("Errors   = " + numberOfNotReachedError);
            System.out.println("Average  = " + avg + ", Max = " + maxPing + ", Min = " + minPing);
            System.out.println();
            if (disconnectTimeInfo.size() > 0) {
                System.out.println("Disconnected at least 3 in a row = " + disconnectTimeInfo.size());
                for (long timeOfDisconnect : disconnectTimeInfo) {
                    long howLongAgoMs = timeNow - timeOfDisconnect;
                    long howLongAgoMins = (long) ((howLongAgoMs / 1000d) / 60d);

                    if(howLongAgoMins<=1){
                        long howLongAgoSec = (long) ((howLongAgoMs / 1000d));
                        System.out.println("Disconnected " + howLongAgoSec + " seconds ago (" + simpleDateFormat.format(timeOfDisconnect) + ")");
                    }else{
                        System.out.println("Disconnected " + howLongAgoMins + " minutes ago (" + simpleDateFormat.format(timeOfDisconnect) + ")");
                    }
                }
                Long lastDisconnectTimeLog = disconnectTimeInfo.get(disconnectTimeInfo.size() - 1);
                try {
                    Files.write(lastDisconnectFile.toPath(), (lastDisconnectTimeLog+"").getBytes(), StandardOpenOption.TRUNCATE_EXISTING );
                } catch (IOException e) {
                    System.out.println("Unable to save date to the file '"+lastDisconnectFile.toPath()+"': "+e);
                }
            } else {
                if(lastDisconnectedFromFile!=-1){
                    long howLongAgoMs = timeNow - lastDisconnectedFromFile;
                    long howLongAgoMins = (long) ((howLongAgoMs / 1000d) / 60d);
                    if(howLongAgoMins<=1){
                        long howLongAgoSec = (long) ((howLongAgoMs / 1000d));
                        System.out.println("Last known disconnect was "+howLongAgoSec+ " seconds ago (" + simpleDateFormatWithDate.format(lastDisconnectedFromFile) + ")");
                    }else{
                        System.out.println("Last known disconnect was "+howLongAgoMins+ " minutes ago (" + simpleDateFormatWithDate.format(lastDisconnectedFromFile) + ")");

                    }
                    System.out.println();
                }
                System.out.println("No recorded disconnections");
            }
            System.out.println();
            System.exit(0);
        };
    }



}
