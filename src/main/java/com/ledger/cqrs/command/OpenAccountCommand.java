package com.ledger.cqrs.command;

import java.util.List;

import com.ledger.domain.Account;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenAccountCommand {

	private String entityId;
	private String entityName;
	
	private List<Account> account;
}
