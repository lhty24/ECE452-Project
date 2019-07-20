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
    private Button playPauseBtn;
    private Button resetBtn;

    private boolean playing;

    public PianoStage() {
        super();

        this.playing = false;
        Event newStageEvent = new Event<>(Event.EventType.NEW_STAGE, this);
        EventBus.getInstance().dispatch(newStageEvent);

        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        playPauseBtn = new TextButton("Start",uiSkin,"default");
        playPauseBtn.setSize(300,100);
        playPauseBtn.setPosition(this.getViewport().getScreenX(),Gdx.graphics.getHeight()-200);
        playPauseBtn.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
                EventBus.getInstance().dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                setPlaying(!isPlaying());
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Playing" : "Paused");
            }
        });

        this.addActor(playPauseBtn);

        resetBtn = new TextButton("Reset",uiSkin,"default");
        resetBtn.setSize(300,100);
        resetBtn.setPosition(this.getViewport().getScreenX(),Gdx.graphics.getHeight()-320);
        resetBtn.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
                EventBus.getInstance().dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                setPlaying(!isPlaying());
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Playing" : "Paused");
            }
        });

        this.addActor(resetBtn);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        super.touchDragged(screenX, screenY, pointer);
        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
        float x = Gdx.input.getDeltaX();
        if (cam.position.x - x> getViewport().getScreenWidth()/2 && cam.position.x - x < Constants.WK_WIDTH*49-getViewport().getScreenWidth()/2) {
            cam.position.set(cam.position.x - x, cam.position.y, 0);
            playPauseBtn.setPosition(playPauseBtn.getX() - x, Gdx.graphics.getHeight()-200);
            resetBtn.setPosition(resetBtn.getX() - x, Gdx.graphics.getHeight()-320);
        }
        this.getCamera().update();
        return true;
    }
}
