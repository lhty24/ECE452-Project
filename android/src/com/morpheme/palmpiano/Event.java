package com.morpheme.palmpiano;

public class Event <D> {
    public enum EventType {PIANO_KEY_DOWN, PIANO_KEY_UP, MIDI_DATA}

    private long timestamp;
    private EventType eventType;
    private D data;

    public Event(EventType eventType, D data) {
        // Precision up to 1E-6 s
        this.timestamp = System.nanoTime();
        this.eventType = eventType;
        this.data = data;
    }

    /* Getters */
    public long getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public D getData() {
        return data;
    }

    @Override
    public String toString() {
        if (data != null) {
            return timestamp + " - eventType: " + eventType.toString() + " - data: " + data.toString();
        } else {
            return timestamp + " - eventType: " + eventType.toString() + " - data: null";
        }
    }
}
