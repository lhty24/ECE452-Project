package com.morpheme.palmpiano.midi;

import com.morpheme.palmpiano.EventListener;

import java.util.List;

public interface MidiNotePlayback extends EventListener, Runnable {
    void playbackMidi();
    void setMidiNotes(List<Note> midiNoteEvents);
}
