package com.morpheme.palmpiano;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.morpheme.palmpiano.midi.MidiComposer;
import com.morpheme.palmpiano.midi.MidiNotePlayback;
import com.morpheme.palmpiano.midi.MidiPlayback;
import com.morpheme.palmpiano.midi.MidiPlaybackProxy;
import com.morpheme.palmpiano.util.Constants;
import com.morpheme.palmpiano.sheetmusic.FileUri;
import com.morpheme.palmpiano.sheetmusic.SheetMusicActivity;

public class MainMenu extends Activity {
    private MidiNotePlayback playback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                ModeTracker.setMode(Constants.PianoMode.MODE_COMPOSITION);
                launchPalmPiano("");
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
                configureTrackList();
                System.out.println("Going to game mode");
                ModeTracker.setMode(Constants.PianoMode.MODE_GAME);
            }
        });
    }

    private void configureTrackList() {
    }

    private void configureButtonStart() {
        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting mode");
                Spinner spinner = findViewById(R.id.trackSpinner);
                int trackNum = spinner.getSelectedItemPosition();
                String midiFileName = getResources().getStringArray(R.array.trackListFileName)[trackNum];
                launchPalmPiano(midiFileName);
            }
        });
    }

    private void launchPalmPiano(String midiFileName) {
        if (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK) {
            Uri uri = Uri.parse("file:///android_asset/" + midiFileName);
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
}
