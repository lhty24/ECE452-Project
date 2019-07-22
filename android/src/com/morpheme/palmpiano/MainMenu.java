package com.morpheme.palmpiano;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.OpenableColumns;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainMenu extends Activity {
    private MidiNotePlayback playback;
    private static final int READ_PERM = 1;
    private static final int WRITE_PERM = 2;
    private static final int IMPORT_CODE = 900;
    private static boolean hasReadPerms = false;
    private static boolean hasWritePerms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        ModeTracker.setMode(Constants.PianoMode.MODE_MENU);
        Bundle bun = this.getIntent().getExtras();
        if (bun != null) {
            Serializable serial = bun.getSerializable("destination");
            switch (serial.toString()) {
                case Constants.MENU_LEADERBOARD:
                    // TODO: actually go to leaderboard
                    System.out.println("Going to leaderboard screen");
                    break;
                case Constants.MENU_SETTINGS:
                    setContentView(R.layout.activity_settings_menu);
                    configureTrackList();
                    configureButtonImport();
                    configureButtonExport();
                    configureButtonShare();
                    configureButtonDelete();
                    configureButtonBack(false);
                    break;
                default:
                    break;
            }
        } else {
            setContentView(R.layout.activity_main_menu);

            initializeModules();
            configureMain();
        }


    }

    private void configureMain() {
        configureButtonComposition();
        configureButtonPlayback();
        configureButtonGame();
        configureButtonLeaderboard();
        configureButtonSettings();
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

        FailNoteListener failNoteListener = new FailNoteListener();
        eventBus.register(failNoteListener);

        ScoreSystem scoreSystem = new ScoreSystem();
        eventBus.register(scoreSystem);

        NoteTimerTracker noteTimerTracker = new NoteTimerTracker();
        eventBus.register(noteTimerTracker);
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
                configureButtonBack(true);
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
                configureButtonBack(true);
                configureTrackList();
                System.out.println("Going to game mode");
                ModeTracker.setMode(Constants.PianoMode.MODE_GAME);
            }
        });
    }

    private void configureTrackList() {
        Spinner spinner = findViewById(R.id.trackSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getMidiFiles());
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

    private void configureButtonLeaderboard() {
        Button buttonLeaderboard = findViewById(R.id.buttonLeaderboard);
        buttonLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going to leaderboard screen");
            }
        });
    }

    private void configureButtonSettings() {
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going back to settings screen");
                setContentView(R.layout.activity_settings_menu);
                configureTrackList();
                configureButtonImport();
                configureButtonExport();
                configureButtonShare();
                configureButtonDelete();
                configureButtonBack(true);
            }
        });
    }

    private void configureButtonExport() {
        Button buttonExport = findViewById(R.id.buttonExport);
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = findViewById(R.id.trackSpinner);
                String midiFileName = spinner.getSelectedItem().toString();

                writeExternal(midiFileName);
            }
        });
    }

    private void configureButtonShare() {
        Button buttonShare = findViewById(R.id.buttonShare);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = findViewById(R.id.trackSpinner);
                String midiFileName = spinner.getSelectedItem().toString();
                writeExternal(midiFileName);

                File f = new File(Environment.getExternalStorageDirectory(), "midi/" + midiFileName);
                Uri path = Uri.fromFile(f);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("vnd.android.cursor.dir/email");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PalmPiano: " + midiFileName);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }

    private void configureButtonDelete() {
        Button buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = findViewById(R.id.trackSpinner);
                String midiFileName = spinner.getSelectedItem().toString();

                // For now delete the matching file on both local and external dirs
                File f1 = new File(Constants.localPath, midiFileName);
                File f2 = new File(Environment.getExternalStorageDirectory(), "midi/" + midiFileName);
                boolean deleted = f1.delete() || f2.delete();
                if (deleted) {
                    Toast.makeText(MainMenu.this, "Deleted " + midiFileName + " from storage", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainMenu.this, "Failed to delete " + midiFileName + " from storage", Toast.LENGTH_LONG).show();
                }
                configureTrackList();
            }
        });
    }

    private void configureButtonImport() {
        Button buttonImport = findViewById(R.id.buttonImport);
        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Importing");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, IMPORT_CODE);
            }
        });
    }

    // If toMenu = true, always return to Main Menu.
    // Otherwise, returns to previous activity
    private void configureButtonBack(boolean toMenu) {
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toMenu) {
                    System.out.println("Going back to menu screen");
                    setContentView(R.layout.activity_main_menu);
                    ModeTracker.setMode(Constants.PianoMode.MODE_MENU);
                    initializeModules();
                    configureMain();
                } else {
                    onBackPressed();
                }
            }
        });
    }

    private void launchPalmPiano(String midiFileName) {
        if (ModeTracker.getMode() == Constants.PianoMode.MODE_PLAYBACK) {
            Uri uri;

            // First check if the file is in LOCAL
            File f = new File(Constants.localPath + midiFileName);
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
        } else {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMPORT_CODE:
                if (data != null) {
                    Uri uri = data.getData();
                    System.out.println(requestCode);
                    System.out.println(resultCode);
                    System.out.println(data.getDataString());

                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;

                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                    }

                    if (!(displayName.endsWith(".mid") || displayName.endsWith(".midi"))) {
                        Toast.makeText(MainMenu.this, "This is not a MIDI file!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);

                        File dir = new File(Constants.localPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File f = new File(Constants.localPath, displayName);

                        int read;
                        byte[] bytes = new byte[1024];
                        FileOutputStream fos = new FileOutputStream(f);
                        while ((read = is.read(bytes)) != -1) {
                            fos.write(bytes, 0, read);
                        }
                        fos.close();
                        configureTrackList();
                        Toast.makeText(MainMenu.this, "Imported " + displayName + " successfully", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;

        }

    }


    public List<String> getMidiFiles() {
        List<String> fileNames = new ArrayList<>();
        try {
            // MIDI files in ASSETS folder
            String[] assets = getAssets().list("");
            for (String f : assets) {
                if (f.endsWith(".mid") || f.endsWith(".midi"))
                    fileNames.add(f);
            }

            // MIDI files in DATA/ INTERNAL STORAGE folder
            File local = new File(Constants.localPath);
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
        return fileNames;
    }

    public void writeExternal(String midiFileName) {
        hasWritePerms = ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        // Check write permissions
        if (hasWritePerms) {
            System.out.println("Exporting");
            Uri uri;
            // First check if the file is in LOCAL
            File f = new File(Constants.localPath + midiFileName);
            if (f.exists()) {
                uri = Uri.parse(f.toString());
            } else {
                // If not, it is in INTERNAL
                uri = Uri.parse("file:///android_asset/" + midiFileName);
            }

            FileUri file = new FileUri(uri, midiFileName);
            byte[] data = file.getData(MainMenu.this);

            System.out.println(file.getUri());
            System.out.println(data);

            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "midi/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                f = new File(Environment.getExternalStorageDirectory(), "midi/" + midiFileName);

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);
                fos.close();
                Toast.makeText(MainMenu.this, "Saved to external storage at ~/midi/" + midiFileName, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(MainMenu.this, "Failed to write to external storage", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(MainMenu.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERM);
        }
    }
}
