package com.ledger.dto;

import com.ledger.domain.Wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GetWalletResponse extends BaseResponse{

	private Wallet wallet;
	
    public GetWalletResponse(String message) {
        super(message);
    }
}
