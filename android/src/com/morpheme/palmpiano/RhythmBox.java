package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class RhythmBox extends Actor {
    private boolean bk;
    private boolean started;

    private static Texture textureWk;
    private static Texture textureBk;

    private long duration;

    private float actorX;
    private float actorY;

    public RhythmBox(boolean bk, int midi_note, long duration) {
        this.bk = bk;
        this.started = true;
        int notePosition = PalmPiano.getNotePosition((byte) midi_note);
        this.actorX = notePosition;
        this.duration = duration;
        this.actorY = 1300;
    }

    public static void setTextures() {
        textureWk = new Texture(Gdx.files.internal("t1.png"));
        textureBk = new Texture(Gdx.files.internal("t2.png"));
    }

    // FIXME - Magic Eyeball
    @Override
    public void draw(Batch batch, float alpha){
        batch.setColor(1,1,1, 1);

        if (bk) {
            batch.draw(textureBk, actorX, actorY, textureBk.getWidth(), (int) (duration / 5000000L));
        } else {
            batch.draw(textureWk, actorX, actorY, textureWk.getWidth(), (int) (duration / 5000000L));
        }

        batch.setColor(1,1,1,1);
    }

    @Override
    public void act(float delta){
        if(started) {
            actorY -= 5;
        }
    }
}