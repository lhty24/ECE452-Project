package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.util.Constants;

public class PianoStage extends Stage {
    public PianoStage(PalmPiano.PalmPianoCallback ppCallback) {
        super();

        int xLeft = this.getViewport().getScreenX();
        int width = this.getViewport().getScreenWidth();

        KeyboardGroup keyboardGroup = new KeyboardGroup();
        ToolbarGroup toolbarGroup = new ToolbarGroup(xLeft, width, ppCallback);
        this.addActor(keyboardGroup);
        this.addActor(toolbarGroup);

        GameVisualsGroup gvg = new GameVisualsGroup();
        gvg.setName("gameGroup");
        gvg.setY(Constants.WK_HEIGHT);
        this.addActor(gvg);

        Event newStageEvent = new Event<>(Event.EventType.NEW_STAGE, this);
        EventBus.getInstance().dispatch(newStageEvent);
        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
        cam.position.set(cam.position.x + Constants.CAMERA_VIEWPORT_X_OFFSET, cam.position.y, 0);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        super.touchDragged(screenX, screenY, pointer);
        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
        float x = Gdx.input.getDeltaX();
        if (cam.position.x - x > getViewport().getScreenWidth() / 2 && cam.position.x - x < Constants.WK_WIDTH * 49 - getViewport().getScreenWidth() / 2) {
            cam.position.set(cam.position.x - x, cam.position.y, 0);
            switch (ModeTracker.getMode()) {
                case MODE_COMPOSITION:
//                    recordStopBtn.setPosition(recordStopBtn.getX() - x, Gdx.graphics.getHeight() - 200);
                    Actor recordStopBtn = this.getRoot().findActor("recordStopBtn");
                    Actor returnBtn_comp = this.getRoot().findActor("returnBtn");
                    Actor menuBtn_comp = this.getRoot().findActor("menuBtn");
                    recordStopBtn.setPosition(recordStopBtn.getX() - x, recordStopBtn.getY());
                    returnBtn_comp.setPosition(returnBtn_comp.getX()-x, returnBtn_comp.getY());
                    menuBtn_comp.setPosition(menuBtn_comp.getX()-x, menuBtn_comp.getY());
                    break;
                case MODE_GAME:
                    Actor playPauseBtn = this.getRoot().findActor("playPauseBtn");
                    Actor resetBtn = this.getRoot().findActor("resetBtn");
                    Actor returnBtn = this.getRoot().findActor("returnBtn");
                    Actor menuBtn_game = this.getRoot().findActor("menuBtn");
                    playPauseBtn.setPosition(playPauseBtn.getX() - x, playPauseBtn.getY());
                    resetBtn.setPosition(resetBtn.getX() - x, resetBtn.getY());
                    returnBtn.setPosition(returnBtn.getX() - x, returnBtn.getY());
                    menuBtn_game.setPosition(menuBtn_game.getX()-x, menuBtn_game.getY());
                    break;
            }
        }
        this.getCamera().update();
        return true;
    }


}

