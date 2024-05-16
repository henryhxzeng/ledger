package com.ledger.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FundOutCommand {
	private String walletId;
	private double amount;
}
