package com.ratelimitservice.ratelimitservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
public class RateLimitHandler {

    // TODO Are url params different keys? (service applies for domain rater than URLs?)

    /**
     * Enum class for the proposed return values.
     */
    private static class RequestBodyParams {
        @JsonProperty
        @NotNull
        private String url;
        @JsonCreator
        public RequestBodyParams(final @NotNull String url){
            this.url = url;
        }
        public @NotNull String getUrl(){
            return this.url;
        }
    }

    /**
     * Proposed return values
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
     * Instantiates a RateLimitHandler Controller
     * @param rateLimitService the RateLimitService Bean
     * @param validationService the ValidationService Bean
     */
    public RateLimitHandler(final RateLimitService rateLimitService, final ValidationService validationService) {
        this.validationService = validationService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * a handler that injects parsing / validation & business logic tasks to the respective services. returns a deferred result,
     * thus non-blocking. the RateLimitService resolves that result when, and if done successfully, this operation is delegated to the
     * spring container daemon thread, that by nature is always running.
     * @param payload the payload given in the POST method for /report URL:
     * @return RETURN_VALUE enum denoted above in this class.
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public DeferredResult<String> handleURL(@RequestBody() final RequestBodyParams payload) {

        final long currentTimeMillis = System.currentTimeMillis();
        final DeferredResult<String> output = new DeferredResult<>();

        validationService.validateAndPrepare(payload.getUrl(), currentTimeMillis).
                handle((result, ex) -> {
                    if (ex == null) {
                        rateLimitService.isRequestAllowed(result).whenComplete((res, ex2) -> {
                            final String jsonBody = res ? RETURN_VALUES.DO_NOT_BLOCK.toString() : RETURN_VALUES.BLOCK.toString();
                            output.setResult(jsonBody);
                        });
                    } else {
                        output.setResult(ex.getCause().getMessage());
                    }
                    return null;
                });
        return output;
    }}