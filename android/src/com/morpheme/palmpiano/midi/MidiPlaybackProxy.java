package com.morpheme.palmpiano.midi;

import com.morpheme.palmpiano.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MidiPlaybackProxy implements MidiNotePlayback {
    private MidiNotePlayback actualPlayback;

    private HashSet<Event.EventType> monitoredEvents;
    private int hand;
    private List<Note> notes;
    private boolean isPlaying;

    private String midiFileName;

    public MidiPlaybackProxy(int hand) {
        // Data to store when actual object is needed
        this.hand = hand;
        this.notes = null;
        this.midiFileName = null;

        // Un-instantiated actual object
        this.actualPlayback = null;

        // Dummy data to return when actual object not yet instantiated
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.NEW_MIDI_FILE);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.isPlaying = false;
    }

    @Override
    public void playbackMidi() {
        if (actualPlayback != null) {
            actualPlayback.playbackMidi();
        } else {
            // Virtual proxy behaviour; only instantiate and run upon beginning to play
            while (!this.isPlaying) {}
            actualPlayback = new MidiPlayback(hand);
            if (notes != null) {
                actualPlayback.setMidiNotes(notes);
            } else {
                actualPlayback.setMidiNotes(midiFileName);
            }
            actualPlayback.handleEvent(new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
            actualPlayback.playbackMidi();
        }
    }

    @Override
    public void setMidiNotes(String midiFileName) {
        if (actualPlayback != null) {
            actualPlayback.setMidiNotes(midiFileName);
        }
        else {
            this.midiFileName = midiFileName;
        }
    }

    @Override
    public void setMidiNotes(List<Note> midiNoteEvents) {
        if (actualPlayback != null) {
            actualPlayback.setMidiNotes(midiNoteEvents);
        } else {
            notes = midiNoteEvents;
        }
    }

    @Override
    public void handleEvent(Event<?> event) {
        if (actualPlayback != null) {
            actualPlayback.handleEvent(event);
        } else {
            switch (event.getEventType()) {
                case NEW_MIDI_FILE:
                    setMidiNotes((String) event.getData());
                    break;
                case MIDI_FILE_PLAY:
                    this.isPlaying = true;
                    break;
                case MIDI_FILE_PAUSE:
                    this.isPlaying = false;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        if (actualPlayback != null) return actualPlayback.getMonitoredEvents();
        return monitoredEvents;
    }

    @Override
    public void run() {
        if (actualPlayback != null) {
            actualPlayback.run();
        } else {
            playbackMidi();
        }
    }
}
