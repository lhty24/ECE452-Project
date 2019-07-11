package com.morpheme.palmpiano.midi;

import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;

public class Note {
    public static final int NOTE_ON = 1;
    public static final int NOTE_OFF = 2;

    private MidiEvent midiEvent;
    private long timestamp;
    private long nsPerTick;
    private long lengthTicks;
    private long lengthNs;
    private int track;
    private int BPM;
    private int PPQ;
    private int velocity;
    private int type;
    private int note;
    private long tick;

    public Note(int track, int note, int velocity, long tick, long lengthNs) {
        this.track = track;
        this.note = note;
        this.velocity = velocity;
        this.tick = tick;
        this.lengthNs = lengthNs;
    }

    public Note(MidiEvent event, int track, int BPM, int PPQ) {
        this.midiEvent = event;
        this.track = track;
        this.BPM = BPM;
        this.PPQ = PPQ;
        this.nsPerTick = 60000000000L / (this.PPQ * this.BPM);
        this.timestamp = event.getTick() * nsPerTick;
        this.lengthTicks = 0;
        this.lengthNs = 0;
        this.tick = event.getTick();

        if (event instanceof NoteOn) {
            NoteOn on = (NoteOn) event;
            this.note = on.getNoteValue();
            this.velocity = on.getVelocity();
            this.type = this.velocity == 0 ? Note.NOTE_OFF : Note.NOTE_ON;
        }

        if (event instanceof NoteOff) {
            NoteOff off = (NoteOff) event;
            this.note = off.getNoteValue();
            this.velocity = off.getVelocity();
            this.type = Note.NOTE_OFF;
        }
    }

    public MidiEvent getMidiEvent() {
        return midiEvent;
    }

    public void setLength(long ticks) {
        this.lengthTicks = ticks;
        this.lengthNs = ticks * nsPerTick;
    }

    public long getLengthTicks() {
        return lengthTicks;
    }

    public long getLengthNs() {
        return lengthNs;
    }

    public long getTick() {
        return tick;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTrackNumber() {
        return track;
    }

    public int getNoteType() {
        return type;
    }

    public int getNoteValue() {
        return note;
    }

    public int getVelocity() {
        return velocity;
    }
}
