package com.ledger.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalanceUpdateEvent extends Event {
	private String EntityId;
	private String walletId;
	private double amount;
}
