package com.ledger.eventsourcing.projector;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ledger.common.EventStatus;
import com.ledger.common.Status;
import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.cqrs.repository.AccountReadRepository;
import com.ledger.domain.Account;
import com.ledger.domain.Entity;
import com.ledger.domain.Wallet;
import com.ledger.eventsourcing.event.AccountOpenedEvent;
import com.ledger.eventsourcing.event.UpdateAccountStatusEvent;
import com.ledger.eventsourcing.event.WalletBalanceUpdateEvent;
import com.ledger.eventsourcing.repository.EventStore;

import jakarta.annotation.PostConstruct;

@Component
public class LedgerProjector implements Runnable{
	private final Logger logger = Logger.getLogger(LedgerProjector.class.getName());
	
	@Autowired
	private EventStore eventRepository;
	
	@Autowired
	private AccountReadRepository readRepository;
	
	@PostConstruct
	public void init() {
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while(true) {
			logger.log(Level.INFO, "Balance projector is running");
			try {
				doJob();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public void doJob() throws InterruptedException {
		try {
			while(true) {
				String aggId = eventRepository.getEventQueue().poll();
				if(aggId == null){
					Thread.yield();
					continue;
				}
	
				var events = eventRepository.getEventStore().get(aggId);
				Entity entity = null;
				try { 
					entity = readRepository.getEntity(aggId);
				} catch (AggregateNotFoundException e) {
				}
				
				int version = entity == null? 0: entity.getVersion();
				
				for(int i = version ; i < events.size(); i++){
					var event = events.get(i);
					if (event.getEStatus() == EventStatus.PENDING) {
						if (event instanceof WalletBalanceUpdateEvent)
							apply((WalletBalanceUpdateEvent) event);
						if (event instanceof AccountOpenedEvent) 
							apply((AccountOpenedEvent) event);
						if (event instanceof UpdateAccountStatusEvent)
							apply((UpdateAccountStatusEvent) event);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public void apply(WalletBalanceUpdateEvent event) {
		Entity entity = readRepository.getEntity(event.getEntityId());
		Wallet wallet = readRepository.getWallet(event.getWalletId());
    	if (wallet != null) {
    		wallet.setBalance(wallet.getBalance() + event.getAmount());
    		entity.setVersion(entity.getVersion() + 1);
    		event.setEStatus(EventStatus.CLEARED);
    		event.setDatetime(new Date());
    	}
    	logger.log(Level.INFO, MessageFormat.format("Wallet id {0} update balance by {1}.", event.getWalletId(), event.getAmount()));
	}
	
    public void apply(AccountOpenedEvent event) {
    	
    	var entity = Entity.builder().entityId(event.getEntityId()).entityName(event.getEntityName()).version(1).build();
    	List<Account> accounts = Optional.ofNullable(event.getAccount()).orElse(new ArrayList<Account>());
    	
    	for (Account eventAccount: accounts) {
    		var accountId = UUID.randomUUID().toString();
    		eventAccount.setAccountId(accountId);
    		eventAccount.setEntityId(event.getEntityId());
    		eventAccount.setStatus(Status.OPEN);
    		
    		List<Wallet> wallets = Optional.ofNullable(eventAccount.getWallets()).orElse(new ArrayList<Wallet>());
    		
    		for (Wallet eventWallet: wallets) {
    			var walletId = UUID.randomUUID().toString();
    			eventWallet.setAccountId(accountId);
    			eventWallet.setWalletId(walletId);
    			
    			readRepository.addWallet(walletId, eventWallet);
    		}
    		
    		eventAccount.setWallets(wallets);
    		readRepository.addAccount(accountId, eventAccount);
    		event.setEStatus(EventStatus.CLEARED);
    		event.setDatetime(new Date());
    	}
    	
    	entity.setAccounts(accounts);
    	readRepository.addEntity(event.getEntityId(), entity);
    	logger.log(Level.INFO, MessageFormat.format("Entity {0} is created successfuly.", event.getEntityId()));
    }
    
    public void apply(UpdateAccountStatusEvent event) {
    	Entity entity = readRepository.getEntity(event.getEntityId());
    	
    	Account account = readRepository.getAccount(event.getAccountId());
    	if (account != null) {
	    	account.setStatus(event.getStatus());
	        entity.setVersion(entity.getVersion() + 1);
	        event.setEStatus(EventStatus.CLEARED);
			event.setDatetime(new Date());
			logger.log(Level.INFO, MessageFormat.format("Account {0}: status is updated successfuly.", event.getAccountId()));
    	}
    }
}
