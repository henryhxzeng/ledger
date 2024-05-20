package com.ledger.cqrs.command;

import java.util.List;

import com.ledger.dto.AccountInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OpenAccountCommand {

	private String entityId;
	@NotEmpty(message = "entityName is mandatory")
	private String entityName;
	
	@NotEmpty(message = "account is mandatory")
	@Valid
	private List<AccountInfo> account;
}
