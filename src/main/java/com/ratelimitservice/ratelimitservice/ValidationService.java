package com.ratelimitservice.ratelimitservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ValidationService {

    private final static Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final ExecutorService validationExec;

    public ValidationService() {
        validationExec = Executors.newSingleThreadExecutor();
    }

    /**
     * @param url the url given
     * @param currentTimeMillis time in milliseconds
     * @return a CompletableFuture with the parsed, correct, ready to process data.
     */
    public CompletableFuture<RequestData> validateAndPrepare(final String url, final long currentTimeMillis){

        return CompletableFuture.supplyAsync(() -> {
            try {
                return _validateAndPrepare(url, currentTimeMillis);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, validationExec);
    }

    // This function is private and contains the logic for validating input & parsing it and any additional possible
    // computation before returning a well-formed data for further work.
    private RequestData _validateAndPrepare(final String url, final long currentTimeMillis) throws Exception {

        // Check if received value is of URL form
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            final String err = "Invalid URL format";
            logger.error(err);
            throw new Exception(err);
        }

        return new RequestData(url.hashCode(), new Date(currentTimeMillis), url, currentTimeMillis);
    }
}