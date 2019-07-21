package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.morpheme.palmpiano.util.Constants;

public class FailNoteActor extends Actor {
    private static final float scaleSpeedFactor = 0.5f;
    private static final float floatUpSpeedFactor = 7.5f;
    private static float initialScale = 0.075f;
    private static Texture texturePaintbrushX;

    public FailNoteActor(byte midi_note) {
        int notePosition = PalmPiano.getNotePosition(midi_note);
        if (PalmPiano.getNoteBk(midi_note)) {
            setX(notePosition + Constants.BK_WIDTH / 2);
        }
        else {
            setX(notePosition + Constants.WK_WIDTH / 2);
        }
        setY(Constants.WK_HEIGHT);
    }

    public static void setTextures() {
        texturePaintbrushX = new Texture(Gdx.files.internal("red_paintbrush_x.png"));
    }

    public static float getInitialScale() {
        return initialScale;
    }

    // Set the initial scaling factor applied to the image to fit the piano key
    public static void setInitialScale(float newInitialScale) {
        initialScale = newInitialScale;
    }

    @Override
    public void draw(Batch batch, float alpha){
        batch.draw(texturePaintbrushX, getX() - (texturePaintbrushX.getWidth() * initialScale * getScaleX()) / 2,
                getY() + (texturePaintbrushX.getHeight() * initialScale * (1 - getScaleY())),
                texturePaintbrushX.getWidth() * initialScale * getScaleX(),
                texturePaintbrushX.getHeight() * initialScale * getScaleY());
    }

    @Override
    public void act(float delta){
        setScale(getScaleX() - (delta * scaleSpeedFactor), getScaleY() - (delta * scaleSpeedFactor));
        if (getScaleX() <= 0.1 || getScaleY() <= 0.1) {
            this.remove();
        }
        setY(getY() + (delta * floatUpSpeedFactor));
    }
}
