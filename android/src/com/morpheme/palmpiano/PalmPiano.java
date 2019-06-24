package com.morpheme.palmpiano;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.morpheme.palmpiano.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class PalmPiano implements ApplicationListener {
    private PianoStage stage;
    private EventBus eb;
    private Context context;
    private TextButton buttonBack;
	private TextButton playPauseButton;
    private PianoMode mode;

	// Order of notes in octave
	public String[] notes = {"A", "AS", "B", "C", "CS", "D", "DS", "E", "F", "FS", "G", "GS"};

	public int wkInterval = Constants.WK_WIDTH + Constants.WK_GAP;
	public int[] offsetMap = {
			0,
			wkInterval - Constants.BK_WIDTH / 2,
			wkInterval,
			2 * wkInterval,
			3 * wkInterval - Constants.BK_WIDTH / 2,
			3 * wkInterval,
			4 * wkInterval - Constants.BK_WIDTH / 2,
			4 * wkInterval,
			5 * wkInterval,
			6 * wkInterval - Constants.BK_WIDTH / 2,
			6 * wkInterval,
			7 * wkInterval - Constants.BK_WIDTH / 2
	};

    public PalmPiano(Context context) {
    	super();
		this.context = context;
	}

	public enum PianoMode {MODE_COMPOSITION, MODE_GAME}

	public class PianoKey extends Actor {
		boolean bk = false;
		Byte midiNote = 0x0b;
		Texture texture;
		Sprite sprite;
		float actorX = 0, actorY = 0;
		boolean pressed;

		public PianoKey(boolean bk, final Byte midiNote, float x){
			String file = "wk.png";
			this.bk = bk;
			if (bk) {
				this.actorY = Constants.WK_HEIGHT-Constants.BK_HEIGHT;
				file = "bk.png";
			}
			texture = new Texture(Gdx.files.internal(file));
			sprite = new Sprite(texture);
			this.midiNote = midiNote;
			this.actorX = x;
			setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
			addListener(new InputListener(){
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					System.out.println(midiNote.toString());
					pressed = true;
					System.out.println(midiNote);
					eb.dispatch(new Event<Object>(Event.EventType.PIANO_KEY_DOWN, midiNote));
//					eb.dispatch(new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
					return true;
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					pressed = false;
					System.out.println(midiNote);
					eb.dispatch(new Event<Object>(Event.EventType.PIANO_KEY_UP, midiNote));
//					eb.dispatch(new Event<>(Event.EventType.MIDI_FILE_PAUSE, null));
				}
			});
		}


		@Override
		public void draw(Batch batch, float alpha){
			// Visual feedback that key is pressed
			if (this.pressed)
				batch.setColor(1,1,1, 0.5f);
			else
				batch.setColor(1,1,1, 1);
			batch.draw(texture,actorX,actorY);
			batch.setColor(1,1,1,1);
		}

		@Override
		public void act(float delta){

		}
	}


	@Override
	public void create() {
		eb = EventBus.getInstance();
		stage = new PianoStage();
		Gdx.input.setInputProcessor(stage);

		List<PianoKey> wks = new ArrayList<>();
		List<PianoKey> bks = new ArrayList<>();

		for (int oc = 0; oc < 7; oc++) {
			boolean bk;
			for (int i = 0; i < notes.length; i++) {
				if ( i == 1 || i == 4|| i == 6 || i == 9 || i == 11 ) {
					bk = true;
				} else {
					bk = false;
				}
				PianoKey k = new PianoKey(bk, (byte) (Constants.MIDI_OFFSET + i + oc*12),  offsetMap[i] + oc* (7* wkInterval));
				k.setTouchable(Touchable.enabled);
				if (bk) {
					bks.add(k);
					continue;
				} else {
					wks.add(k);
				}
			}
		}

		for (PianoKey wk : wks) {
			stage.addActor(wk);
		}

		for (PianoKey bk : bks) {
			stage.addActor(bk);
		}

		Intent intent = ((Activity) context).getIntent();
		Bundle bundle = intent.getExtras();
		this.mode = (PianoMode) bundle.getSerializable("pianoMode");

		// TODO: Implement actual logic for composition/game mode-specific actions
		switch (mode) {
			case MODE_COMPOSITION:
				System.out.println("Detected composition mode");
				break;
			case MODE_GAME:
				System.out.println("Detected game mode");
				break;
			default:
				break;
		}

		SoundPlayer.initialize(context);

		MidiFileIO midi = new MidiFileIO();
		Thread midiThread = new Thread(midi);
		midiThread.start();
	}

	@Override
	public void dispose() {
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
	}

	@Override
	public void resume() {
	}

	// Given a midi byte, return the corresponding x position of the key in the game engine
	public int getNotePosition(Byte midiByte) {
    	int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
    	int octave = shifted/12;
    	int keyIndex = shifted % 12;

    	return ((7 * octave) * wkInterval + offsetMap[keyIndex]);

	}
}
