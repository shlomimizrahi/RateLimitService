package com.ratelimitservice.ratelimitservice;

import org.springframework.stereotype.Service;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ValidationService {

    private final ExecutorService validationExec;
    private final static String URL = "url";

    public ValidationService() {
        validationExec = Executors.newSingleThreadExecutor();
    }

    public RequestData _validateAndPrepare(final Map<String, Object> payload, final long currentTimeMillis) throws Exception {

        // Check if received value is not empty and is a valid string
        final String url;
        try {
            url = (String) payload.get(URL);
        } catch (final Exception e){
            throw new Exception("Input must be a String representing a URL");
        }

        // Check if received value is of URL form
        try {
            new URL(url);
        } catch (MalformedURLException e) {

            throw new Exception(("Invalid URL Format"));
        }

        return new RequestData(url.hashCode(), new Date(currentTimeMillis), url);
    }

    public CompletableFuture<RequestData> validateAndPrepare(final Map<String, Object> payload, final long currentTimeMillis){

        return CompletableFuture.supplyAsync(() -> {
            try {
                return _validateAndPrepare(payload, currentTimeMillis);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, validationExec);

    }
}