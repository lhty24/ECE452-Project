package com.morpheme.palmpiano;

import com.morpheme.palmpiano.midi.Note;
import com.morpheme.palmpiano.util.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

class NoteTimer extends TimerTask {
    private Note note;

    public NoteTimer(Note note) {
        super();
        this.note = note;
    }

    @Override
    public void run() {
        EventBus.getInstance().dispatch(new Event<>(Event.EventType.EXPIRED_NOTE, note));
    }
}

public class ScoreSystem implements EventListener {
    private HashSet<Event.EventType> monitoredEvents;
    private List<Note> onGameNotes;
    private List<Note> offGameNotes;

    private Semaphore onGameNotesMutex;
    private Semaphore offGameNotesMutex;

    public ScoreSystem () {
        onGameNotes = new ArrayList<>();
        offGameNotes = new ArrayList<>();

        onGameNotesMutex = new Semaphore(1);
        offGameNotesMutex = new Semaphore(1);

        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        monitoredEvents.add(Event.EventType.MIDI_DATA_GAMEPLAY);
        monitoredEvents.add(Event.EventType.EXPIRED_NOTE);
    }

    private void checkNoteScore(byte midiNote, long timestamp, boolean isKeyDown) {
        if (isKeyDown) {
            try {
                onGameNotesMutex.acquire();
                for (Note note : onGameNotes) {
                    if ((int) midiNote == note.getNoteValue()) {
                        if (note.getTimestamp() < timestamp) {
//                        onGameNotes
                        }
                        break;
                    }
                }
                onGameNotesMutex.release();
            }
            catch (InterruptedException e) {
                System.err.println(e.toString());
            }
        }
    }

    private void storeGameNotes(byte midiNote, long timestamp, long duration) {
        Note noteOn = new Note(midiNote, timestamp + RhythmBox.getDelay());
        Note noteOff = new Note(midiNote, timestamp + RhythmBox.getDelay() + duration);

        try {
            onGameNotesMutex.acquire();
            onGameNotes.add(noteOn);
            onGameNotesMutex.release();

            offGameNotesMutex.acquire();
            offGameNotes.add(noteOff);
            offGameNotesMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }

        Timer onTimer = new Timer();
        Timer offTimer = new Timer();

        onTimer.schedule(new NoteTimer(noteOn), RhythmBox.getDelay() / 1000000L);
        offTimer.schedule(new NoteTimer(noteOff), (RhythmBox.getDelay() + duration) / 1000000L);
    }

    private void checkNoteExpired(Note note) {
        try {
            onGameNotesMutex.acquire();
            for (Note n : onGameNotes) {
                if (n == note) {
                    EventBus.getInstance().dispatch(new Event<>(Event.EventType.FAIL_NOTE, (byte) note.getNoteValue()));
                    onGameNotes.remove(note);
                    break;
                }
            }
            onGameNotesMutex.release();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }

        try {
            offGameNotesMutex.acquire();
            for (Note n : offGameNotes) {
                if (n == note) {
                    EventBus.getInstance().dispatch(new Event<>(Event.EventType.FAIL_NOTE, (byte) note.getNoteValue()));
                    offGameNotes.remove(note);
                    break;
                }
            }
            offGameNotesMutex.release();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void handleEvent(Event<?> event) {
        byte[] data;

        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                checkNoteScore((Byte) event.getData(), event.getTimestamp(), true);
                break;
            case PIANO_KEY_UP:
                checkNoteScore((Byte) event.getData(), event.getTimestamp(), false);
                break;
            case MIDI_DATA_GAMEPLAY:
                data = (byte[]) event.getData();
                if ((((int) data[0]) & ((int) Constants.MIDI_NOTE_ON )) != 0 && data[2] != 0) {
                    byte[] lengthBytes = new byte[data.length - 3];
                    for(int i = 3; i < data.length; i++) {
                        lengthBytes[i - 3] = data[i];
                    }
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.put(lengthBytes);
                    buffer.flip();

                    storeGameNotes(data[1], event.getTimestamp(), buffer.getLong());
                }
                break;
            case EXPIRED_NOTE:
                checkNoteExpired((Note) event.getData());
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }
}
