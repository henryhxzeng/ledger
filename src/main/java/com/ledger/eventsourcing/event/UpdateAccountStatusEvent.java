package com.ledger.eventsourcing.event;

import com.ledger.common.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusEvent extends Event {
	private String EntityId;
	private String accountId;
	private Status status;
}
