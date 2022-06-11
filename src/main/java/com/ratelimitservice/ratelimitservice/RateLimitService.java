package com.ratelimitservice.ratelimitservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    /*
    Some Constants, mainly for logging purposes.
    */

    private final static String COUNT = "count";
    private final static String BLOCKED = "blocked";
    private final static String EQUALS = " = ";
    private final static String ZERO  = "0";
    private final static String NOT_BLOCKED = "not " + BLOCKED;
    private final static String INVALID_INPUT = "NOTICE: INVALID URL INPUT";
    private final static String COMMA = ", ";
    private record URLMetaData(AtomicInteger visitCount, long time) {}
    private final static Logger logger = LoggerFactory.getLogger(RateLimitHandler.class);
    private final ConcurrentHashMap<Long, URLMetaData> urlRateCount;
    private final int threshold;
    private final long timeLimit;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public RateLimitService() {
        this.urlRateCount = new ConcurrentHashMap<>();
        this.threshold = (int) RateLimitServiceApplication.getArgs()[0];
        this.timeLimit = RateLimitServiceApplication.getArgs()[1];
    }

    private boolean _isRequestAllowed(final RequestData requestData) {

        final Date date = requestData.date();
        final long currentTimeMillis = requestData.milliseconds();
        final String url = requestData.url();
        final long hashcode = requestData.urlHashcode();

        String toLog = date + " url "  + url + " is reported, " + COUNT + EQUALS;

        try {
            new URL(url);
        } catch (final MalformedURLException e) {

            toLog += ZERO + COMMA + BLOCKED + INVALID_INPUT;
            logger.error(toLog);
            return false;
        }

        final URLMetaData urlMetaData = urlRateCount.putIfAbsent(hashcode, new URLMetaData(new AtomicInteger(1), currentTimeMillis));

        if (urlMetaData != null) {
            // A key already exists (ie, there's a record of URL, and it's state)

            if (currentTimeMillis - urlMetaData.time < timeLimit) {
                // Time hasn't passed yet
                if (urlMetaData.visitCount.get() < this.threshold) {
                    // Count value hasn't reached threshold, allow entry, increment and log.
                    toLog += urlMetaData.visitCount.incrementAndGet() + COMMA + NOT_BLOCKED;
                    logger.info(toLog);
                    return true;

                } else {
                    // Count value hit the threshold, block entry and log.
                    toLog += threshold + COMMA + BLOCKED;
                    logger.info(toLog);
                    return false;

                }
            } else {
                // Threshold time reached since last measurement taken. Reset the value, i.e reset the counter and set a new timestamp
                urlRateCount.put(hashcode, new URLMetaData(new AtomicInteger(1), currentTimeMillis));
                toLog += 1 + COMMA + NOT_BLOCKED;
                logger.info(toLog);
                return true;

            }
        }
        // Value is empty, i.e the denoted url was first seen.
        toLog += 1 + COMMA + NOT_BLOCKED;
        logger.info(toLog);
        return true;
    }

    /**
     * @param requestData the request data
     * @return true if visited, false if blocked.
     */
    public CompletableFuture<Boolean> isRequestAllowed(final RequestData requestData){

        return CompletableFuture.supplyAsync(() -> _isRequestAllowed(requestData) , executor);

    }
}