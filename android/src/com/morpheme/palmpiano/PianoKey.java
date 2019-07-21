package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.morpheme.palmpiano.util.Constants;

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
                pressed = true;
                System.out.println(midiNote);
                EventBus.getInstance().dispatch(new Event<Object>(Event.EventType.PIANO_KEY_DOWN, midiNote));
                return true;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                pressed = false;
                System.out.println(midiNote);
                EventBus.getInstance().dispatch(new Event<Object>(Event.EventType.PIANO_KEY_UP, midiNote));
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
