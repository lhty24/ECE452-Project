package com.morpheme.palmpiano;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.morpheme.palmpiano.util.Constants;

public class AndroidLauncher extends AndroidApplication implements PalmPiano.PalmPianoCallback{
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		PalmPiano palmPiano = new PalmPiano(this);
		palmPiano.setCallback(this);
		initialize(palmPiano, config);
	}

	@Override
	public void onMenuPressed() {
		Intent intent = new Intent(this, MainMenu.class);
		intent.putExtra("destination", Constants.MENU_SETTINGS);
		finish();
		startActivity(intent);
	}

	@Override
	public void onReturnPressed() {
		Intent intent = new Intent(this, MainMenu.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
		startActivity(intent);
	}

	@Override
	public void onGameEnd() {
		Intent intent = new Intent(this, MainMenu.class);
		intent.putExtra("destination", Constants.MENU_LEADERBOARD);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
		startActivity(intent);
	}
}
