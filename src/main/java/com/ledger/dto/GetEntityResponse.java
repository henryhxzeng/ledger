package com.ledger.dto;

import com.ledger.domain.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GetEntityResponse extends BaseResponse{

	private Entity entity;
	
    public GetEntityResponse(String message) {
        super(message);
    }
}
