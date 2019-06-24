package com.morpheme.palmpiano;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.EventListener;

import java.util.HashSet;
import java.util.Set;

public class RhythmBoxListener implements EventListener {
    private HashSet<Event.EventType> monitoredEvents;

    private RhythmBoxListener() {
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.MIDI_DATA);
        EventBus.getInstance().register(this);
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    @Override
    public void handleEvent(Event event) {
        System.out.println("SoundPlayer received event: " + event.toString());
        switch (event.getEventType()) {
            case MIDI_DATA:

            default:
                break;
        }
    }

}
