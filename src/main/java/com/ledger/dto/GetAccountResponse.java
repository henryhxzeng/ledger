package com.ledger.dto;

import com.ledger.domain.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GetAccountResponse extends BaseResponse{

	private Account account;
	
    public GetAccountResponse(String message) {
        super(message);
    }
}
