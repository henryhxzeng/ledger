package com.ledger.dto;

import com.ledger.common.AssetType;
import com.ledger.common.ValueOfEnum;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletInfo {
	@NotEmpty(message = "walletName is mandatory")
	private String walletName;
	@NotEmpty(message = "type is mandatory")
	@ValueOfEnum(enumClass = AssetType.class)
	private String type;
}
