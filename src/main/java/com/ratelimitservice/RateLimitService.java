package com.ratelimitservice;

import com.ratelimitservice.entities.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.*;

@Service
public class RateLimitService {

    private static class URLMetaData {
        final long time;
        int visitCount;

        public URLMetaData(final long time){
            this.visitCount = 1;
            this.time = time;
        }
        public int incrementNumOfVisitsAndGet(){
            this.visitCount++;
            return visitCount;
        }
    }

    /*
    Some Constants, mainly for logging purposes.
    */
    private final static String COUNT = "count";
    private final static String BLOCKED = "blocked";
    private final static String EQUALS = " = ";
    private final static String NOT_BLOCKED = "not " + BLOCKED;
    private final static String COMMA = ", ";
    private final static Logger logger = LoggerFactory.getLogger(RateLimitHandler.class);
    private final HashMap<Long, URLMetaData> urlRateCount;
    private final int threshold;
    private final long timeLimit;
    private final Executor executor;
    private final ScheduledExecutorService cleanupOldEntriesScheduler;

    public RateLimitService() {
        this.executor = Executors.newSingleThreadExecutor();
        this.cleanupOldEntriesScheduler = Executors.newScheduledThreadPool(1);
        this.urlRateCount = new HashMap<>();
        this.threshold = (int) RateLimitServiceApplication.getArgs()[0];
        this.timeLimit = RateLimitServiceApplication.getArgs()[1];
    }

    /**
     * @param requestData the request data
     * @return true if visited, false if blocked.
     */
    public CompletableFuture<Boolean> isRequestAllowed(final RequestData requestData){
        return CompletableFuture.supplyAsync(() -> _isRequestAllowed(requestData) , executor);
    }

    // Private function to run the relevant processing of an incoming request
    private boolean _isRequestAllowed(final RequestData requestData) {
        final Date date = requestData.date();
        final long currentTimeMillis = requestData.milliseconds();
        final String url = requestData.url();
        final long hashcode = requestData.urlHashcode();

        String toLog = date + " url "  + url + " is reported, " + COUNT + EQUALS;

        final URLMetaData urlMetaData = urlRateCount.putIfAbsent(hashcode, new URLMetaData(currentTimeMillis));

        if (urlMetaData != null) {
            // A key already exists (ie, there's a record of URL, and it's state)

            if (currentTimeMillis - urlMetaData.time < timeLimit) {
                // Time hasn't passed yet, raise counter

                final int count = urlMetaData.incrementNumOfVisitsAndGet();

                if (count < this.threshold) {
                    // Count value hasn't reached threshold, allow entry, increment and log.
                    toLog += count + COMMA + NOT_BLOCKED;
                    logger.info(toLog);
                    return true;
                } else {
                    // Count value hit the threshold, block entry and log.
                    toLog += count + COMMA + BLOCKED;
                    logger.info(toLog);
                    return false;
                }
            } else {
                // Threshold time reached since last measurement taken. Reset the value, i.e reset the counter and set a new timestamp
                urlRateCount.put(hashcode, new URLMetaData(currentTimeMillis));
                toLog += 1 + COMMA + NOT_BLOCKED;
                logger.info(toLog);
                return true;
            }
        }
        // Value is empty, i.e the denoted url was first seen.

        // Set a scheduled task to remove this entry after the time threshold has reached to reduce memory usage
        cleanupOldEntriesScheduler.schedule(()-> {this.urlRateCount.remove(hashcode);}, timeLimit, TimeUnit.MILLISECONDS);

        toLog += 1 + COMMA + NOT_BLOCKED;
        logger.info(toLog);
        return true;
    }
}