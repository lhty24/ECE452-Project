package com.morpheme.palmpiano;


import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.midi.Note;

import java.util.Timer;
import java.util.TimerTask;

public class NoteTimer extends TimerTask {
    private Timer timer;
    private long runningTimestamp;
    private long pausedTimestamp;
    private long duration;
    private long unpausedDuration;

    private Note note;

    public NoteTimer(Note note) {
        super();
        this.timer = new Timer();
        this.runningTimestamp = 0;
        this.pausedTimestamp = 0;
        this.duration = 0;
        this.unpausedDuration = 0;
        this.note = note;
    }

    public Timer getTimer() {
        return timer;
    }

    public Note getNote() {
        return note;
    }

    public void startTimer(long duration) {
        runningTimestamp = System.nanoTime();
        this.duration = duration;
        timer.schedule(this, (duration - unpausedDuration) / 1000000L);
    }

    public void pause() {
        pausedTimestamp = System.nanoTime();
        timer.cancel();
        unpausedDuration += pausedTimestamp - runningTimestamp;
    }

    public NoteTimer resume() {
        runningTimestamp = System.nanoTime();
        if (duration > unpausedDuration) {
            NoteTimer noteTimer = new NoteTimer(note);
            noteTimer.startTimer(duration - unpausedDuration);
            return noteTimer;
        }
        return null;
    }

    @Override
    public void run() {
        EventBus eventBus = EventBus.getInstance();
        eventBus.dispatch(new Event<>(Event.EventType.EXPIRED_NOTE, note));
    }
}
