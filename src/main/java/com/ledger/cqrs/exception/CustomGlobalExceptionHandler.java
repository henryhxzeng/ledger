package com.ledger.cqrs.exception;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class CustomGlobalExceptionHandler{

	private final Logger logger = Logger.getLogger(CustomGlobalExceptionHandler.class.getName());
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", errorMessage);
			logger.info(safeErrorMessage);
		});
		return errors;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected Map<String, String> handleConstraintViolation(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		errors.put("message", ex.getConstraintViolations().iterator().next().getMessage());
		var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", ex.getConstraintViolations().iterator().next().getMessage());
		logger.info(safeErrorMessage);
		return errors;
	}
}