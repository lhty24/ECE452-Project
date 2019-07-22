package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.morpheme.palmpiano.util.Constants;

public class RhythmBox extends Actor {
    private static boolean isRunning = true;
    private static final float VELOCITY = 250.0F;

    private boolean bk;

    private static Texture textureWk;
    private static Texture textureBk;

    private long duration;
    private int boxHeight;

    private float actorX;
    private float actorY;
    private float viewWidth;
    private float viewHeight;
    private float groupHeight;

    private static long delay = 0L;

    public RhythmBox(boolean bk, int midi_note, long duration) {
        this.bk = bk;
        int notePosition = KeyboardGroup.getNotePosition((byte) midi_note);
        this.actorX = notePosition;
        this.duration = duration;
        this.viewWidth = Gdx.graphics.getWidth();
        this.viewHeight = Gdx.graphics.getHeight();
        this.groupHeight = this.viewHeight - Constants.WK_HEIGHT;
        this.actorY = this.groupHeight;
        delay = (long) (this.groupHeight / VELOCITY * 1000000000L);
        this.boxHeight = (int) (duration / 1000000000.0F * VELOCITY);
    }

    public static float getVelocity() {
        return VELOCITY;
    }

    public static long getDelay() {
        return delay;
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
        // Only draw falling tiles within bounds - above keyboard
        Rectangle scissors = new Rectangle();
        Rectangle clipBounds = new Rectangle(0, 0, viewWidth * 20, viewHeight - Constants.WK_HEIGHT);

        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clipBounds, scissors);

        batch.flush();

        if (ScissorStack.pushScissors(scissors)) {
            if (bk) {
                batch.draw(textureBk, actorX, actorY, Constants.BK_WIDTH, boxHeight);
            } else {
                batch.draw(textureWk, actorX, actorY, Constants.WK_WIDTH, boxHeight);
            }

            batch.flush();
            ScissorStack.popScissors();
        }
    }

    @Override
    public void act(float dt){
        if(isRunning) {
            actorY -= VELOCITY * dt;
            // Negative value to have box persist longer in case it might still be used elsewhere
            if (actorY + boxHeight < -50) {
                GameVisualsGroup gameGroup = (GameVisualsGroup) getParent();
                try {
                    gameGroup.getGameVisualsMutex().acquire();
                    this.remove();
                    gameGroup.getGameVisualsMutex().release();
                }
                catch (InterruptedException e) {
                    System.err.println(e.toString());
                }
            }
        }
    }
}