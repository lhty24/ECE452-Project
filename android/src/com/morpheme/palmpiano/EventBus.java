package com.morpheme.palmpiano;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private static EventBus instance = null;
    private List<EventListener> subscribers;

    private EventBus() {
        this.subscribers = new ArrayList<>();
    }

    public static EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    public void register(EventListener newSubscriber) {
        subscribers.add(newSubscriber);
    }

    public void clearAll() {
        subscribers.clear();
    }

    public void dispatch(Event<?> event) {
        System.out.println("Dispatched event: " + event.toString());
        for(EventListener subscriber : subscribers) {
            if (subscriber.getMonitoredEvents().contains(event.getEventType())) {
                subscriber.handleEvent(event);
            }
        }
    }
}
