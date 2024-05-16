package com.ledger.eventsourcing.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Repository;

import com.ledger.eventsourcing.event.Event;

@Repository
public class EventStore {

    private final Map<String, List<Event>> store = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> aggIdEvents = new ConcurrentLinkedQueue<>();

    public void addEvent(String entityId, Event event) {
    	store.compute(entityId, (x, events)->{
            if (events == null) {
                events = new ArrayList<Event>();
                events.add(event);
            } else {
                events.add(event);
            }
            return events;
        });
        aggIdEvents.add(entityId);
    }

    public List<Event> getEvents(String id) {
        return store.get(id);
    }
    
    public ConcurrentLinkedQueue<String> getEventQueue() {
        return aggIdEvents;
    }
    
    public Map<String, List<Event>> getEventStore() {
    	return store;
    }
   
}
