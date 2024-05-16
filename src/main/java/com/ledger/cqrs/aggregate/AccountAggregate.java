package com.ledger.cqrs.aggregate;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ledger.common.EventStatus;
import com.ledger.common.Status;
import com.ledger.cqrs.command.FundInCommand;
import com.ledger.cqrs.command.FundOutCommand;
import com.ledger.cqrs.command.MoveAssetCommand;
import com.ledger.cqrs.command.OpenAccountCommand;
import com.ledger.cqrs.command.UpdateAccountStatusCommand;
import com.ledger.cqrs.repository.AccountReadRepository;
import com.ledger.domain.Account;
import com.ledger.domain.Wallet;
import com.ledger.eventsourcing.event.AccountOpenedEvent;
import com.ledger.eventsourcing.event.UpdateAccountStatusEvent;
import com.ledger.eventsourcing.event.WalletBalanceUpdateEvent;
import com.ledger.eventsourcing.repository.EventStore;

@Service
public class AccountAggregate {

    private EventStore eventRepository;
    private AccountReadRepository readRepository;

    public AccountAggregate(EventStore repository, AccountReadRepository readRepository) {
        this.eventRepository = repository;
        this.readRepository = readRepository;
    }

    public void handleOpenAccountCommand(OpenAccountCommand command) {
    	AccountOpenedEvent event = new AccountOpenedEvent(command.getEntityId(), command.getEntityName(), command.getAccount());
    	event.setEStatus(EventStatus.PENDING);
    	eventRepository.addEvent(command.getEntityId(), event);
    }
    
    public void handleUpdateAccountStatusCommand(UpdateAccountStatusCommand command) {
    	Account account = readRepository.getAccount(command.getId());
    	if (account.getStatus() == command.getStatus()) 
    		throw new IllegalStateException ("update account status declined, need to update status from one to another");
    	UpdateAccountStatusEvent event = new UpdateAccountStatusEvent(account.getEntityId(), command.getId(), command.getStatus());
    	event.setEStatus(EventStatus.PENDING);
    	eventRepository.addEvent(account.getEntityId(), event);
    }
    
    public void handleFundInCommand(List<FundInCommand> commands) {
    	validateFundInCommand(commands);
    	for (FundInCommand command: commands) {
	    	String entityId = readRepository.getEntityIdByWalletId(command.getWalletId());
	    	WalletBalanceUpdateEvent event = new WalletBalanceUpdateEvent(entityId, command.getWalletId(), command.getAmount());
	    	event.setEStatus(EventStatus.PENDING);
	    	eventRepository.addEvent(entityId, event);
    	}
    }
    
    public void handleFundOutCommand(List<FundOutCommand> commands) {
    	validateFundOutCommand(commands);
    	for (FundOutCommand command: commands) {
	    	String entityId = readRepository.getEntityIdByWalletId(command.getWalletId());
	    	
	    	WalletBalanceUpdateEvent event = new WalletBalanceUpdateEvent(entityId, command.getWalletId(), -command.getAmount());
	    	event.setEStatus(EventStatus.PENDING);
	    	eventRepository.addEvent(entityId, event);
    	}
    }

    public void handleMoveAssetCommand(List<MoveAssetCommand> commands) {
    	validateMoveAssetCommand(commands);
    	for (MoveAssetCommand command: commands) {
	    	String entityId = readRepository.getEntityIdByWalletId(command.getFromWalletId());
			WalletBalanceUpdateEvent moveOutEvent = new WalletBalanceUpdateEvent(entityId, command.getFromWalletId(), -command.getAmount());
			moveOutEvent.setEStatus(EventStatus.PENDING);
			eventRepository.addEvent(entityId, moveOutEvent);
			WalletBalanceUpdateEvent moveInEvent = new WalletBalanceUpdateEvent(entityId, command.getToWalletId(), command.getAmount());
			moveInEvent.setEStatus(EventStatus.PENDING);
			eventRepository.addEvent(entityId, moveInEvent);
    	}
    }
    
    public void validateFundInCommand (List<FundInCommand> commands) {
    	Set<String> entityIds = new HashSet<>();
    	for (FundInCommand command: commands) {
    		String accountId = readRepository.getAccountIdByWalletId(command.getWalletId());
    		String entityId = readRepository.getEntityIdByAccountId(accountId);
    		entityIds.add(entityId);
    		validateAccountIsNotClosed(accountId, command.getWalletId());
    	}
    	validateMovementInOneEntity(entityIds);
    }
    
    public void validateFundOutCommand(List<FundOutCommand> commands) {
    	Set<String> entityIds = new HashSet<>();

    	Map<String, Double> balanceUpdateList = new HashMap<>();
    	for (FundOutCommand command: commands) {
    		String accountId = readRepository.getAccountIdByWalletId(command.getWalletId());
    		String entityId = readRepository.getEntityIdByAccountId(accountId);
    		entityIds.add(entityId);
    		validateAccountIsNotClosed(accountId, command.getWalletId());
    		Double delta = balanceUpdateList.get(command.getWalletId());
    		if (delta != null) {
    			delta = delta.doubleValue() + command.getAmount();
    		} else {
    			delta = command.getAmount();
    		}
    		balanceUpdateList.put(command.getWalletId(), delta);
    	}
    	validateMovementInOneEntity(entityIds);
    	
    	for (var entry : balanceUpdateList.entrySet()) {
    	    Wallet wallet = readRepository.getWallet(entry.getKey());
    	    if (wallet.getBalance() - entry.getValue().doubleValue() < 0) {
    	    	var errorMessage = MessageFormat.format("insufficient balance for wallet id {0}", entry.getKey());
    	    	throw new IllegalStateException (errorMessage);
    	    }
    	}
    }
    
    public void validateMoveAssetCommand(List<MoveAssetCommand> commands) {
    	Set<String> entityIds = new HashSet<>();

    	Map<String, Double> balanceUpdateList = new HashMap<>();
    	for (MoveAssetCommand command: commands) {
    		String fromAccountId = readRepository.getAccountIdByWalletId(command.getFromWalletId());
    		String toAccountId = readRepository.getAccountIdByWalletId(command.getToWalletId());
    		String fromEntityId = readRepository.getEntityIdByAccountId(fromAccountId);
    		String toEntityId = readRepository.getEntityIdByAccountId(toAccountId);
    		entityIds.add(fromEntityId);
    		entityIds.add(toEntityId);
    		validateAccountIsNotClosed(fromAccountId, command.getFromWalletId());
    		validateAccountIsNotClosed(toAccountId, command.getToWalletId());
    		
    		Double fromDelta = balanceUpdateList.get(command.getFromWalletId());
    		if (fromDelta != null) {
    			fromDelta = fromDelta.doubleValue() - command.getAmount();
    		} else {
    			fromDelta = -command.getAmount();
    		}
    		Double toDelta = balanceUpdateList.get(command.getToWalletId());
    		if (toDelta != null) {
    			toDelta = toDelta.doubleValue() + command.getAmount();
    		} else {
    			toDelta = command.getAmount();
    		}
    		balanceUpdateList.put(command.getFromWalletId(), fromDelta);
    		balanceUpdateList.put(command.getToWalletId(), toDelta);
    	}
    	
    	validateMovementInOneEntity(entityIds);
    	
    	for (var entry : balanceUpdateList.entrySet()) {
    	    Wallet wallet = readRepository.getWallet(entry.getKey());
    	    if (wallet.getBalance() + entry.getValue().doubleValue() < 0) {
    	    	var errorMessage = MessageFormat.format("insufficient balance for wallet id {0}", entry.getKey());
    	    	throw new IllegalStateException (errorMessage);
    	    }
    	}
    }
    
    private void validateAccountIsNotClosed(String accountId, String walletId) {
    	if (readRepository.getAccount(accountId).getStatus() == Status.CLOSED) {
			var errorMessage = MessageFormat.format("The account {0} of wallet id {1} is closed", accountId, walletId);
			throw new IllegalStateException (errorMessage);
		}
    }
    
    private void validateMovementInOneEntity(Set<String> entityIds) {
    	if (entityIds.size() > 1) 
    		throw new IllegalStateException ("all movements shall be in the same entity");
    }
}
