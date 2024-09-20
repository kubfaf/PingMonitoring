import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class UseCMD {


    public static void main(String[] args) throws IOException {
        String address;
        InetAddress inet;
        String hostToPing = "www.google.com";

        try {
            System.out.println("Getting IP of " + hostToPing);
            address = InetAddress.getByName(hostToPing).getHostAddress();
            inet = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Failed. Check your internet connection. Error: "+e);
            System.exit(1);
            return;
        }

        System.out.println(inet.getHostAddress());

        if(true)return;



        List<String> commands = new ArrayList<String>();
        commands.add("ping");
        commands.add("-n");
        commands.add("1");
        commands.add("google.com");

        ProcessBuilder pb = new ProcessBuilder(commands);
        Process process = pb.start();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s = "";
        while ((s = stdInput.readLine()) != null)
        {
            System.out.println(s);
        }

    }

    public static Object[] doOnePingCMD(InetAddress inet, int timeOutMs) {
        boolean reachable;
        long result = -1;
        long start = System.currentTimeMillis();
        try {
            reachable = inet.isReachable(timeOutMs);
        } catch (IOException e) {
            return new Object[]{false, -1L, start, e};
        }
        long end = System.currentTimeMillis();
        if (reachable) {
            result = end - start;
        }
        return new Object[]{reachable, result, start};
    }

}
