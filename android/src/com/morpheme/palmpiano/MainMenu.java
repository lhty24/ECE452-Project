package com.morpheme.palmpiano;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.morpheme.palmpiano.midi.MidiComposer;
import com.morpheme.palmpiano.midi.MidiNotePlayback;
import com.morpheme.palmpiano.midi.MidiPlayback;
import com.morpheme.palmpiano.midi.MidiPlaybackProxy;
import com.morpheme.palmpiano.sheetmusic.FileUri;
import com.morpheme.palmpiano.sheetmusic.SheetMusicActivity;
import com.morpheme.palmpiano.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainMenu extends Activity {
    private MidiNotePlayback playback;
    private static final int READ_PERM = 1;
    private static final int WRITE_PERM = 2;
    private static boolean hasReadPerms = false;
    private static boolean hasWritePerms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ModeTracker.setMode(Constants.PianoMode.MODE_MENU);
        initializeModules();
        configureButtonComposition();
        configureButtonPlayback();
        configureButtonGame();
    }

    private void initializeModules() {
        EventBus eventBus = EventBus.getInstance();

        SoundPlayer soundPlayer = SoundPlayer.getInstance();
        eventBus.register(soundPlayer);

        MidiComposer c = new MidiComposer();
        eventBus.register(c);

        playback = new MidiPlaybackProxy(MidiPlayback.BOTH_HANDS);
        eventBus.register(playback);

        RhythmBoxListener rhythmBoxListener = new RhythmBoxListener();
        eventBus.register(rhythmBoxListener);
    }

    private void configureButtonComposition() {
        Button buttonComposition = findViewById(R.id.buttonComposition);
        buttonComposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going to composition mode");

                hasWritePerms = ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                // Check write permissions
                if (hasWritePerms) {
                    ModeTracker.setMode(Constants.PianoMode.MODE_COMPOSITION);
                    launchPalmPiano("");
                } else {
                    // Request permission from the user
                    ActivityCompat.requestPermissions(MainMenu.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERM);
                }
            }
        });
    }

    private void configureButtonPlayback() {
        Button buttonPlayback = (Button) findViewById(R.id.buttonPlayback);
        buttonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_track_menu);
                configureButtonStart();
                configureButtonBack();
                configureTrackList();
                System.out.println("Going to playback mode");
                ModeTracker.setMode(Constants.PianoMode.MODE_PLAYBACK);
            }
        });
    }

    private void configureButtonGame() {
        Button buttonGame = findViewById(R.id.buttonGame);
        buttonGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_track_menu);
                configureButtonStart();
                configureButtonBack();
                configureTrackList();
                System.out.println("Going to game mode");
                ModeTracker.setMode(Constants.PianoMode.MODE_GAME);
            }
        });
    }

    private void configureTrackList() {
        Spinner spinner = findViewById(R.id.trackSpinner);
        // list all files in directory
//        Uri uri = Uri.parse("/data/data/" + this.getPackageName() + "/assets");

//        File folder = new File(uri.getPath());
//        File[] listOfFiles = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        try {
            // MIDI files in ASSETS folder
            String[] assets = getAssets().list("");
            for (String f : assets) {
                if (f.endsWith(".mid") || f.endsWith(".midi"))
                    fileNames.add(f);
            }

            // MIDI files in DATA/ INTERNAL STORAGE folder
            File local = new File("/data/user/0/com.morpheme.palmpiano/files/midi/");
            String[] composedMidis = local.list();
            for (String f : composedMidis) {
                if ((f.endsWith(".mid") || f.endsWith(".midi")) && !fileNames.contains(f))
                    fileNames.add(f);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        hasReadPerms = ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        // Check read permissions
        if (hasReadPerms) {
        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(MainMenu.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERM);
        }

        Collections.sort(fileNames);

        // populate above list from your desired path
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configureButtonStart() {
        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting mode");
                Spinner spinner = findViewById(R.id.trackSpinner);
                String midiFileName = spinner.getSelectedItem().toString();
                launchPalmPiano(midiFileName);
            }
        });
    }

    private void configureButtonBack() {
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going back to menu screen");
                setContentView(R.layout.activity_main_menu);
                ModeTracker.setMode(Constants.PianoMode.MODE_MENU);
                initializeModules();
                configureButtonComposition();
                configureButtonPlayback();
                configureButtonGame();
            }
        });
    }

    private void launchPalmPiano(String midiFileName) {
        if (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK) {
            Uri uri;

            // First check if the file is in LOCAL
            File f = new File("/data/user/0/com.morpheme.palmpiano/files/midi/"  + midiFileName);
            if (f.exists()) {
                uri = Uri.parse(f.toString());
            } else {
                // If not, it is in INTERNAL
                uri = Uri.parse("file:///android_asset/" + midiFileName);
            }

            FileUri file = new FileUri(uri, midiFileName);

            Intent intent = new Intent(Intent.ACTION_VIEW, file.getUri(), this, SheetMusicActivity.class);
            intent.putExtra(SheetMusicActivity.MidiTitleID, file.toString());
            startActivity(intent);
        }
        else {
            System.out.println("Launching PalmPiano activity");
            Thread midiThread = new Thread(playback);
            midiThread.start();
            Bundle bundle = new Bundle();
            bundle.putSerializable("midiFile", midiFileName);
            Intent intent = new Intent(MainMenu.this, AndroidLauncher.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(MainMenu.this, "Read permission not granted, MIDI file read disabled", Toast.LENGTH_LONG).show();
                }
                break;
            case WRITE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(MainMenu.this, "Write permission not granted, MIDI composition disabled", Toast.LENGTH_LONG).show();
                }

        }
    }
}
