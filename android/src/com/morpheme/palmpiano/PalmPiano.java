package com.morpheme.palmpiano;

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

	public class PianoKey extends Actor {
		boolean bk = false;
		String note;
		Texture texture;
		Sprite sprite;
		float actorX = 0, actorY = 0;
		boolean pressed;

		public PianoKey(boolean bk, final String note, float x){
			String file = "wk.png";
			this.bk = bk;
			if (bk) {
				this.actorY = 192;
				file = "bk.png";
			}
			texture = new Texture(Gdx.files.internal(file));
			sprite = new Sprite(texture);
			this.note = note;
			this.actorX = x;
			setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
			addListener(new InputListener(){
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
//					setBounds(actorX,actorY,texture.getWidth(),texture.getHeight());
					pressed = true;
					System.out.println(note);
					eb.dispatch(new Event<Object>(Event.EventType.PIANO_KEY_DOWN, "note"));
					return true;
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					pressed = false;
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

//		PianoKey myActor = new PianoKey();

		// White keys
		for(int i = 0; i < 18; i++) {
			PianoKey wk = new PianoKey(false, "w"+i, i*(Constants.WK_WIDTH + Constants.WK_GAP));
			wks.add(wk);
			wk.setTouchable(Touchable.enabled);
			stage.addActor(wk);
		}

		// Black keys
		// Refactor checking into exclusion set
		for(int i = 0; i < 18; i++) {
			if ( i == 3 || i == 6 || i == 10 || i == 13 || i == 17 )
				continue;
			PianoKey bk = new PianoKey(true, "b"+i, (Constants.WK_WIDTH + Constants.WK_GAP)-Constants.BK_WIDTH/2 + i*(Constants.WK_WIDTH + Constants.WK_GAP));
			bks.add(bk);
			bk.setTouchable(Touchable.enabled);
			stage.addActor(bk);
		}



		System.out.println("Test sound player wrld!");
		SoundPlayer player = SoundPlayer.getInstance();
		EventBus bus = EventBus.getInstance();
//		Event e1 = new Event(Event.EventType.PIANO_KEY_DOWN, bus);
//		Event e2 = new Event(Event.EventType.PIANO_KEY_UP, bus);
//		bus.dispatch(e1);
//		player.playNote(SoundPlayer.Note.C4);
//		bus.dispatch(e2);
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
}
