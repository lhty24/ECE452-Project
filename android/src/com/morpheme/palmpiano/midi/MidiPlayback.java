package com.morpheme.palmpiano.midi;

import android.content.Context;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.ModeTracker;
import com.morpheme.palmpiano.util.Constants;
import com.pdrogfer.mididroid.MidiFile;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MidiPlayback implements MidiNotePlayback {
    public static final int BOTH_HANDS = 0;
    public static final int RIGHT_HAND = 1;
    public static final int LEFT_HAND = 2;

    private boolean isPlaying;
    private boolean isPlayingState;
    private HashSet<Event.EventType> monitoredEvents;
    private List<Note> notes;
    private int hand;

    public MidiPlayback(int hand) {
        this.hand = hand;
        this.notes = null;
        this.isPlaying = false;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.NEW_MIDI_FILE);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.monitoredEvents.add(Event.EventType.BACK);
        this.monitoredEvents.add(Event.EventType.PAUSE);
        this.monitoredEvents.add(Event.EventType.RESUME);
        checkHands();
    }

    private void checkHands() {
        if (hand == BOTH_HANDS) return;

        for (Note note : notes) {
            if (note.getTrackNumber() != hand) return;
        }

        hand = BOTH_HANDS;
    }

    @Override
    public void playbackMidi() {
        long newNow;
        long prev = System.nanoTime();
        long dt = 0;

        for(Note note : notes) {
            if (hand != BOTH_HANDS && note.getTrackNumber() != hand) continue;

            long timestamp = note.getTimestamp();

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(note.getLengthNs());

            byte[] len = buffer.array();
            byte[] noteEvent = new byte[3 + len.length];

            if (note.getNoteType() == Note.NOTE_ON) noteEvent[0] = (byte) 0x91;
            if (note.getNoteType() == Note.NOTE_OFF) noteEvent[0] = (byte) 0x81;

            noteEvent[1] = (byte) note.getNoteValue();
            noteEvent[2] = (byte) note.getVelocity();

            for(int i = 3; i < 3 + len.length; i++) {
                noteEvent[i] = len[i - 3];
            }

            while (dt <= timestamp) {
                newNow = System.nanoTime();
                while (!isPlaying) {
                    newNow = System.nanoTime();
                    prev = newNow;
                }
                dt += (newNow - prev);
                prev = newNow;
            }

            if (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_AUDIO, noteEvent));
            } else if (ModeTracker.getMode() == Constants.PianoMode.MODE_GAME) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_GAMEPLAY, noteEvent));
            }
        }
    }

    @Override
    public void setMidiNotes(String midiFileName) {
        MidiFile midiFile = MidiFileIO.getMidiFile(midiFileName);
        this.notes = MidiFileParser.getMidiEvents(midiFile);
    }

    @Override
    public void setMidiNotes(List<Note> midiNoteEvents) {
        notes = midiNoteEvents;
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case NEW_MIDI_FILE:
                setMidiNotes((String) event.getData());
                break;
            case MIDI_FILE_PLAY:
                this.isPlaying = true;
                break;
            case MIDI_FILE_PAUSE:
            case PAUSE:
                this.isPlayingState = this.isPlaying;
                this.isPlaying = false;
                break;
            case RESUME:
                this.isPlaying = this.isPlayingState;
                break;
            case BACK:
                this.isPlaying = false;
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    @Override
    public void run() {
        playbackMidi();
    }
}
