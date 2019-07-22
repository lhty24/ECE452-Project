package com.morpheme.palmpiano;

import com.morpheme.palmpiano.util.Constants;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SoundPlayer implements EventListener {
    private static SoundPlayer soundPlayer = null;

    private HashSet<Event.EventType> monitoredEvents;
    private MidiDriver midi;

    private ArrayList<Byte> notes = new ArrayList<>();
    private boolean paused;

    /* Constructor stuff */
    private SoundPlayer() {
        super();

        this.monitoredEvents = new HashSet<>();
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        this.monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        this.monitoredEvents.add(Event.EventType.MIDI_DATA_AUDIO);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        this.monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        this.monitoredEvents.add(Event.EventType.BACK);
        this.monitoredEvents.add(Event.EventType.PAUSE);
        this.monitoredEvents.add(Event.EventType.RESUME);

        midi = new MidiDriver();
        midi.start();

        byte msg[] = new byte[2];
        // Change MIDI instrument (1 = acoustic grand piano)
        msg[0] = (byte) 0xc0;
        msg[1] = (byte) 1;
        midi.write(msg);
    }

    public static SoundPlayer getInstance() {
        if (soundPlayer == null) soundPlayer = new SoundPlayer();
        return soundPlayer;
    }

    public void clearAll(){
        this.notes.clear();
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                this.playNote((Byte) event.getData());
                this.notes.add((Byte) event.getData());
                break;
            case PIANO_KEY_UP:
                this.stopNote((Byte) event.getData());
                this.notes.remove((Byte) event.getData());
                break;
            case MIDI_DATA_AUDIO:
                this.playMidi((byte[]) event.getData());
                break;
            case BACK:
                this.pauseNotes();
                this.notes.clear();
                break;
            case PAUSE:
                this.pauseNotes();
                break;
            case MIDI_FILE_PAUSE:
                this.paused = true;
                this.pauseNotes();
                break;
            case RESUME:
                this.resumeNotes();
                break;
            case MIDI_FILE_PLAY:
                this.paused = false;
                this.resumeNotes();
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    private void playNote(byte note) {
        // note 60 = C4 (Middle C), volume 64 = mezzo-forte
        sendMidiNote(true, 1, note, Constants.MIDI_PIANO_MEZZO_FORTE);
    }

    private void stopNote(byte note) {
        // note 60 = C4 (Middle C), volume = standard (64)
        sendMidiNote(false, 1, note, Constants.MIDI_PIANO_MEZZO_FORTE);
    }

    private void sendMidiNote(boolean noteOn, int channel, int note, int volume) {
        byte message[] = new byte[3];

        byte midiMessage = (byte) (Constants.MIDI_NOTE_OFF | channel);

        if (noteOn) midiMessage |= Constants.MIDI_NOTE_ON;

        message[0] = midiMessage;
        message[1] = (byte) note;
        message[2] = (byte) volume;

        midi.write(message);
    }

    private void playMidi(byte[] midiMessage) {
        midi.write(midiMessage);

        boolean on = (midiMessage[0] ^ Constants.MIDI_NOTE_ON) - 1 == 0;

        if (on) {
            notes.add(midiMessage[1]);
        } else {
            notes.remove((Byte) midiMessage[1]);
        }
    }

    private void pauseNotes() {
        for (byte note : notes) stopNote(note);
    }

    private void resumeNotes() {
        if (paused) return;
        for (byte note : notes) playNote(note);
    }
}
