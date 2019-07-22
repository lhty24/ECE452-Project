package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.concurrent.Semaphore;

public class GameVisualsGroup extends Group {
    private Semaphore gameVisualsMutex;

    public GameVisualsGroup() {
        this.gameVisualsMutex = new Semaphore(1);
        addActor(new ScoreLabelActor());
    }

    public Semaphore getGameVisualsMutex() {
        return gameVisualsMutex;
    }

}
