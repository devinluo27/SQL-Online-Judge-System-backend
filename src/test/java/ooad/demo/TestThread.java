package ooad.demo;

public class TestThread implements Runnable{
    private String name;
    private static  String s = "111";
    //    private static MethodSync methodSync = new MethodSync();
//    private MethodSync methodSync = new MethodSync();

    public TestThread(String name){
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(name);
        System.out.println(s);
        if (name.equals("test 1")) s = "222";

//        methodSync.method(name);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new TestThread("test 1"));
        Thread t2 = new Thread(new TestThread("test 2"));
        t1.start();
        Thread.sleep(100);
        t2.start();
    }
}