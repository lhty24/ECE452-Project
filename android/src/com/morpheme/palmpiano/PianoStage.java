package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.morpheme.palmpiano.util.Constants;

public class PianoStage extends Stage {
    private Button playPauseBtn, resetBtn, recordStopBtn;

    private boolean playing, recording;

    public PianoStage() {
        super();

        KeyboardGroup keyboardGroup = new KeyboardGroup();
        this.addActor(keyboardGroup);

        this.playing = false;
        this.recording = false;
        Event newStageEvent = new Event<>(Event.EventType.NEW_STAGE, this);
        EventBus.getInstance().dispatch(newStageEvent);

        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        switch (ModeTracker.getMode()) {
            case MODE_COMPOSITION:
                // Composition
                recordStopBtn = new TextButton("Start",uiSkin,"default");
                recordStopBtn.setSize(300,100);
                recordStopBtn.setPosition(this.getViewport().getScreenX(),Gdx.graphics.getHeight()-200);
                recordStopBtn.addListener(new InputListener(){
                    @Override
                    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                        if(!isRecording()) {
                            FileDialog listener = new FileDialog();
                            Gdx.input.getTextInput(listener, "Compose MIDI file", "", "My composition");
                        } else {
                            ((TextButton) recordStopBtn).getLabel().setText("Record");
                            EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_RECORD_STOP, null));
                            setRecording(false);
                        }

                        return true;
                    }
                    @Override
                    public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                        ((TextButton) recordStopBtn).getLabel().setText(isRecording() ? "Stop" : "Record");
                    }
                });

                this.addActor(recordStopBtn);
                break;
            case MODE_GAME:
                // Game
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
                break;

        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
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
                    recordStopBtn.setPosition(recordStopBtn.getX() - x, Gdx.graphics.getHeight() - 200);
                    break;
                case MODE_GAME:
                    playPauseBtn.setPosition(playPauseBtn.getX() - x, Gdx.graphics.getHeight() - 200);
                    resetBtn.setPosition(resetBtn.getX() - x, Gdx.graphics.getHeight() - 320);
                    break;
            }
        }
        this.getCamera().update();
        return true;
    }

    public class FileDialog implements Input.TextInputListener {
        @Override
        public void input (String fileName)
        {
            if (!fileName.isEmpty()) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_RECORD_START, fileName));
                ((TextButton) recordStopBtn).getLabel().setText("Stop");
                setRecording(true);
            }
        }

        @Override
        public void canceled () {
        }


    }
}
