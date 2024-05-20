package com.ledger.controller;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.cqrs.projection.AccountProjection;
import com.ledger.cqrs.query.FindAccountByIdQuery;
import com.ledger.cqrs.query.FindEntityByIdQuery;
import com.ledger.cqrs.query.FindHistoricalBalanceQuery;
import com.ledger.cqrs.query.FindMovementByIdQuery;
import com.ledger.cqrs.query.FindWalletByIdQuery;
import com.ledger.domain.Account;
import com.ledger.domain.Entity;
import com.ledger.domain.Wallet;
import com.ledger.dto.GetAccountResponse;
import com.ledger.dto.GetEntityResponse;
import com.ledger.dto.GetMovementResponse;
import com.ledger.dto.GetWalletResponse;
import com.ledger.eventsourcing.event.Event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/query")
@RequiredArgsConstructor
@Validated
public class QueryController {

	private final Logger logger = Logger.getLogger(QueryController.class.getName());
	private final AccountProjection projection;
	@GetMapping(path = "/entity/{id}")
	public ResponseEntity<GetEntityResponse> getEntityById(@PathVariable(value = "id") 
			@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "entityId must be a UUID") String id) {
		
		try {
			Entity entity = projection.handle(new FindEntityByIdQuery(id));
			GetEntityResponse response = GetEntityResponse.builder().entity(entity).
					message("Success").build();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (AggregateNotFoundException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new GetEntityResponse(safeErrorMessage), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
            var safeErrorMessage = "Failed to complete get entity request!";
            logger.log(Level.SEVERE, safeErrorMessage, e);
            return new ResponseEntity<>(new GetEntityResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping(path = "/account/{id}")
	public ResponseEntity<GetAccountResponse> getAccountById(@PathVariable(value = "id") 
		@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "accountId must be a UUID") String id) {
		
		try {
			Account account = projection.handle(new FindAccountByIdQuery(id));
			GetAccountResponse response = GetAccountResponse.builder().account(account).
					message("Success").build();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (AggregateNotFoundException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new GetAccountResponse(safeErrorMessage), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
            var safeErrorMessage = "Failed to complete get account request!";
            logger.log(Level.SEVERE, safeErrorMessage, e);
            return new ResponseEntity<>(new GetAccountResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping(path = "/wallet/{id}")
	public ResponseEntity<GetWalletResponse> getWalletById(@PathVariable(value = "id") 
	@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "walletId must be a UUID") String id) {
		
		try {
			Wallet wallet = projection.handle(new FindWalletByIdQuery(id));
			GetWalletResponse response = GetWalletResponse.builder().wallet(wallet).
					message("Success").build();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (AggregateNotFoundException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new GetWalletResponse(safeErrorMessage), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
            var safeErrorMessage = "Failed to complete get wallet request!";
            logger.log(Level.SEVERE, safeErrorMessage, e);
            return new ResponseEntity<>(new GetWalletResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping(path = "/movement/{id}")
	public ResponseEntity<GetMovementResponse> getMovementById(@PathVariable(value = "id") 
	@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "id must be a UUID") String id) {
		try {
			List<Event> movements = projection.handle(new FindMovementByIdQuery(id));
			GetMovementResponse response = GetMovementResponse.builder().movements(movements).
					message("Success").build();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
            var safeErrorMessage = "Failed to complete get movements request!";
            logger.log(Level.SEVERE, safeErrorMessage, e);
            return new ResponseEntity<>(new GetMovementResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@PostMapping(path = "/walletHistoricalBalance")
	public ResponseEntity<GetWalletResponse> getHistoricalBalanceOfWallet(@RequestBody @Valid FindHistoricalBalanceQuery query) {
		try {
			Wallet wallet = projection.handle(query);
			GetWalletResponse response = GetWalletResponse.builder().wallet(wallet).
					message("Success").build();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (AggregateNotFoundException e) {
			var safeErrorMessage = MessageFormat.format("Client made a bad request - {0}.", e.getMessage());
			logger.info(safeErrorMessage);
			return new ResponseEntity<>(new GetWalletResponse(safeErrorMessage), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			var safeErrorMessage = "Failed to complete get historical balance of wallet request!";
            logger.log(Level.SEVERE, safeErrorMessage, e);
            return new ResponseEntity<>(new GetWalletResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
   		}
	}
	
}
