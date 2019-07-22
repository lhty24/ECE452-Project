package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.morpheme.palmpiano.util.Constants;

import java.util.concurrent.Semaphore;

public class GameVisualsGroup extends Group {
    private Semaphore gameVisualsMutex;

    public GameVisualsGroup() {
        this.gameVisualsMutex = new Semaphore(1);
        if (ModeTracker.getMode() == Constants.PianoMode.MODE_GAME) {
            ScoreLabelActor scoreLabel = new ScoreLabelActor();
            EventBus.getInstance().register(scoreLabel);
            addActor(scoreLabel);
        }
    }

    public Semaphore getGameVisualsMutex() {
        return gameVisualsMutex;
    }

}
