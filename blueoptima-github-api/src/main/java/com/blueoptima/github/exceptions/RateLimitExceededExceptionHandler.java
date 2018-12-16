package com.blueoptima.github.exceptions;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler class for RateLimitExceededException
 * @author MohsinM
 *
 */
@ControllerAdvice
public class RateLimitExceededExceptionHandler {

	@ExceptionHandler(RateLimitExceededException.class)
	  public final ResponseEntity<RateLimitExceeded> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
		RateLimitExceeded errorDetails = new RateLimitExceeded(ex.getMessage(), new Date());
	    return new ResponseEntity<>(errorDetails, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
	  }
}
