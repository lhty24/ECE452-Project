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
import com.morpheme.palmpiano.midi.MidiComposer;
import com.morpheme.palmpiano.midi.MidiFileIO;
import com.morpheme.palmpiano.midi.MidiFileParser;
import com.morpheme.palmpiano.midi.MidiNotePlayback;
import com.morpheme.palmpiano.midi.MidiPlayback;
import com.morpheme.palmpiano.midi.MidiPlaybackProxy;
import com.morpheme.palmpiano.midi.Note;
import com.morpheme.palmpiano.util.Constants;
import com.pdrogfer.mididroid.MidiFile;

import java.util.ArrayList;
import java.util.List;

public class PalmPiano implements ApplicationListener {
    private PianoStage stage;
    private EventBus eb;
    private Context context;
    private PianoMode mode;

	// Order of notes in octave
	public String[] notes = {"A", "AS", "B", "C", "CS", "D", "DS", "E", "F", "FS", "G", "GS"};

	public static final int wkInterval = Constants.WK_WIDTH + Constants.WK_GAP;
	public static final int[] offsetMap = {
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

	public static class RhythmBox extends Actor {
		private boolean bk = true;
		private static Texture textureWk;
		private static Texture textureBk;

		private long duration;

		private float actorX = 0, actorY = 0;
		private boolean started = true;

		public RhythmBox(boolean bk, int midi_note, long duration){
			this.bk = bk;
			int notePosition = getNotePosition((byte) midi_note);
			this.actorX = notePosition;
			this.duration = duration;
			System.out.println("actorX value>>>>>>>>>>>>>>>>>>>>>>>: " + actorX);
			this.actorY = 1300;

			if (textureWk == null || textureBk == null) {
				textureWk = new Texture(Gdx.files.internal("t1.png"));
				textureBk = new Texture(Gdx.files.internal("t2.png"));
			}
//			setBounds(actorX,actorY,text.getWidth(),texture.getHeight());
			addListener(new InputListener(){
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					((RhythmBox)event.getTarget()).started = true;
					return true;
				}
			});
		}

		@Override
		public void draw(Batch batch, float alpha){
			batch.setColor(1,1,1, 1);
//			System.out.println("Size: >>>>>>>>>>>>>>" + duration + " " + (int)(duration / 10000000l));
			if (bk)
				batch.draw(textureBk,actorX,actorY,textureBk.getWidth(), (int)(duration / 5000000l));
			else
				batch.draw(textureWk,actorX,actorY,textureWk.getWidth(), (int)(duration / 5000000l));
			batch.setColor(1,1,1,1);
		}

		@Override
		public void act(float delta){
			if(started){
				actorY-=5;
			}
		}
	}

	public enum PianoMode {MODE_COMPOSITION, MODE_PLAYBACK, MODE_GAME}

	public class PianoKey extends Actor {
		private boolean bk = false;
		private Byte midiNote = 0x0b;
		private Texture texture;
		private Sprite sprite;
		private float actorX = 0, actorY = 0;
		private boolean pressed;

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
			batch.draw(sprite,actorX,actorY);
			batch.setColor(1,1,1,1);
		}

		@Override
		public void act(float delta){

		}
	}


	@Override
	public void create() {
		eb = EventBus.getInstance();
		stage = new PianoStage(eb);
		Gdx.input.setInputProcessor(stage);

		RhythmBoxListener rhythmBoxListener = new RhythmBoxListener(stage);

		List<PianoKey> wks = new ArrayList<>();
		List<PianoKey> bks = new ArrayList<>();
		List<RhythmBox> boxes = new ArrayList<>();

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
		if (bundle != null) {
			this.mode = (PianoMode) bundle.getSerializable("pianoMode");

			// TODO: Implement actual logic for composition/game mode-specific actions
			switch (mode) {
				case MODE_PLAYBACK:
					System.out.println("Detected composition mode");
					break;
				case MODE_GAME:
					System.out.println("Detected game mode");

					for (int oc = 0; oc < 7; oc++) {
						//int offset = 0 + oc * (7 * (Constants.WK_WIDTH + Constants.WK_GAP));
						boolean bk;

						//RhythmBox box = new RhythmBox(true, getNotePosition((byte) 80));
						RhythmBox box = new RhythmBox(false, 21, 1);
						//System.out.println("NotePosition>>>>>>>>>>>>>>>>: " + getNotePosition((byte) 21));
						box.setTouchable(Touchable.enabled);
						boxes.add(box);
						stage.addActor(box);
					}
					break;
				default:
					break;
			}
		} else {
			this.mode = null;
		}

		SoundPlayer.initialize(context);

		switch (mode) {
			case MODE_COMPOSITION:
				MidiComposer c = new MidiComposer(context);
				break;
			case MODE_GAME:
			case MODE_PLAYBACK:
				MidiNotePlayback playback = new MidiPlaybackProxy(mode, MidiPlayback.BOTH_HANDS, context, (String) bundle.getSerializable("midiFile"));
				EventBus.getInstance().register(playback);
				Thread midiThread = new Thread(playback);
				midiThread.start();
				break;
			default:
				break;
		}
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
	public static int getNotePosition(Byte midiByte) {
    	int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
    	int octave = shifted/12;
    	int keyIndex = shifted % 12;

		//System.out.println("KeyIndex value>>>>>>>>>>>>>>>>>>>>>>>: " + keyIndex);
    	return ((7 * octave) * wkInterval + offsetMap[keyIndex]); //hardcoded

	}

	public static boolean getNoteBk(Byte midiByte) {
		int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
		int i = shifted % 12;

		//System.out.println("KeyIndex value>>>>>>>>>>>>>>>>>>>>>>>: " + keyIndex);
		return ( i == 1 || i == 4|| i == 6 || i == 9 || i == 11 ); //hardcoded

	}
}
