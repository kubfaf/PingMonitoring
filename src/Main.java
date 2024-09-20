

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Main {


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
        Pinger.inet = inet;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(120);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor2 = new ScheduledThreadPoolExecutor(120);
        //konec inicializace


        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                scheduledThreadPoolExecutor2.execute(new Runnable() {
                    @Override
                    public void run() {
                        Pinger.doOnePing();
                    }
                });
            }
        }, 0,1, TimeUnit.NANOSECONDS);















    }




}
