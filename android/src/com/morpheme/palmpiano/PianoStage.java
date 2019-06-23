package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.util.Constants;

public class PianoStage extends Stage {
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        super.touchDragged(screenX, screenY, pointer);
        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
        float x = Gdx.input.getDeltaX();
        System.out.println(getViewport().getScreenWidth()/2);
        if (cam.position.x - x> getViewport().getScreenWidth()/2 && cam.position.x - x < Constants.WK_WIDTH*49-getViewport().getScreenWidth()/2)
            cam.position.set(cam.position.x - x, cam.position.y, 0);
        this.getCamera().update();
        return true;
    }
}
