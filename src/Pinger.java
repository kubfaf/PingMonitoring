import java.io.IOException;
import java.net.InetAddress;

public class Pinger {
    static int timeOutMs = 900;
    static InetAddress inet;





    public static Object[] doOnePing(InetAddress inet, int timeOutMs) {
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


    public static void doOnePing(){
        boolean reachable;
        long result = -1;
        long start = System.currentTimeMillis();
        try {
            reachable = inet.isReachable(timeOutMs);
        } catch (IOException e) {
            return;
        }
        long end = System.currentTimeMillis();
        if(reachable){
            result = end-start;
        }

        //odevzdat vysledek

    }


}
