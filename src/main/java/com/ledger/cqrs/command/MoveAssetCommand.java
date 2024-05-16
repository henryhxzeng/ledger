package com.ledger.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveAssetCommand {

	private String fromWalletId;
	private String toWalletId;
	private double amount;
}
