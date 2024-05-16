package com.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse extends BaseResponse {
    private String id;
    
    public AccountResponse(String message, String id) {
        super(message);
        this.id = id;
    }
}
