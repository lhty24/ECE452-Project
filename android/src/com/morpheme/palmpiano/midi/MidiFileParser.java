package com.morpheme.palmpiano.midi;

import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.MidiTrack;
import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;
import com.pdrogfer.mididroid.event.meta.Tempo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class MidiFileParser {
    public static List<Note> getMidiEvents(MidiFile midiFile) {
        int PPQ = midiFile.getResolution();

        if ((PPQ & 0x8000) != 0) {
            System.out.println("TODO: Time-Code based time.");
        }

        List<Note> midiNoteEvents = new ArrayList<>();

        // Midi Default BPM
        int BPM = 120;

        int trackNum = 0;

        for (MidiTrack track : midiFile.getTracks()) {
            List<MidiEvent> midiEvents = new ArrayList<>();
            Iterator<MidiEvent> it = track.getEvents().iterator();

            // If type is 1, all tracks use the same BPM
            BPM = midiFile.getType() == 1 ? BPM : 120;

            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event instanceof NoteOn || event instanceof NoteOff) {
                    midiEvents.add(event);
                } else if (event instanceof Tempo) {
                    BPM = (int) ((Tempo) event).getBpm();
                }
            }

            trackNum++;

            for (MidiEvent event : midiEvents) {
                midiNoteEvents.add(new Note(event, trackNum, BPM, PPQ));
            }
        }

        Collections.sort(midiNoteEvents, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                if (o1.getTimestamp() > o2.getTimestamp()) return 1;
                if (o1.getTimestamp() < o2.getTimestamp()) return -1;
                return 0;
            }
        });

        // Calculate length of note
        for (int i = 0; i < midiNoteEvents.size(); i++) {
            Note note = midiNoteEvents.get(i);
            if (note.getNoteType() == Note.NOTE_OFF) continue;

            for (int j = i + 1; j < midiNoteEvents.size(); j++) {
                Note offNote = midiNoteEvents.get(j);

                if (offNote.getNoteType() == Note.NOTE_OFF && offNote.getNoteValue() == note.getNoteValue()) {
                    midiNoteEvents.get(i).setLength(offNote.getTick() - note.getTick());
                    break;
                }
            }
        }

        return midiNoteEvents;
    }
}
