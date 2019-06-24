package com.morpheme.palmpiano;

import com.pdrogfer.mididroid.event.MidiEvent;

public class MidiNoteEvent {

    private MidiEvent midiEvent;
    private long timestamp;

    public MidiNoteEvent(MidiEvent event, long timestamp) {
        this.midiEvent = event;
        this.timestamp = timestamp;
    }

    public MidiEvent getMidiEvent() {
        return midiEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
