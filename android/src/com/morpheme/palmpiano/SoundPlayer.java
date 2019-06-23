package com.morpheme.palmpiano;
//import javax.sound.midi.*;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.billthefarmer.mididriver.MidiDriver;

import static com.morpheme.palmpiano.Event.EventType.PIANO_KEY_DOWN;

public class SoundPlayer implements EventListener {
    public enum Note {F3, G3, A3 ,B3, C4, D4, E4, F4, G4, A4, B4, C5, D5, E5, F5, G5, A5, B5, C6, D6, E6,
        FS3, GS3, AS3 ,BS3, CS4, DS4, ES4, FS4, GS4, AS4, BS4, CS5, DS5, ES5, FS5, GS5, AS5, BS5, CS6, DS6, ES6};

    private static SoundPlayer soundPlayer = null;

    private Context context = null;
//    private Synthesizer synth = null;
    private MediaPlayer mediaPlayer = null;
    private HashSet<Event.EventType> monitoredEvents;
    private MidiDriver midi = null;

    /* Constructor stuff */
    private SoundPlayer() {
        super();
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(PIANO_KEY_DOWN);
        monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        EventBus.getInstance().register(this);

        midi = new MidiDriver();
        midi.start();

        byte msg[] = new byte[2];
        // Change MIDI instrument (1 = acoustic grand piano)
        msg[0] = (byte) 0xc0;
        msg[1] = (byte) 1;
        midi.write(msg);
    }

    private SoundPlayer(Context context) {
        this();
        // Used for MediaPlayer (MIDI file playback)
        this.context = context;
    }

    public static void initialize(Context context) {
        soundPlayer = new SoundPlayer(context);
    }

    public static SoundPlayer getInstance() {
        if (soundPlayer == null) {
            soundPlayer = new SoundPlayer();
        }
        return soundPlayer;
    }

    @Override
    public void handleEvent(Event event) {
        System.out.println("SoundPlayer received event: " + event.toString());
        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                this.playNote((Note) event.getData());
                break;
            case PIANO_KEY_UP:
                this.stopNote((Note) event.getData());
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    private void playNote(Note note) {
        // m=action, n=note, v=volume
        // 0x90 = channel 1 note on, 60 = C4, 64 = mezzo-forte
        sendMidi(0x90, 60, 64);
    }

    private void stopNote(Note note) {
        // 0x80 = channel 1 note off
        sendMidi(0x80, 60, 64);
    }

    protected void sendMidi(int m, int n, int v)
    {
        byte msg[] = new byte[3];

        msg[0] = (byte) m;
        msg[1] = (byte) n;
        msg[2] = (byte) v;

        midi.write(msg);
    }
}
