package com.ledger.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class Entity {

	private String entityId;
	private String entityName;
	private int version;
	private List<Account> accounts = new ArrayList<>();
}
