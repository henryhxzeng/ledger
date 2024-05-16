package com.ledger.cqrs.command;

import com.ledger.common.Status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAccountStatusCommand{
	
	private String id;
	private Status status;

}

