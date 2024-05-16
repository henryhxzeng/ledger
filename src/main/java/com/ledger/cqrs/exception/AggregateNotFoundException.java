package com.ledger.cqrs.exception;

public class AggregateNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1321255604315159785L;

	public AggregateNotFoundException(String message) {
        super(message);
    }
}
