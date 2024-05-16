package com.ledger.cqrs.projection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ledger.common.EventStatus;
import com.ledger.cqrs.query.FindAccountByIdQuery;
import com.ledger.cqrs.query.FindEntityByIdQuery;
import com.ledger.cqrs.query.FindHistoricalBalanceQuery;
import com.ledger.cqrs.query.FindMovementByIdQuery;
import com.ledger.cqrs.query.FindWalletByIdQuery;
import com.ledger.cqrs.repository.AccountReadRepository;
import com.ledger.domain.Account;
import com.ledger.domain.Entity;
import com.ledger.domain.Wallet;
import com.ledger.eventsourcing.event.Event;
import com.ledger.eventsourcing.event.WalletBalanceUpdateEvent;
import com.ledger.eventsourcing.repository.EventStore;

@Service
public class AccountProjection {
	
	private AccountReadRepository repository;
	private EventStore eventStore;
	
	
	public AccountProjection(AccountReadRepository repository, EventStore eventStore) {
		this.repository = repository;
		this.eventStore = eventStore;
	}
	
	
	public Entity handle(FindEntityByIdQuery query) {
		return repository.getEntity(query.getId());
	}
	
	public Account handle(FindAccountByIdQuery query) {
		return repository.getAccount(query.getId());
	}
	
	public Wallet handle(FindWalletByIdQuery query) {
		return repository.getWallet(query.getId());
	}
	
	public List<Event> handle(FindMovementByIdQuery query) {
		List<Event> movements = Optional.ofNullable(eventStore.getEvents(query.getId())).orElse(new ArrayList<Event>()).
				stream().filter(e -> e instanceof WalletBalanceUpdateEvent).collect(Collectors.toList());
		Collections.sort(movements, new Comparator<Event>() {
			public int compare(Event m1, Event m2) {
				return m2.getDatetime().compareTo(m1.getDatetime());
			}
		});
		return movements;
		
	}

	public Wallet handle(FindHistoricalBalanceQuery query) throws ParseException {
		String entityId = repository.getEntityIdByWalletId(query.getId());
		Wallet wallet = repository.getWallet(query.getId());
		List<Event> movements = Optional.ofNullable(eventStore.getEvents(entityId)).orElse(new ArrayList<Event>()).
				stream().filter(e -> e instanceof WalletBalanceUpdateEvent).collect(Collectors.toList());
		double balance = 0.0d;
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Date date = format.parse(query.getDatetime());
		
		for (Event movement: movements) {
			if (((WalletBalanceUpdateEvent) movement).getWalletId().equals(query.getId()) 
				&& movement.getEStatus() == EventStatus.CLEARED
				&& movement.getDatetime().compareTo(date) <= 0) {
				balance = balance + ((WalletBalanceUpdateEvent) movement).getAmount();
			}
		}
		
		return Wallet.builder().accountId(wallet.getAccountId()).balance(balance).walletId(wallet.getWalletId())
				.walletName(wallet.getWalletName()).type(wallet.getType()).build();
	}
	
}
