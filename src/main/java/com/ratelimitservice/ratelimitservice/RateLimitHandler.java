package com.ratelimitservice.ratelimitservice;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

@Controller
public class RateLimitHandler {

    // TODO Block if threshold < 2
    // TODO Logging
    // TODO Check if considering urlparams as different key
    // TODO Tests
    // TODO Readme
    // TODO Explaination

    /**
     * Enum class for the proposed return values.
     */
    public enum RETURN_VALUES {

        BLOCK("{\"block\": true}"),
        DO_NOT_BLOCK("{\"block\": false}");

        private final String text;

        /**
         * @param text the Enum raw value.
         */
        RETURN_VALUES(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final RateLimitService rateLimitService;
    private final ValidationService validationService;

    /**
     *
     */
    public RateLimitHandler(final RateLimitService rateLimitService, final ValidationService validationService) {
        this.validationService = validationService;
        this.rateLimitService = rateLimitService;
    }

    /**
     *
     * @param payload the payload given in the POST method for /report URL:
     * @return RETURN_VALUE enum denoted above in this class.
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public DeferredResult<String> handleURL(@RequestBody() final Map<String, Object> payload) {

        final long currentTimeMillis = System.currentTimeMillis();
        DeferredResult<String> output = new DeferredResult<>();

        // TODO CATCH EXCEPTIONS
        validationService.validateAndPrepare(payload, currentTimeMillis).
                handle((result, ex) -> {
                    rateLimitService.limitVisits(result).whenComplete((res, ex2)-> output.setResult(res));
                    return null;
                });
        return output;
    }}

/* These are the scopes of the exception(s) might be caught by the abstraction
                 layer of the thread(s), ie, the pool / executor, we can log anything here or react accordingly if we want,
                 regardless, the executor object by default will recreate the thread(s) and tasks will carry on,
                 this is another level of safety for production mode that can be of advantage at times, depending on the
                 severity, design or requirement(s), for example stopping the service, extra logging, send an email alert, etc.
                 for the exercise purposes it's kept as null ie return nothing meaningful neither executes anything */