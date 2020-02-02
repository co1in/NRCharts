package server;

import java.util.Scanner;

public class Tester {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            Scanner reader = new Scanner(System.in);
            while(true) {
                NRChartSender.getInstance().putNumber("Velocity", reader.nextDouble());
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        NRChartSender.getInstance().init();
    }
}
