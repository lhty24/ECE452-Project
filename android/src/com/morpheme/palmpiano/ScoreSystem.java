package com.morpheme.palmpiano;

import com.morpheme.palmpiano.midi.Note;
import com.morpheme.palmpiano.util.Constants;
import com.morpheme.palmpiano.util.TimerData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ScoreSystem implements EventListener, Runnable {
    public static final int EASY_DIFFICULTY = 60;
    public static final int MEDIUM_DIFFICULTY = 40;
    public static final int HARD_DIFFICULTY = 20;

    private HashSet<Event.EventType> monitoredEvents;
    private boolean isRunning;

    public static int difficulty;
    public static long difficultyTime;
    private long numerator;
    private long denominator;
    private List<Note> onGameNotes;
    private List<Note> offGameNotes;

    private Semaphore onGameNotesMutex;
    private Semaphore offGameNotesMutex;

    public ScoreSystem () {
        isRunning = false;
        this.difficulty = MEDIUM_DIFFICULTY;
        this.difficultyTime = (long) (difficulty / RhythmBox.getVelocity() * 1000000000L);
        this.numerator = 1;
        this.denominator = 1;
        onGameNotes = new ArrayList<>();
        offGameNotes = new ArrayList<>();

        onGameNotesMutex = new Semaphore(1);
        offGameNotesMutex = new Semaphore(1);

        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.BACK);
        monitoredEvents.add(Event.EventType.PAUSE);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        monitoredEvents.add(Event.EventType.MIDI_DATA_GAMEPLAY);
        monitoredEvents.add(Event.EventType.EXPIRED_NOTE);
        monitoredEvents.add(Event.EventType.END_OF_SONG);
    }

    public static void setDifficulty(int difficulty) {
        ScoreSystem.difficulty = difficulty;
        ScoreSystem.difficultyTime = (long) (difficulty / RhythmBox.getVelocity() * 1000000000L);
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

        EventBus.getInstance().dispatch(new Event<>(Event.EventType.NEW_TIMER, new TimerData(noteOn, RhythmBox.getDelay() + difficultyTime)));
        EventBus.getInstance().dispatch(new Event<>(Event.EventType.NEW_TIMER, new TimerData(noteOff, RhythmBox.getDelay() + difficultyTime + duration)));
    }

    private void checkNotePressed(byte midiNote, long timestamp, boolean isKeyDown) {
        List<Note> gameNotesList;
        Semaphore gameNotesMutex;
        if (isKeyDown) {
            gameNotesList = onGameNotes;
            gameNotesMutex = onGameNotesMutex;
        }
        else {
            gameNotesList = offGameNotes;
            gameNotesMutex = offGameNotesMutex;
        }
        try {
            boolean foundNote = false;
            gameNotesMutex.acquire();
            for (Note note : gameNotesList) {
                if ((int) midiNote == note.getNoteValue()) {
                    long midline = note.getTimestamp() - difficultyTime;
                    long difference = Math.abs(midline - timestamp);
                    if (difference <= difficultyTime) {
                        // Accuracy is at least 50% if within difficultyTime
                        numerator += 2;
                        denominator += 2;
                        System.out.println("numerator: " + numerator + ", denominator: " + denominator);
                        EventBus.getInstance().dispatch(new Event<>(Event.EventType.CANCEL_TIMER, note));
                        gameNotesList.remove(note);
                        foundNote = true;
                    }
                    break;
                }
            }
            gameNotesMutex.release();
            if (!foundNote) {
                numerator += 0;
                denominator += 2;
                System.out.println("numerator: " + numerator + ", denominator: " + denominator);

                EventBus.getInstance().dispatch(new Event<>(Event.EventType.FAIL_NOTE, midiNote));
            }
            EventBus.getInstance().dispatch(new Event<>(Event.EventType.UPDATE_SCORE, calculateScore()));
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    private void checkNoteExpired(Note note) {
        try {
            onGameNotesMutex.acquire();
            for (Note n : onGameNotes) {
                if (n == note) {
                    EventBus.getInstance().dispatch(new Event<>(Event.EventType.FAIL_NOTE, (byte) note.getNoteValue()));
                    numerator += 0;
                    denominator += 2;
                    System.out.println("numerator: " + numerator + ", denominator: " + denominator);
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
                    numerator += 0;
                    denominator += 2;

                    System.out.println("numerator: " + numerator + ", denominator: " + denominator);
                    offGameNotes.remove(note);
                    break;
                }
            }
            offGameNotesMutex.release();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }

        EventBus.getInstance().dispatch(new Event<>(Event.EventType.UPDATE_SCORE, calculateScore()));
    }

    private void clearNotes() {
        try {
            onGameNotesMutex.acquire();
            onGameNotes.clear();
            onGameNotesMutex.release();
            offGameNotesMutex.acquire();
            offGameNotes.clear();
            offGameNotesMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    private float calculateScore() {
        if (numerator == 1 && denominator == 1) {
            return 0.0F;
        }
        return (float) (numerator - 1) / (float) (denominator - 1);
    }

    @Override
    public void handleEvent(Event<?> event) {
        byte[] data;

        switch (event.getEventType()) {
            case NEW_MIDI_FILE:
                if (ModeTracker.getMode() == Constants.PianoMode.MODE_GAME) {
                    numerator = 1;
                    denominator = 1;
                }
                break;
            case BACK:
                isRunning = false;
                clearNotes();
                break;
            case PAUSE:
                isRunning = false;
                break;
            case MIDI_FILE_PLAY:
                isRunning = true;
                break;
            case MIDI_FILE_PAUSE:
                isRunning = false;
                break;
            case PIANO_KEY_DOWN:
                if (isRunning) {
                    checkNotePressed((Byte) event.getData(), event.getTimestamp(), true);
                }
                break;
            case PIANO_KEY_UP:
                if (isRunning) {
                    checkNotePressed((Byte) event.getData(), event.getTimestamp(), false);
                }
                break;
            case MIDI_DATA_GAMEPLAY:
                if (isRunning) {
                    data = (byte[]) event.getData();
                    if ((((int) data[0]) & ((int) Constants.MIDI_NOTE_ON)) != 0 && data[2] != 0) {
                        byte[] lengthBytes = new byte[data.length - 3];
                        for (int i = 3; i < data.length; i++) {
                            lengthBytes[i - 3] = data[i];
                        }
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.put(lengthBytes);
                        buffer.flip();

                        storeGameNotes(data[1], event.getTimestamp(), buffer.getLong());
                    }
                }
                break;
            case EXPIRED_NOTE:
                checkNoteExpired((Note) event.getData());
                break;
            case END_OF_SONG:
                new Thread(this).start();
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
        boolean sentFinalScore = false;
        while (!sentFinalScore) {
            try {
                Thread.sleep(1000);
                onGameNotesMutex.acquire();
                offGameNotesMutex.acquire();
//                for (Note note : onGameNotes) {
//                    System.out.println("(" + System.nanoTime() + ") Found onNote: " + note.getNoteValue() + " @ time " + note.getTimestamp());
////                        long difference = System.nanoTime() - note.getTimestamp();
//                }
//                for (Note note : offGameNotes) {
//                    System.out.println("(" + System.nanoTime() + ") Found offNote: " + note.getNoteValue() + " @ time " + note.getTimestamp());
//                }
                if (onGameNotes.isEmpty() && offGameNotes.isEmpty()) {
//                    System.out.println("FINAL SCORE: " + calculateScore());
                    EventBus.getInstance().dispatch(new Event<>(Event.EventType.FINAL_SCORE, calculateScore()));
                    numerator = 1;
                    denominator = 1;
                    sentFinalScore = true;
                }
            } catch (InterruptedException e) {
                System.err.println(e.toString());
            } finally {
                offGameNotesMutex.release();
                onGameNotesMutex.release();
            }
        }
    }
}
