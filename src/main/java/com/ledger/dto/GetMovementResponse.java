package com.ledger.dto;

import java.util.List;

import com.ledger.eventsourcing.event.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GetMovementResponse extends BaseResponse{

	private List<Event> movements;
	
    public GetMovementResponse(String message) {
        super(message);
    }
}
