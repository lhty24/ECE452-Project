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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MidiFileIO {

    public MidiFileIO() {

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

            // Create midi file.
            return new MidiFile(mid);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getMidiEvents(MidiFile midiFile) {
        int PPQ = midiFile.getResolution();

        if((PPQ & 0x0000) != 0) {
            System.out.println("easy");
        }

        List<MidiNoteEvent> midiNoteEvents = new ArrayList<MidiNoteEvent>();

        for(MidiTrack track : midiFile.getTracks()) {
            List<MidiEvent> midiEvents = new ArrayList<MidiEvent>();
            Iterator<MidiEvent> it = track.getEvents().iterator();

            int BPM = 200;

            while(it.hasNext()) {
                MidiEvent event = it.next();

                if(event instanceof NoteOn || event instanceof NoteOff) {
                    midiEvents.add(event);
                } else if(event instanceof Tempo) {
                    BPM = (int)((Tempo) event).getBpm();
                }
            }

            long nsPerTick = 60000000000l / (PPQ * BPM);

            for(MidiEvent event : midiEvents) {
                midiNoteEvents.add(new MidiNoteEvent(event, nsPerTick));
            }
        }

        Collections.sort(midiNoteEvents, new SortMidi());

        long now = System.nanoTime();

        for(MidiNoteEvent event : midiNoteEvents) {
            MidiEvent e = event.getMidiEvent();
            long timestamp = event.getTimestamp();

            byte[] noteEvent = new byte[3];

            if(e instanceof NoteOn) {
                NoteOn note = (NoteOn)e;
                noteEvent[0] = note.getVelocity() > 0 ? (byte)0x91 : (byte)0x81;
                noteEvent[1] = (byte)note.getNoteValue();
                noteEvent[2] = (byte)note.getVelocity();
            } else if(e instanceof NoteOff) {
                NoteOff note = (NoteOff)e;
                noteEvent[0] = (byte)0x81;
                noteEvent[1] = (byte)note.getNoteValue();
                noteEvent[2] = (byte)note.getVelocity();
            }

            while(System.nanoTime() - now < timestamp) {}

            System.out.println(timestamp
                    + (noteEvent[0] == (byte)0x91 ? " ON  " : " OFF ")
                    + " Note: " + (int)noteEvent[1]
                    + " Velocity: " + (int)noteEvent[2]
            );
        }
    }


}

class SortMidi implements Comparator<MidiNoteEvent> {

    @Override
    public int compare(MidiNoteEvent o1, MidiNoteEvent o2) {
        return (int)(o1.getTimestamp() - o2.getTimestamp());
    }
}