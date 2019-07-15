package com.morpheme.palmpiano.midi;

import android.content.Context;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.PalmPiano;
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
    private HashSet<Event.EventType> monitoredEvents;
    private PalmPiano.PianoMode pianoMode;
    private List<Note> notes;
    private int hand;

    public MidiPlayback(PalmPiano.PianoMode mode, int hand, Context context, String midiFileName) {
        this.pianoMode = mode;
        MidiFile midiFile = MidiFileIO.getMidiFile(context, midiFileName);
        this.notes = MidiFileParser.getMidiEvents(midiFile);
        this.hand = hand;
        this.isPlaying = false;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        checkHands();
    }

    public MidiPlayback(PalmPiano.PianoMode mode, int hand, List<Note> midiNotes) {
        this.pianoMode = mode;
        this.notes = midiNotes;
        this.hand = hand;
        this.isPlaying = false;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
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

            if (pianoMode == PalmPiano.PianoMode.MODE_PLAYBACK) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_AUDIO, noteEvent));
            } else if (pianoMode == PalmPiano.PianoMode.MODE_GAME) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_GAMEPLAY, noteEvent));
            }
        }
    }

    @Override
    public void setMidiNotes(List<Note> midiNoteEvents) {
        notes = midiNoteEvents;
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
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

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    @Override
    public void run() {
        playbackMidi();
    }
}
