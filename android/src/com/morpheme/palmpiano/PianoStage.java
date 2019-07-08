package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.morpheme.palmpiano.util.Constants;

public class PianoStage extends Stage {
    public Button playPauseBtn;
    public Button resetBtn;
    public EventBus eb;

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean playing = false;

    public PianoStage(final EventBus eb) {
        super();
        this.eb = eb;
        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        playPauseBtn = new TextButton("Start",uiSkin,"default");
        resetBtn = new TextButton("Reset",uiSkin,"default");
        playPauseBtn.setSize(300,100);
        resetBtn.setSize(300,100);
        playPauseBtn.setPosition(this.getViewport().getScreenX(),Gdx.graphics.getHeight()-200);
        resetBtn.setPosition(this.getViewport().getScreenX(),Gdx.graphics.getHeight()-320);
        playPauseBtn.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
                eb.dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                setPlaying(!isPlaying());
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Playing" : "Paused");
            }
        });
        resetBtn.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
                eb.dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                setPlaying(!isPlaying());
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Playing" : "Paused");
            }
        });
        this.addActor(playPauseBtn);
        this.addActor(resetBtn);
    }



    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        super.touchDragged(screenX, screenY, pointer);
        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
        float x = Gdx.input.getDeltaX();
//        System.out.println(getViewport().getScreenWidth()/2);
        if (cam.position.x - x> getViewport().getScreenWidth()/2 && cam.position.x - x < Constants.WK_WIDTH*49-getViewport().getScreenWidth()/2) {
            cam.position.set(cam.position.x - x, cam.position.y, 0);
            playPauseBtn.setPosition(playPauseBtn.getX() - x, Gdx.graphics.getHeight()-200);
            resetBtn.setPosition(resetBtn.getX() - x, Gdx.graphics.getHeight()-320);
        }
        this.getCamera().update();
        return true;
    }
}
