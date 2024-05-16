package com.ledger.cqrs.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.ledger.cqrs.exception.AggregateNotFoundException;
import com.ledger.domain.Account;
import com.ledger.domain.Entity;
import com.ledger.domain.Wallet;

@Repository
public class AccountReadRepository {

	private Map<String, Entity> entityReadRepo = new ConcurrentHashMap<>();
	private Map<String, Account> accountReadRepo = new ConcurrentHashMap<>();
	private Map<String, Wallet> walletReadRepo = new ConcurrentHashMap<>();
	
	public void addEntity(String entityId, Entity entity) {
		entityReadRepo.put(entityId, entity);
	}
	
	public void addAccount(String accountId, Account account) {
		accountReadRepo.put(accountId, account);
	}
	
	public void addWallet(String walletId, Wallet wallet) {
		walletReadRepo.put(walletId, wallet);
	}
	
	public Entity getEntity(String entityId) {
		Entity entity = entityReadRepo.get(entityId);
		if (entity == null)
			throw new AggregateNotFoundException("Incorrect entityId provided");
		return entity;
	}
	
	public Account getAccount(String accountId) {
		Account account = accountReadRepo.get(accountId);
		if (account == null)
			throw new AggregateNotFoundException("Incorrect accountId provided");
		return account;
		
	}
	
	public Wallet getWallet(String walletId) {
		Wallet wallet = walletReadRepo.get(walletId);
		if (wallet == null)
			throw new AggregateNotFoundException("Incorrect walletId provided");
		return wallet;
	}
	
	public String getAccountIdByWalletId(String walletId) {
		return getWallet(walletId).getAccountId();
	}
	
	public String getEntityIdByAccountId(String accountId){
		return getAccount(accountId).getEntityId();
	}
	
	public String getEntityIdByWalletId(String walletId) {
		return getEntityIdByAccountId(getAccountIdByWalletId(walletId));
	}

}
