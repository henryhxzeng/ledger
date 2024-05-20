package com.ledger.cqrs.command;

import java.util.List;

import com.ledger.dto.FundOut;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundOutCommand {
	@NotEmpty(message = "requests is mandatory")
	@Valid
	private List<FundOut> requests;
}
