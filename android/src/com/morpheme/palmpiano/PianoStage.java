package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.morpheme.palmpiano.util.Constants;

public class PianoStage extends Stage {
    public PianoStage() {
        super();

        int posX = this.getViewport().getScreenX();

        KeyboardGroup keyboardGroup = new KeyboardGroup();
        ToolbarGroup toolbarGroup = new ToolbarGroup(posX);
        this.addActor(keyboardGroup);
        this.addActor(toolbarGroup);

        GameVisualsGroup gvg = new GameVisualsGroup();
        gvg.setName("gameGroup");
        gvg.setY(Constants.WK_HEIGHT);
        this.addActor(gvg);

        Event newStageEvent = new Event<>(Event.EventType.NEW_STAGE, this);
        EventBus.getInstance().dispatch(newStageEvent);
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
                    Actor btn_comp = this.getRoot().findActor("recordStopBtn");
                    btn_comp.setPosition(btn_comp.getX() - x, Gdx.graphics.getHeight() - 200);
                    break;
                case MODE_GAME:
                    Actor btn_game1 = this.getRoot().findActor("playPauseBtn");
                    Actor btn_game2 = this.getRoot().findActor("resetBtn");
                    btn_game1.setPosition(btn_game1.getX() - x, Gdx.graphics.getHeight() - 200);
                    btn_game2.setPosition(btn_game2.getX() - x, Gdx.graphics.getHeight() - 320);
                    break;
            }
        }
        this.getCamera().update();
        return true;
    }


}

