package com.ratelimitservice.ratelimitservice;

import java.util.Date;

/**
 * Immutable, read-only for valid request data
 * @param urlHashcode the hashcode of a URL String
 * @param date date of the exact request as received in the system
 */
public record RequestData(long urlHashcode, Date date, String url, long milliseconds) {}