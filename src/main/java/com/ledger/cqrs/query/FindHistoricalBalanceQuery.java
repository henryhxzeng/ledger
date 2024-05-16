package com.ledger.cqrs.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindHistoricalBalanceQuery extends BaseQuery {

	private String datetime;
	
	public FindHistoricalBalanceQuery(String id, String datetime) {
		super(id);
		this.datetime = datetime;
	}
}
