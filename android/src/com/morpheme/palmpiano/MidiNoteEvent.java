package com.morpheme.palmpiano;

import com.pdrogfer.mididroid.event.MidiEvent;

public class MidiNoteEvent {

    private MidiEvent midiEvent;
    private long nsPerTick;

    public MidiNoteEvent(MidiEvent event, long nsPerTick) {
        this.midiEvent = event;
        this.nsPerTick = nsPerTick;
    }

    public MidiEvent getMidiEvent() {
        return midiEvent;
    }

    public long getTimestamp() {
        return midiEvent.getTick() * nsPerTick;
    }
}
