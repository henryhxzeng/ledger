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
import com.ledger.cqrs.command.FundInCommand;
import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.dto.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/fundIn")
@RequiredArgsConstructor
public class FundInController {
	private final Logger logger = Logger.getLogger(FundInController.class.getName());
	private final AccountAggregate accountAggregate;
	
    @PostMapping
	public ResponseEntity<BaseResponse> fundIn(@RequestBody @Valid FundInCommand command) {
		
		try {
			accountAggregate.handleFundInCommand(command);
			logger.info("Fund in request returned, it is processing in async mode");
			return new ResponseEntity<>(new BaseResponse("Fund in is processing in async mode!"), HttpStatus.OK);
		} catch (AggregateNotFoundException | IllegalStateException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new BaseResponse(safeErrorMessage), HttpStatus.BAD_REQUEST);
		} 
		catch (Exception e) {
			var safeErrorMessage = "Failed to complete fund in";
			logger.log(Level.SEVERE, safeErrorMessage, e);
			return new ResponseEntity<>(new BaseResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
