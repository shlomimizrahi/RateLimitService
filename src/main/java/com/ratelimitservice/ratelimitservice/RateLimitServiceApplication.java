package com.ratelimitservice.ratelimitservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RateLimitServiceApplication {


    private static final long[] appArgs = new long[2];

    public static void main(String[] args) {

        try {
            appArgs[0] = Integer.parseInt(args[0]); // Parse & read threshold
            appArgs[1] = Long.parseLong(args[1]); // Parse & read time limit

            if (appArgs[0] < 1 || appArgs[1] < 1) {
                System.out.println("Invalid input. Must be two separate natual numbers, i.e: 10 60000");
                System.exit(0);
            }

        } catch(final Exception e){
            System.out.println("Invalid input. Must be two separate natual numbers, i.e: 10 60000");
            System.exit(0);
        }

        SpringApplication.run(RateLimitServiceApplication.class, args);
    }

    @Bean(name = "args")
    public static long[] getExecutionArgs(){
        return appArgs;
    }
}