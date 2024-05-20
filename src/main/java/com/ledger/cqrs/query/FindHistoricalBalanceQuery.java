package com.ledger.cqrs.query;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindHistoricalBalanceQuery extends BaseQuery {

	@NotEmpty(message = "datetime is mandatory")
	@Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}:\\d{2}:\\d{2}[\\+]\\d{4})$", message = "datetime must be in format of yyyy-MM-dd HH:mm:ssZ. For example: 2024-05-19 13:48:00+0000")
	private String datetime;
	
	public FindHistoricalBalanceQuery(String id, String datetime) {
		super(id);
		this.datetime = datetime;
	}
}
