package com.ratelimitservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimitServiceApplication {

    private static int appArg1;
    private static long appArg2;

    /**
     * @param args App arguments (Integer: Threshold, Long: TimeInMilliseconds)
     */
    public static void main(String[] args) {
        try {
            appArg1 = Integer.parseInt(args[0]); // Parse & read threshold
            appArg2 = Long.parseLong(args[1]); // Parse & read time limit

            if (appArg1 < 1 || appArg2 < 1) {
                System.out.println("Invalid input. Must be two separate natual numbers, i.e: 10 60000");
                System.exit(0);
            }
        } catch (final Exception e) {
            System.out.println("Invalid input. Must be two separate natual numbers, i.e: 10 60000");
            System.exit(0);
        }

        SpringApplication.run(RateLimitServiceApplication.class, args);
    }

    public static int getAppArg1(){
        return appArg1;
    }

    public static long getAppArg2(){
        return appArg2;
    }
}