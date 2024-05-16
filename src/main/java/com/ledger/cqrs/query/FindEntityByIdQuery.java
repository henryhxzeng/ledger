package com.ledger.cqrs.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindEntityByIdQuery extends BaseQuery {
	
	public FindEntityByIdQuery(String id) {
		super(id);
	}
}
