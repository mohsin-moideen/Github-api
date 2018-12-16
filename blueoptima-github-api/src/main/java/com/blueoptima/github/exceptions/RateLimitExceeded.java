package com.blueoptima.github.exceptions;

import java.util.Date;

/**
 * Return object details for RateLimitExceededException handler
 * @author MohsinM
 *
 */
public class RateLimitExceeded {
	
	private final String exception = "RateLimit exceeded exception";
	private String message;
	private Date timestamp;
	
	public String getException() {
		return exception;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public RateLimitExceeded(String message, Date timestamp) {
		super();
		this.message = message;
		this.timestamp = timestamp;
	}
}
