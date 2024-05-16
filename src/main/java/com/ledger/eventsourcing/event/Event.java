package com.ledger.eventsourcing.event;

import java.util.Date;
import java.util.UUID;

import com.ledger.common.EventStatus;

import lombok.Data;
import lombok.ToString;

@Data
public abstract class Event {

    public final UUID eventId = UUID.randomUUID();

    public Date datetime = new Date();
    
    private EventStatus eStatus;

}
