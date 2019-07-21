package com.morpheme.palmpiano.midi;

import android.os.Environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.pdrogfer.mididroid.MidiFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class MidiFileIO {
    public static MidiFile getMidiFile(String filename) {
        try {
            File dir = Gdx.files.local("midi/").file();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File mid = Gdx.files.local("midi/" + filename).file();

            if (!mid.exists() || mid.length() == 0) {
                // Internal storage is read only so read it and write to local storage file
                // Get byte array of pre selected midi file
                FileHandle handle = Gdx.files.internal(filename);
                byte[] midiBytes = handle.readBytes();

                // Write bytes to empty file
                OutputStream fos = new FileOutputStream(mid);
                fos.write(midiBytes);
                fos.close();
            }

            // Create midi file
            return new MidiFile(mid);
        } catch (GdxRuntimeException e) {
            System.err.println("Internal APK midi file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeMidiFile(MidiFile midi, String filename) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) return;

        try {
            File dir = Gdx.files.local("midi/").file();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File mid = Gdx.files.local("midi/"+filename).file();
            midi.writeToFile(mid);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
