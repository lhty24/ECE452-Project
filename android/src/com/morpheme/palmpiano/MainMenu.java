package com.morpheme.palmpiano;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.morpheme.palmpiano.sheetmusic.SheetMusicActivity;

public class MainMenu extends AppCompatActivity {
    private com.morpheme.palmpiano.PalmPiano.PianoMode mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        configureButtonComposition();
        configureButtonGame();
        configureButtonSheetMusic();
    }

    private void configureButtonComposition() {
        Button buttonComposition = (Button) findViewById(R.id.buttonComposition);
        buttonComposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_track_menu);
                configureButtonStart();
                configureTrackList();
                System.out.println("Going to composition mode");
                mode = PalmPiano.PianoMode.MODE_COMPOSITION;
            }
        });
    }

    private void configureButtonGame() {
        Button buttonGame = (Button) findViewById(R.id.buttonGame);
        buttonGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_track_menu);
                configureButtonStart();
                configureTrackList();
                System.out.println("Going to game mode");
                mode = PalmPiano.PianoMode.MODE_GAME;
            }
        });
    }

    private void configureButtonSheetMusic() {
        Button buttonComposition = (Button) findViewById(R.id.buttonComposition);
        buttonComposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_track_menu);
                configureButtonStart();
                configureTrackList();
                System.out.println("Going to sheet music mode");
                mode = PalmPiano.PianoMode.MODE_SHEET_MUSIC;
            }
        });
    }

    private void configureTrackList() {
    }

    private void configureButtonStart() {
        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting mode");
                Spinner spinner = findViewById(R.id.trackSpinner);
                int trackNum = spinner.getSelectedItemPosition();
                String midiFileName = getResources().getStringArray(R.array.trackListFileName)[trackNum];
                launchPalmPiano(mode, midiFileName);
            }
        });
    }

    private void launchPalmPiano(PalmPiano.PianoMode mode, String midiFileName) {
        System.out.println("Launching PalmPiano activity");

        if (mode == PalmPiano.PianoMode.MODE_SHEET_MUSIC) {
            Intent intent = new Intent(MainMenu.this, SheetMusicActivity.class);
            // FIXME: include appropriate input data (MIDI file?)
            intent.putExtras();
            startActionMode(intent);
        }
        else {
            startActivity(new Intent(MainMenu.this, AndroidLauncher.class));
            Bundle bundle = new Bundle();
            bundle.putSerializable("pianoMode", mode);
            bundle.putSerializable("midiFile", midiFileName);
            Intent intent = new Intent(MainMenu.this, AndroidLauncher.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
