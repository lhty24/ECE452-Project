package com.morpheme.palmpiano;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.morpheme.palmpiano.midi.MidiComposer;
import com.morpheme.palmpiano.midi.MidiNotePlayback;
import com.morpheme.palmpiano.midi.MidiPlayback;
import com.morpheme.palmpiano.midi.MidiPlaybackProxy;
import com.morpheme.palmpiano.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class PalmPiano implements ApplicationListener {
    private PianoStage stage;
    private Context context;
    private Constants.PianoMode mode;

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

	@Override
	public void create() {
		stage = new PianoStage();
		Gdx.input.setInputProcessor(stage);

		RhythmBoxListener rhythmBoxListener = new RhythmBoxListener(stage);
		EventBus.getInstance().register(rhythmBoxListener);

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
			this.mode = (Constants.PianoMode) bundle.getSerializable("pianoMode");

			// TODO: Implement actual logic for composition/game mode-specific actions
			switch (mode) {
				case MODE_PLAYBACK:
					System.out.println("Detected composition mode");
					break;
				case MODE_GAME:
					System.out.println("Detected game mode");

					for (int oc = 0; oc < 7; oc++) {
						RhythmBox box = new RhythmBox(false, 21, 1);
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
				EventBus.getInstance().register(c);
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
    	int octave = shifted / 12;
    	int keyIndex = shifted % 12;
    	return ((7 * octave) * wkInterval + offsetMap[keyIndex]); //hardcoded
	}

	public static boolean getNoteBk(Byte midiByte) {
		int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
		int i = shifted % 12;
		return (i == 1 || i == 4 || i == 6 || i == 9 || i == 11); //hardcoded
	}
}
