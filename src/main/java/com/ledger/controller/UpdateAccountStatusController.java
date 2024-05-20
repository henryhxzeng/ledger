package com.ledger.controller;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.cqrs.aggregate.AccountAggregate;
import com.ledger.cqrs.command.UpdateAccountStatusCommand;
import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.dto.AccountResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/updateAccountStatus")
@RequiredArgsConstructor
public class UpdateAccountStatusController {
	private final Logger logger = Logger.getLogger(UpdateAccountStatusController.class.getName());
	private final AccountAggregate accountAggregate;
	
    @PostMapping
	public ResponseEntity<AccountResponse> updateAccountStatus(@RequestBody @Valid UpdateAccountStatusCommand command) {
		
		try {
			accountAggregate.handleUpdateAccountStatusCommand(command);
			logger.info("Account status update request returned, it is processing in async mode");
			return new ResponseEntity<>(new AccountResponse("Account status update request is processing in async mode!", command.getId()), HttpStatus.OK);
		}  catch (AggregateNotFoundException | IllegalStateException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new AccountResponse(safeErrorMessage, command.getId()), HttpStatus.BAD_REQUEST);
		}
		 catch (Exception e) {
			var safeErrorMessage = MessageFormat.format("Error while processing request to update account status for id - {0}.", command.getId());
			logger.log(Level.SEVERE, safeErrorMessage, e);
			return new ResponseEntity<>(new AccountResponse(safeErrorMessage, command.getId()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
}
