package com.morpheme.palmpiano.midi;

import android.content.Context;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.PalmPiano;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MidiPlaybackProxy implements MidiNotePlayback {
    private MidiNotePlayback actualPlayback;

    private HashSet<Event.EventType> monitoredEvents;
    private PalmPiano.PianoMode mode;
    private int hand;
    private List<Note> notes;
    private boolean isPlaying;

    private Context context;
    private String midiFileName;

    public MidiPlaybackProxy(PalmPiano.PianoMode mode, int hand, Context context, String midiFileName) {
        this(mode, hand, null);
        this.context = context;
        this.midiFileName = midiFileName;
    }

    public MidiPlaybackProxy(PalmPiano.PianoMode mode, int hand, List<Note> midiNotes) {
        // Data to store when actual object is needed
        this.mode = mode;
        this.hand = hand;
        this.notes = midiNotes;
        this.context = null;
        this.midiFileName = null;

        // Un-instantiated actual object
        this.actualPlayback = null;

        // Dummy data to return when actual object not yet instantiated
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.isPlaying = false;
    }

    @Override
    public void playbackMidi() {
        if (actualPlayback != null) {
            actualPlayback.playbackMidi();
        }
        else {
            System.out.println("Proxy playbackMidi");
            // Virtual proxy behaviour; only instantiate and run upon beginning to play
            while (!this.isPlaying) {}
            System.out.println("Proxy continuing playback");
            if (notes == null) {
                System.out.println("Proxy actual init: " + mode + ", " + hand + ", " + context + ", " + midiFileName);
                actualPlayback = new MidiPlayback(mode, hand, context, midiFileName);
            } else {
                actualPlayback = new MidiPlayback(mode, hand, notes);
            }
            actualPlayback.handleEvent(new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
            actualPlayback.playbackMidi();
        }
    }

    @Override
    public void setMidiNotes(List<Note> midiNoteEvents) {
        if (actualPlayback != null) {
            actualPlayback.setMidiNotes(midiNoteEvents);
        }
        else {
            notes = midiNoteEvents;
        }
    }

    @Override
    public void handleEvent(Event<?> event) {
        System.out.println("Proxy handleEvent: " + event.toString());
        if (actualPlayback != null) {
            actualPlayback.handleEvent(event);
        }
        else {
            switch (event.getEventType()) {
                case MIDI_FILE_PLAY:
                    this.isPlaying = true;
                    System.out.println("Proxy isPlaying=true");
                    break;
                case MIDI_FILE_PAUSE:
                    this.isPlaying = false;
                    System.out.println("Proxy isPlaying=false");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        if (actualPlayback != null) {
            return actualPlayback.getMonitoredEvents();
        }
        return monitoredEvents;
    }

    @Override
    public void run() {
        if (actualPlayback != null) {
            actualPlayback.run();
        }
        else {
            System.out.println("Proxy running...");
            playbackMidi();
        }
    }
}
