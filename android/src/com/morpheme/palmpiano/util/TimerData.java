package com.morpheme.palmpiano.util;

import com.morpheme.palmpiano.midi.Note;

public class TimerData {
    private Note note;
    private long duration;

    public TimerData(Note note, long duration) {
        this.note = note;
        this.duration = duration;
    }

    public Note getNote() {
        return note;
    }

    public long getDuration() {
        return duration;
    }
}
