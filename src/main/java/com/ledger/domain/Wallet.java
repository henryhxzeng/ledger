package com.ledger.domain;

import com.ledger.common.AssetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

	private String walletId;
	private String accountId;
	private String walletName;
	private AssetType type;
	private double balance;
}
