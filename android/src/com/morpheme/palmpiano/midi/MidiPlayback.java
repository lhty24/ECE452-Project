package com.morpheme.palmpiano.midi;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.EventListener;
import com.morpheme.palmpiano.PalmPiano;
import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MidiPlayback implements EventListener, Runnable {
    public static final int BOTH_HANDS = 0;
    public static final int RIGHT_HAND = 1;
    public static final int LEFT_HAND = 2;

    private boolean isPlaying;
    private HashSet<Event.EventType> monitoredEvents;
    private PalmPiano.PianoMode pianoMode;
    private List<Note> notes;
    private int hand;

    public MidiPlayback(PalmPiano.PianoMode mode, List<Note> midiNotes, int hand) {
        this.pianoMode = mode;
        this.notes = midiNotes;
        this.hand = hand;
        this.isPlaying = false;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        EventBus.getInstance().register(this);
        checkHands();
    }

    private void checkHands() {
        if (hand == BOTH_HANDS) return;

        for (Note note : notes) {
            if (note.getTrackNumber() != hand) return;
        }

        hand = BOTH_HANDS;
    }

    public void playbackMidi(List<Note> midiNoteEvents) {
        long newNow;
        long prev = System.nanoTime();
        long dt = 0;

        for(Note event : midiNoteEvents) {
            if (hand != BOTH_HANDS && event.getTrackNumber() != hand) continue;

            MidiEvent e = event.getMidiEvent();
            long timestamp = event.getTimestamp();

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(event.getLengthNs());
            byte[] len = buffer.array();

            byte[] noteEvent = new byte[3 + len.length];

            if (e instanceof NoteOn) {
                NoteOn note = (NoteOn) e;
                noteEvent[0] = note.getVelocity() > 0 ? (byte) 0x91 : (byte) 0x81;
                noteEvent[1] = (byte) note.getNoteValue();
                noteEvent[2] = (byte) note.getVelocity();
                for(int i = 3; i < 3 + len.length; i++) {
                    noteEvent[i] = len[i - 3];
                }
            } else if (e instanceof NoteOff) {
                NoteOff note = (NoteOff) e;
                noteEvent[0] = (byte) 0x81;
                noteEvent[1] = (byte) note.getNoteValue();
                noteEvent[2] = (byte) note.getVelocity();
                for(int i = 3; i < 3 + len.length; i++) {
                    noteEvent[i] = len[i - 3];
                }
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

            if (pianoMode == PalmPiano.PianoMode.MODE_COMPOSITION) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_AUDIO, noteEvent));
            } else if (pianoMode == PalmPiano.PianoMode.MODE_GAME) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_GAMEPLAY, noteEvent));
            }
        }
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
        playbackMidi(notes);
    }
}
