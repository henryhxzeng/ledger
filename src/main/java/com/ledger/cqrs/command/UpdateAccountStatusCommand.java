package com.ledger.cqrs.command;

import com.ledger.common.Status;
import com.ledger.common.ValueOfEnum;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAccountStatusCommand{
	
	@NotEmpty(message = "id is mandatory")
	@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "id must be a UUID")
	private String id;
	@NotEmpty(message = "status is mandatory")
	@ValueOfEnum(enumClass = Status.class)
	private String status;

}

