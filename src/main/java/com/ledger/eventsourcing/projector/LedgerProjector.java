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

import com.ledger.common.AssetType;
import com.ledger.common.EventStatus;
import com.ledger.common.Status;
import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.cqrs.repository.AccountReadRepository;
import com.ledger.domain.Account;
import com.ledger.domain.Entity;
import com.ledger.domain.Wallet;
import com.ledger.dto.AccountInfo;
import com.ledger.dto.WalletInfo;
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
			logger.info("Balance projector is running");
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
				logger.info("Checking aggreagateId (entityId) in the queue...");
				String aggId = eventRepository.getEventQueue().poll();
				if(aggId == null){
					logger.info("Nothing in the queue, sleep 10 seconds");
					Thread.sleep(10000);
					continue;
				}
	
				logger.info(MessageFormat.format("Processing events for entityId: {0}", aggId));
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
				logger.info(MessageFormat.format("Complete event processing for entityId: {0}", aggId));
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
    	List<AccountInfo> accountInfos = Optional.ofNullable(event.getAccount()).orElse(new ArrayList<AccountInfo>());
    	List<Account> accounts = new ArrayList<>();
    	for (AccountInfo eventAccount: accountInfos) {
    		Account account = new Account();
    		var accountId = UUID.randomUUID().toString();
    		account.setAccountId(accountId);
    		account.setEntityId(event.getEntityId());
    		account.setAccountName(eventAccount.getAccountName());
    		account.setStatus(Status.OPEN);
    		
    		List<WalletInfo> walletInfos = Optional.ofNullable(eventAccount.getWallets()).orElse(new ArrayList<WalletInfo>());
    		List<Wallet> wallets = new ArrayList<Wallet>();
    		for (WalletInfo eventWallet: walletInfos) {
    			Wallet wallet = new Wallet();
    			var walletId = UUID.randomUUID().toString();
    			wallet.setAccountId(accountId);
    			wallet.setWalletId(walletId);
    			wallet.setWalletName(eventWallet.getWalletName());
    			wallet.setType(AssetType.valueOf(eventWallet.getType()));
    			wallets.add(wallet);
    			readRepository.addWallet(walletId, wallet);
    		}
    		
    		account.setWallets(wallets);
    		accounts.add(account);
    		readRepository.addAccount(accountId, account);
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
