package com.morpheme.palmpiano;
import java.util.Set;

public interface EventListener {
    void handleEvent(Event<?> event);
    Set<Event.EventType> getMonitoredEvents();
}
