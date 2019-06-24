package com.morpheme.palmpiano;

import android.content.Context;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.morpheme.palmpiano.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class PalmPiano implements ApplicationListener {
	private Stage stage;
	private EventBus eb;
	private Context context;


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

	public class RhythmBox extends Actor {
		boolean bk = true;
		//String note;
		Texture texture;
		Sprite sprite;
		float actorX = 0, actorY = 0;
		boolean started = true;

		public RhythmBox(boolean bk, float x){
			//String file = "box_gr.png";
			String file = "t1.png";
			this.bk = bk;
			//if (bk) {
			this.actorY = 1000;
//				file = "box_gr.png";
			file = "t1.png";
			//}
			texture = new Texture(Gdx.files.internal(file));
			sprite = new Sprite(texture);
			//this.note = note;
			this.actorX = x;
			setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
//			addListener(new InputListener(){
//				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
////					setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
//					pressed = true;
//					System.out.println(note);
//					return true;
//				}
//
//				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//					pressed = false;
//				}
//			});
			addListener(new InputListener(){
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					((RhythmBox)event.getTarget()).started = true;
					return true;
				}
			});
		}


		@Override
		public void draw(Batch batch, float alpha){
			// Visual feedback that key is pressed
			if (this.started)
				batch.setColor(1,1,1, 0.5f);
			else
				batch.setColor(1,1,1, 1);
			batch.draw(texture,actorX,actorY);
			batch.setColor(1,1,1,1);
		}

		@Override
		public void act(float delta){
			if(started){
				actorY-=5;
			}
		}
	}

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
//					setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
					System.out.println(midiNote.toString());
					pressed = true;
					System.out.println(midiNote);
//					eb.dispatch(new Event<Object>(Event.EventType.PIANO_KEY_DOWN, midiNote));
					return true;
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					pressed = false;
					System.out.println(midiNote);
//					eb.dispatch(new Event<Object>(Event.EventType.PIANO_KEY_UP, midiNote));
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
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		List<PianoKey> wks = new ArrayList<>();
		List<PianoKey> bks = new ArrayList<>();
		List<RhythmBox> boxes = new ArrayList<>();
		//List<RhythmBox> boxes_wk = new ArrayList<>();


		for (int oc = 0; oc < 7; oc++) {
			boolean bk;
			for (int i = 0; i < notes.length; i++) {
				if ( (i < 5 && i % 2 == 1) || (i >= 5 && i % 2 == 0) ) {
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

		//Rhythm Boxes
		for (int oc = 0; oc < 7; oc++) {
			//int offset = 0 + oc * (7 * (Constants.WK_WIDTH + Constants.WK_GAP));
			boolean bk;
			for (int i = 0; i < notes.length; i++) {
				if ( (i < 5 && i % 2 == 1) || (i >= 5 && i % 2 == 0) ) {
					bk = true;
				} else {
					bk = false;
				}

				//RhythmBox box = new RhythmBox(bk, bk ? offset - Constants.BK_WIDTH/2 : offset);
				//RhythmBox box = new RhythmBox(true, bk? i*(130)+30-45 : i*(130)+30);
				//RhythmBox box = new RhythmBox(true, bk ? i*(130) : i*(130) - 45 );
				RhythmBox box = new RhythmBox(true, i*(130));
				box.setTouchable(Touchable.enabled);
				boxes.add(box);
//				if (bk) {
//					boxes_bk.add(box);
//					continue;
//				} else {
//					boxes_wk.add(box);
//				}
				//offset += (Constants.WK_WIDTH + Constants.WK_GAP);
				stage.addActor(box);
			}
		}


//		for(int i = 0; i < 18; i++) {
//			RhythmBox box = new RhythmBox(true,i*(130));
//			boxes_bk.add(box);
//			box.setTouchable(Touchable.enabled);
//			stage.addActor(box);
//		}

//		// Black keys
//		// Refactor checking into exclusion set
//		// Note ES and BS are never used, refactor later
//		String[] notesSharp = {"CS", "DS", "ES", "FS", "GS", "AS", "BS"};
//		keyIndex = 2;
//		octave = 3;
//		for(int i = 0; i < 18; i++) {
//			keyIndex++;
//			if (keyIndex % 7 == 0) {
//				keyIndex = 0;
//				octave++;
//			}
//			if ( i == 3 || i == 6 || i == 10 || i == 13 || i == 17 )
//				continue;
//			PianoKey bk = new PianoKey(true, SoundPlayer.Note.valueOf(notesSharp[keyIndex]+octave), (Constants.WK_WIDTH + Constants.WK_GAP)-Constants.BK_WIDTH/2 + i*(Constants.WK_WIDTH + Constants.WK_GAP));
//			bks.add(bk);
//			bk.setTouchable(Touchable.enabled);
//			stage.addActor(bk);
//		}

		SoundPlayer.initialize(context);
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
