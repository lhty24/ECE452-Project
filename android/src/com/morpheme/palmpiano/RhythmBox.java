package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.morpheme.palmpiano.util.Constants;

public class RhythmBox extends Actor {
    // FIXME: currently some random number that (kinda) works
    private static long time_by_height_factor = 5000000L;
    private static boolean isRunning = true;

    private boolean bk;

    private static Texture textureWk;
    private static Texture textureBk;

    private long duration;
    private int boxHeight;

    private float actorX;
    private float actorY;

    public RhythmBox(boolean bk, int midi_note, long duration) {
        this.bk = bk;
        int notePosition = KeyboardGroup.getNotePosition((byte) midi_note);
        this.actorX = notePosition;
        this.duration = duration;
        this.boxHeight = (int) (duration / time_by_height_factor);
        this.actorY = 1300;
    }

    public static void setTextures() {
        textureWk = new Texture(Gdx.files.internal("t1.png"));
        textureBk = new Texture(Gdx.files.internal("t2.png"));
    }

    public static void setIsRunning(boolean newIsRunning) {
        isRunning = newIsRunning;
    }

    @Override
    public void draw(Batch batch, float alpha){
        batch.setColor(1,1,1, 1);

        if (bk) {
            batch.draw(textureBk, actorX, actorY, textureBk.getWidth(), boxHeight);
        } else {
            batch.draw(textureWk, actorX, actorY, textureWk.getWidth(), boxHeight);
        }

        batch.setColor(1,1,1,1);
    }

    @Override
    public void act(float delta){
        if(isRunning) {
            actorY -= 5;
            if (actorY + boxHeight < getParent().getY()) {
                this.remove();
            }
        }
    }
}