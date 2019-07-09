package com.morpheme.palmpiano.midi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pdrogfer.mididroid.MidiFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class MidiFileIO {
    public static MidiFile getMidiFile(String filename) {
        try {
            // Internal storage is read only so read it and write to local storage file
            // Get byte array of pre selected midi file
            FileHandle handle = Gdx.files.internal(filename);
            byte[] midiBytes = handle.readBytes();

            // Create empty file
            File mid = Gdx.files.local(filename).file();
            mid.createNewFile();

            // Write bytes to empty file
            OutputStream fos = new FileOutputStream(mid);
            fos.write(midiBytes);
            fos.close();

            // Create midi file
            return new MidiFile(mid);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
