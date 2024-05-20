package com.ledger.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfo {
	@NotEmpty(message = "accountName is mandatory")
    private String accountName;
	@NotEmpty(message = "wallet is mandatory")
	@Valid
    private List<WalletInfo> wallets = new ArrayList<>();
}
