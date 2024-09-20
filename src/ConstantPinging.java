import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConstantPinging {


    public static void main(String[] args) throws InterruptedException {

        String address;
        InetAddress inet;
        try {
            address = InetAddress.getByName("www.google.com").getHostAddress();
            inet = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Sending Ping Request to " + address);

        while (true) {
            boolean reachable = false;
            long start = System.currentTimeMillis();
            try {
                reachable = inet.isReachable(1000);
            } catch (IOException e) {
                System.out.println("nope");
                continue;
            }
            long end = System.currentTimeMillis();
            if (reachable) {
                System.out.println(end - start);
            } else {
                System.out.println("nope");
            }

            //Thread.sleep(100);
        }


    }


}
