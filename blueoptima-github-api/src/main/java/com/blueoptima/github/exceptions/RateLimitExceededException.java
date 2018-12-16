package com.blueoptima.github.exceptions;

/**
 * Custom exception for when rate limit has been exceeded
 * @author MohsinM
 *
 */
public class RateLimitExceededException extends RuntimeException {
	
	public RateLimitExceededException(String exception) {
	    super(exception);
	  }
}
