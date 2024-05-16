package com.ledger.cqrs.query;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindMovementByIdQuery extends BaseQuery {
	public FindMovementByIdQuery(String id) {
		super(id);
	}
}
