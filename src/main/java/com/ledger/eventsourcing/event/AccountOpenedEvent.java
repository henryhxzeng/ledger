package com.ledger.eventsourcing.event;

import java.util.List;

import com.ledger.domain.Account;
import com.ledger.dto.AccountInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountOpenedEvent extends Event {
	private String entityId;
    private String entityName;
    private List<AccountInfo> account;
}
