package com.morpheme.palmpiano;

import com.pdrogfer.mididroid.event.MidiEvent;

public class MidiNoteEvent {

    private MidiEvent midiEvent;
    private long timestamp;
    private long nsPerTick;
    private long length;

    public MidiNoteEvent(MidiEvent event, long nsPerTick) {
        this.midiEvent = event;
        this.nsPerTick = nsPerTick;
        this.timestamp = event.getTick() * nsPerTick;
        this.length = 0;
    }

    public MidiEvent getMidiEvent() {
        return midiEvent;
    }

    public void setLength(long length) {
        this.length = length * nsPerTick;
    }

    public long getLength() {
        return length;
    }

    public long getTimestamp() {
        return timeStamp;
    }
}
