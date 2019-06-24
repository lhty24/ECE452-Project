package com.morpheme.palmpiano;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        configureButtonComposition();
        configureButtonGame();
    }

    private void configureButtonComposition() {
        Button buttonComposition = (Button) findViewById(R.id.buttonComposition);
        buttonComposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going to composition mode");
                launchPalmPiano(PalmPiano.PianoMode.MODE_COMPOSITION);
            }
        });
    }

    private void configureButtonGame() {
        Button buttonGame = (Button) findViewById(R.id.buttonGame);
        buttonGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Going to game mode");
                launchPalmPiano(PalmPiano.PianoMode.MODE_GAME);
            }
        });
    }

    private void launchPalmPiano(PalmPiano.PianoMode mode) {
        System.out.println("Launching PalmPiano activity");
        startActivity(new Intent(MainMenu.this, AndroidLauncher.class));
        Bundle bundle = new Bundle();
        bundle.putSerializable("pianoMode", mode);
        Intent intent = new Intent(MainMenu.this, AndroidLauncher.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
