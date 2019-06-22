package com.morpheme.palmpiano;
//import javax.sound.midi.*;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SoundPlayer implements EventListener {
    private static SoundPlayer soundPlayer = null;
//    private Synthesizer synth = null;
    private MediaPlayer mplayer = null;
    private HashSet<Event.EventType> monitoredEvents;

    @Override
    public void handleEvent(Event event) {
        // TODO: implement piano key event handling (up/down)
        System.out.println("SoundPlayer received event: " + event.toString());
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    public enum Note {C4, D4, E4, F4, G4, A4, B4};

    /* Constructor stuff */
    private SoundPlayer() {
//        System.out.println("test 1");
//        try {
//            System.out.println("test 2");
//            synth = MidiSystem.getSynthesizer();
//            synth.open();
//            System.out.println("test 3");
//        } catch (MidiUnavailableException e) {
//            System.err.println(e.toString());
//            synth = null;
//        } catch (Exception e) {
//            System.err.println("Unexpected: " + e.toString());
//        }
//        System.out.println("test 4");
        super();
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        EventBus.getInstance().register(this);
}

    public static SoundPlayer getInstance() {
        if (soundPlayer == null) {
            soundPlayer = new SoundPlayer();
        }
        return soundPlayer;
    }

    /* Tmp */
    public void playNote(Note note) {
        System.out.println("Currently playing note " + note.toString());
//        System.out.println("Instruments: " + synth.getAvailableInstruments());
//        mplayer = MediaPlayer.create(, Uri.fromFile(new File("Piano.mid")));
//        mplayer.start();
    }
}
