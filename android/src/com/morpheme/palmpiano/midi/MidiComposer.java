package com.morpheme.palmpiano.midi;

import android.content.Context;

import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
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
    private final int VELOCITY = 100;

    private Context context;
    private MidiFile midi;
    private MidiTrack track;
    private MidiTrack tempoTrack;
    private HashSet<Event.EventType> monitoredEvents;
    private long startTime;
    private long duration;
    private long nsPerTick;

    private boolean start;

    public MidiComposer(Context context) {
        this.context = context;
        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        EventBus.getInstance().register(this);
        this.nsPerTick = 60000000000L / (PPQ * BPM);
        this.start = false;
    }

    private void start() {
        this.startTime = System.nanoTime();
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

    private void saveNote(int state, byte note) {
        if (!start) return;

        int value = (int) note;

        long now = System.nanoTime();
        long dt = now - startTime;

        duration += dt;
        startTime = now;

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
        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                this.saveNote(Note.NOTE_ON, ((Byte) event.getData()).byteValue());
                break;
            case PIANO_KEY_UP:
                this.saveNote(Note.NOTE_OFF, ((Byte) event.getData()).byteValue());
                break;
            case MIDI_FILE_PLAY:
                this.start();
                break;
            case MIDI_FILE_PAUSE:
                this.stop();
                // Test line
                MidiFileIO.writeMidiFile(context, getMidiFile(), "test_compose.mid");
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
