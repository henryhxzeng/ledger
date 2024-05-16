package com.ledger.cqrs.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindWalletByIdQuery extends BaseQuery {
	
	public FindWalletByIdQuery(String id) {
		super(id);
	}
}
