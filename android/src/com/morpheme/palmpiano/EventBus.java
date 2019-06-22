package com.morpheme.palmpiano;
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private static EventBus instance = null;
    private List<EventListener> subscribers;

    private EventBus() {
        // initialize
        this.subscribers = new ArrayList<EventListener>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void register(EventListener newSubscriber) {
        subscribers.add(newSubscriber);
    }

    public void dispatch(Event<?> event) {
        System.out.println("Received " + event.getData());
        for(EventListener subscriber : subscribers) {
            if (subscriber.getMonitoredEvents().contains(event)) {
                subscriber.handleEvent(event);
            }
        }
    }
}
