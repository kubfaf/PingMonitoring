public class ThreadTest {

    public static Object o = new Object();
    public static void main(String[] args) throws InterruptedException {


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("11");


                System.out.println("22");
            }
        });

        t.start();


        System.out.println("AA");

        Thread.sleep(1000);

        synchronized (o){
            Thread.currentThread().join();
        }



        System.out.println("BB");

    }
}
