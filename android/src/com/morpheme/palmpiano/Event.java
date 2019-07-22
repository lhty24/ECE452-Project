package com.morpheme.palmpiano;

public class Event<D> {
    public enum EventType {
        BACK,
        PAUSE,
        RESUME,
        NEW_STAGE,
        PIANO_KEY_DOWN,
        PIANO_KEY_UP,
        MIDI_DATA_AUDIO,
        MIDI_DATA_GAMEPLAY,
        NEW_MIDI_FILE,
        MIDI_FILE_PLAY,
        MIDI_FILE_PAUSE,
        MIDI_RECORD_START,
        MIDI_RECORD_STOP,
        EXPIRED_NOTE,
        FAIL_NOTE,
        UPDATE_SCORE,
    }

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
