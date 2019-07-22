package com.morpheme.palmpiano;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class PalmPiano implements ApplicationListener {
    private PianoStage stage;
    private Context context;

	public interface PalmPianoCallback {
		public void onMenuPressed();
		public void onGameEnd();
	}

	private PalmPianoCallback ppCallback;


	public PalmPiano(Context context) {
    	super();
		this.context = context;
	}

	public void setCallback(PalmPianoCallback ppCallback) {
		this.ppCallback = ppCallback;
	}

	@Override
	public void create() {
		stage = new PianoStage(ppCallback);
		Gdx.input.setInputProcessor(stage);

		Intent intent = ((Activity) context).getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			// TODO: Implement actual logic for composition/game mode-specific actions
			switch (ModeTracker.getMode()) {
				case MODE_COMPOSITION:
					System.out.println("Detected composition mode");
					EventBus.getInstance().dispatch(new Event<>(Event.EventType.NEW_MIDI_FILE, null));
					break;
				case MODE_GAME:
					System.out.println("Detected game mode");
					EventBus.getInstance().dispatch(new Event<>(Event.EventType.NEW_MIDI_FILE, (String) bundle.getSerializable("midiFile")));
					break;
				case MODE_PLAYBACK:
					System.out.println("Detected playback mode");
					EventBus.getInstance().dispatch(new Event<>(Event.EventType.NEW_MIDI_FILE, (String) bundle.getSerializable("midiFile")));
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void dispose() {
		EventBus.getInstance().dispatch(new Event<>(Event.EventType.BACK, null));
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		EventBus.getInstance().dispatch(new Event<>(Event.EventType.PAUSE, null));
	}

	@Override
	public void resume() {
		EventBus.getInstance().dispatch(new Event<>(Event.EventType.RESUME, null));
	}
}
