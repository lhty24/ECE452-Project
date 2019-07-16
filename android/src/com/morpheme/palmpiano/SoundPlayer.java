package com.morpheme.palmpiano;

import com.morpheme.palmpiano.util.Constants;

import java.util.HashSet;
import java.util.Set;

import org.billthefarmer.mididriver.MidiDriver;

public class SoundPlayer implements EventListener {
    private static SoundPlayer soundPlayer = null;

    private HashSet<Event.EventType> monitoredEvents;
    private MidiDriver midi;

    /* Constructor stuff */
    private SoundPlayer() {
        super();
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
        monitoredEvents.add(Event.EventType.PIANO_KEY_UP);
        monitoredEvents.add(Event.EventType.MIDI_DATA_AUDIO);

        midi = new MidiDriver();
        midi.start();

        byte msg[] = new byte[2];
        // Change MIDI instrument (1 = acoustic grand piano)
        msg[0] = (byte) 0xc0;
        msg[1] = (byte) 1;
        midi.write(msg);
    }

    public static SoundPlayer getInstance() {
        if (soundPlayer == null) {
            soundPlayer = new SoundPlayer();
        }
        return soundPlayer;
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.getEventType()) {
            case PIANO_KEY_DOWN:
                this.playNote((Byte) event.getData());
                break;
            case PIANO_KEY_UP:
                this.stopNote((Byte) event.getData());
                break;
            case MIDI_DATA_AUDIO:
                this.playMidi((byte[]) event.getData());
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

    private void sendMidiNote(boolean noteOn, int channel, int note, int volume)
    {
        byte message[] = new byte[3];

        byte midiMessage = (byte) (Constants.MIDI_NOTE_OFF | channel);
        if (noteOn) {
            midiMessage |= Constants.MIDI_NOTE_ON;
        }

        message[0] = midiMessage;
        message[1] = (byte) note;
        message[2] = (byte) volume;

        midi.write(message);
    }

    private void playMidi(byte[] midiMessage) {
        midi.write(midiMessage);
    }
}

// On pause, stop all sounds or else you will hear a long note.