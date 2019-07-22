package com.morpheme.palmpiano;

import com.morpheme.palmpiano.midi.Note;
import com.morpheme.palmpiano.util.TimerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class NoteTimerTracker implements EventListener {
    private HashSet<Event.EventType> monitoredEvents;
    private List<NoteTimer> noteTimers;
    private Semaphore timerTrackerMutex;

    public NoteTimerTracker() {
        noteTimers = new ArrayList<>();
        timerTrackerMutex = new Semaphore(1);

        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.BACK);
        monitoredEvents.add(Event.EventType.PAUSE);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        monitoredEvents.add(Event.EventType.NEW_TIMER);
        monitoredEvents.add(Event.EventType.CANCEL_TIMER);
        monitoredEvents.add(Event.EventType.EXPIRED_NOTE);
    }

    public void startNewTimer(Note note, long duration) {
        try {
            timerTrackerMutex.acquire();
            NoteTimer newNoteTimer = new NoteTimer(note);
            noteTimers.add(newNoteTimer);
            newNoteTimer.startTimer(duration);
            timerTrackerMutex.release();
        }
        catch (InterruptedException e){
            System.err.println(e.toString());
        }
    }

    public void pauseAll() {
        try {
            timerTrackerMutex.acquire();
            for (NoteTimer noteTimer : noteTimers) {
                noteTimer.pause();
            }
            timerTrackerMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public void resumeAll() {
        try {
            timerTrackerMutex.acquire();
            // Cancelled TaskTimers cannot be re-scheduled; new timers must be created
            List<NoteTimer> newNoteTimers = new ArrayList<>();
            for (NoteTimer noteTimer : noteTimers) {
                NoteTimer newNoteTimer = noteTimer.resume();
                if (newNoteTimer != null) {
                    newNoteTimers.add(newNoteTimer);
                }
            }
            noteTimers.clear();
            for (NoteTimer noteTimer : newNoteTimers) {
                noteTimers.add(noteTimer);
            }
            newNoteTimers.clear();
            timerTrackerMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public void cancelNoteTimer(Note note) {
        try {
            timerTrackerMutex.acquire();
            for (NoteTimer noteTimer : noteTimers) {
                noteTimer.getTimer().cancel();
                noteTimers.remove(noteTimer);
                break;
            }
            timerTrackerMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public void cancelAll() {
        try {
            timerTrackerMutex.acquire();
            for (NoteTimer noteTimer : noteTimers) {
                noteTimer.getTimer().cancel();
            }
            noteTimers.clear();
            timerTrackerMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public void removeNoteTimer(Note note) {
        try {
            timerTrackerMutex.acquire();
            for (NoteTimer noteTimer : noteTimers) {
                if (note == noteTimer.getNote()) {
                    noteTimers.remove(note);
                    break;
                }
            }
            timerTrackerMutex.release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void handleEvent(Event<?> event) {
        TimerData timerData;
        switch (event.getEventType()) {
            case BACK:
                cancelAll();
                break;
            case PAUSE:
                pauseAll();
                break;
            case MIDI_FILE_PAUSE:
                pauseAll();
                break;
            case MIDI_FILE_PLAY:
                resumeAll();
                break;
            case NEW_TIMER:
                timerData = (TimerData) event.getData();
                startNewTimer(timerData.getNote(), timerData.getDuration());
                break;
            case CANCEL_TIMER:
                cancelNoteTimer((Note) event.getData());
                break;
            case EXPIRED_NOTE:
                removeNoteTimer((Note) event.getData());
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
