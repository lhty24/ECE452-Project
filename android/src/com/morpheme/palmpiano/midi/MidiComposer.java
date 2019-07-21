package com.morpheme.palmpiano.midi;

import android.content.Context;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventListener;
import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.MidiTrack;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;
import com.pdrogfer.mididroid.event.meta.Tempo;
import com.pdrogfer.mididroid.event.meta.TimeSignature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MidiComposer implements EventListener {
    private final int PPQ = 480;
    private final int BPM = 120;
    private final int VELOCITY = 60;

    private Context context;
    private MidiFile midi;
    private MidiTrack track;
    private MidiTrack tempoTrack;
    private HashSet<Event.EventType> monitoredEvents;
    private long startTime;
    private long pauseTime;
    private long duration;
    private long nsPerTick;

    private boolean start;

    public MidiComposer() {
        this.context = null;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        this.monitoredEvents.add(Event.EventType.BACK);
        this.monitoredEvents.add(Event.EventType.PAUSE);
        this.monitoredEvents.add(Event.EventType.RESUME);
        this.nsPerTick = 60000000000L / (PPQ * BPM);
        this.start = false;
    }

    private void start(long timestamp) {
        this.startTime = timestamp;
        this.pauseTime = 0;
        this.duration = 0;
        this.track = new MidiTrack();
        this.tempoTrack = new MidiTrack();
        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
        Tempo tempo = new Tempo();
        tempo.setBpm(BPM);
        this.tempoTrack.insertEvent(ts);
        this.tempoTrack.insertEvent(tempo);
        this.start = true;
    }

    private void stop() {
        ArrayList<MidiTrack> tracks = new ArrayList<>();
        tracks.add(tempoTrack);
        tracks.add(track);
        midi = new MidiFile(PPQ, tracks);
        this.start = false;
    }

    private void saveNote(int state, byte note, long timestamp) {
        if (!start) return;

        int value = (int) note;

        duration += timestamp - startTime - pauseTime;
        pauseTime = 0;
        startTime = timestamp;

        long tick = duration / nsPerTick;

        if (state == Note.NOTE_ON) {
            track.insertEvent(new NoteOn(tick, 0, value, VELOCITY));
        } else {
            track.insertEvent(new NoteOff(tick, 0, value, 0));
        }
    }

    public MidiFile getMidiFile() {
        return midi;
    }

    @Override
    public void handleEvent(Event<?> event) {
        long timestamp = event.getTimestamp();

        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                this.saveNote(Note.NOTE_ON, (Byte) event.getData(), timestamp);
                break;
            case PIANO_KEY_UP:
                this.saveNote(Note.NOTE_OFF, (Byte) event.getData(), timestamp);
                break;
            case MIDI_FILE_PLAY:
                this.start(timestamp);
                break;
            case MIDI_FILE_PAUSE:
                this.stop();
                // FIXME Test line
                MidiFileIO.writeMidiFile(midi, "test_compose.mid");
                break;
            case PAUSE:
                this.start = false;
                this.pauseTime = timestamp;
                break;
            case RESUME:
                this.start = true;
                this.pauseTime = timestamp - this.pauseTime;
                break;
            case BACK:
                this.start = false;
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
