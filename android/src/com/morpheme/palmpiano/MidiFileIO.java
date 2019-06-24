package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.MidiTrack;
import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;
import com.pdrogfer.mididroid.event.meta.Tempo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MidiFileIO implements EventListener, Runnable {

    private boolean isPlaying;
    private HashSet<Event.EventType> monitoredEvents;
    private PalmPiano.PianoMode pianoMode;

    public MidiFileIO(PalmPiano.PianoMode mode) {
        this.pianoMode = mode;
    }

    public MidiFile getMidiFile(String filename) {
        try {
            // Internal storage is read only so read it and write to local storage file.
            // Get byte array of pre selected midi file.
            FileHandle handle = Gdx.files.internal(filename);
            byte[] midiBytes = handle.readBytes();

            // Create empty file.
            File mid = Gdx.files.local(filename).file();
            mid.createNewFile();

            // Write bytes to empty file.
            OutputStream fos = new FileOutputStream(mid);
            fos.write(midiBytes);
            fos.close();

            isPlaying = false;

            this.monitoredEvents = new HashSet<>();
            monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
            monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
            EventBus.getInstance().register(this);

            // Create midi file.
            return new MidiFile(mid);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MidiNoteEvent> getMidiEvents(MidiFile midiFile) {
        int PPQ = midiFile.getResolution();

        if ((PPQ & 0x0000) != 0) {
            System.out.println("TODO");
        }

        List<MidiNoteEvent> midiNoteEvents = new ArrayList<MidiNoteEvent>();

        // Midi Default BPM.
        int BPM = 120;

        for (MidiTrack track : midiFile.getTracks()) {
            List<MidiEvent> midiEvents = new ArrayList<MidiEvent>();
            Iterator<MidiEvent> it = track.getEvents().iterator();

            // If type is 1, all tracks use the same BPM?
            BPM = midiFile.getType() == 1 ? BPM : 120;

            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event instanceof NoteOn || event instanceof NoteOff) {
                    midiEvents.add(event);
                } else if (event instanceof Tempo) {
                    BPM = (int) ((Tempo) event).getBpm();
                }
            }

            long nsPerTick = 60000000000l / (PPQ * BPM);

            for (MidiEvent event : midiEvents) {
                midiNoteEvents.add(new MidiNoteEvent(event, event.getTick() * nsPerTick));
            }
        }

        Collections.sort(midiNoteEvents, new Comparator<MidiNoteEvent>() {
            @Override
            public int compare(MidiNoteEvent o1, MidiNoteEvent o2) {
                if (o1.getTimestamp() > o2.getTimestamp()) return 1;
                if (o1.getTimestamp() < o2.getTimestamp()) return -1;
                return 0;
            }
        });

        return midiNoteEvents;
    }

    public void playbackMidi(List<MidiNoteEvent> midiNoteEvents) {
        long newNow = 0;
        long prev = System.nanoTime();
        long dt = 0;

        for(MidiNoteEvent event : midiNoteEvents) {
            MidiEvent e = event.getMidiEvent();
            long timestamp = event.getTimestamp();

            byte[] noteEvent = new byte[3];

            if (e instanceof NoteOn) {
                NoteOn note = (NoteOn) e;
                noteEvent[0] = note.getVelocity() > 0 ? (byte) 0x91 : (byte) 0x81;
                noteEvent[1] = (byte) note.getNoteValue();
                noteEvent[2] = (byte) note.getVelocity();
            } else if (e instanceof NoteOff) {
                NoteOff note = (NoteOff) e;
                noteEvent[0] = (byte) 0x81;
                noteEvent[1] = (byte) note.getNoteValue();
                noteEvent[2] = (byte) note.getVelocity();
            }

            while (dt <= timestamp) {
                newNow = System.nanoTime();
                while (!isPlaying) {
                    newNow = System.nanoTime();
                    prev = newNow;
                }
                dt += (newNow - prev);
                prev = newNow;
            }

            if (pianoMode == PalmPiano.PianoMode.MODE_COMPOSITION) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_AUDIO, noteEvent));
            } else if (pianoMode == PalmPiano.PianoMode.MODE_GAME) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_DATA_GAMEPLAY, noteEvent));
            }
        }
    }

    @Override
    public void run() {
        MidiFile midiFile = getMidiFile("mozart_eine_kleine_easy.mid");
        List<MidiNoteEvent> midiEvents = getMidiEvents(midiFile);
        playbackMidi(midiEvents);
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case MIDI_FILE_PLAY:
                this.isPlaying = true;
                break;
            case MIDI_FILE_PAUSE:
                this.isPlaying = false;
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
