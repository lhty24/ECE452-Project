package com.morpheme.palmpiano.midi;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.ModeTracker;
import com.morpheme.palmpiano.PalmPiano;
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

    private boolean isThreadRunning;
    private boolean isPlaying;
    private HashSet<Event.EventType> monitoredEvents;
    private List<Note> notes;
    private int hand;

    public MidiPlayback(int hand) {
        this.hand = hand;
        this.notes = null;
        this.isThreadRunning = false;
        this.isPlaying = false;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.NEW_MIDI_FILE);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.monitoredEvents.add(Event.EventType.BACK);
        this.monitoredEvents.add(Event.EventType.PAUSE);
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

        while (notes == null) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("Exception " + e.toString());
            }
        }

        checkHands();

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
                while (!isPlaying && isThreadRunning) {
                    newNow = System.nanoTime();
                    prev = newNow;
                }
                dt += (newNow - prev);
                prev = newNow;
            }

            if (!isThreadRunning) {
                break;
            }

            if (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_AUDIO, noteEvent));
            } else if (ModeTracker.getMode() == Constants.PianoMode.MODE_GAME) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_GAMEPLAY, noteEvent));
            }
        }

        if (isThreadRunning) {
            EventBus.getInstance().dispatch(new Event<>(Event.EventType.END_OF_SONG, null));
        }
    }

    @Override
    public void setMidiNotes(String midiFileName) {
        if (midiFileName == null) {
            this.notes = null;
        }
        else {
            MidiFile midiFile = MidiFileIO.getMidiFile(midiFileName);
            this.notes = MidiFileParser.getMidiEvents(midiFile);
        }
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
                this.isPlaying = false;
                break;
            case BACK:
                this.isPlaying = false;
                this.isThreadRunning = false;
                this.notes = null;
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
        isThreadRunning = true;
//        if ((ModeTracker.getMode() == Constants.PianoMode.MODE_GAME) || (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK)) {
        if ((ModeTracker.getMode() == Constants.PianoMode.MODE_GAME)) {
            playbackMidi();
        }
        isThreadRunning = false;
    }
}
