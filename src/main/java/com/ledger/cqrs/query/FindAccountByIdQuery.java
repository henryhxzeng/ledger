package com.ledger.cqrs.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindAccountByIdQuery extends BaseQuery {
	
	public FindAccountByIdQuery(String id) {
		super(id);
	}
}
