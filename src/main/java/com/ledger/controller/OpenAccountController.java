package com.ledger.controller;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.cqrs.aggregate.AccountAggregate;
import com.ledger.cqrs.command.OpenAccountCommand;
import com.ledger.dto.AccountResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/openAccount")
@RequiredArgsConstructor
public class OpenAccountController {
	private final Logger logger = Logger.getLogger(OpenAccountController.class.getName());
	private final AccountAggregate accountAggregate;
	
	@PostMapping
	public ResponseEntity<AccountResponse> openAccount(@RequestBody @Valid OpenAccountCommand command) {

		var id = UUID.randomUUID().toString();
		command.setEntityId(id);
		try {
			accountAggregate.handleOpenAccountCommand(command);
			logger.info("Account creation request returned, it is processing in async mode");
			return new ResponseEntity<>(new AccountResponse("Account creation request is processing in async mode!", id), HttpStatus.CREATED);
		} catch (Exception e) {
			var safeErrorMessage = MessageFormat.format("Error while processing request to open account for id - {0}.", id);
			logger.log(Level.SEVERE, safeErrorMessage, e);
			return new ResponseEntity<>(new AccountResponse(safeErrorMessage, id), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
