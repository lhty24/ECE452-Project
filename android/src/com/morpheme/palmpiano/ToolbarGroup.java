package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.morpheme.palmpiano.util.Constants;

public class ToolbarGroup extends Group {
    private Button returnBtn, playPauseBtn, resetBtn, recordStopBtn, menuBtn;
    private boolean playing, recording;


    public ToolbarGroup(int xLeft, int width, PalmPiano.PalmPianoCallback ppCallback) {
        super();
        this.setPosition(0, 0);
        this.playing = false;
        this.recording = false;

        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        this.setPosition(xLeft, Gdx.graphics.getHeight()-100);

        Texture textureReturn, textureRecordStop, texturePlayPause, textureReset, textureMenu;
        TextureRegion regionReturn, regionRecordStop, regionPlayPause, regionReset, regionMenu;
        TextureRegionDrawable drawableReturn, drawableRecordStop, drawablePlayPause, drawableReset, drawableMenu;
        TextureRegionDrawable drawableRecordStopChecked, drawablePlayPauseChecked;

        textureReturn = new Texture(Gdx.files.internal("btn_return.png"));
        regionReturn = new TextureRegion(textureReturn);
        drawableReturn = new TextureRegionDrawable(regionReturn);

        returnBtn = new Button(drawableReturn);
        returnBtn.setName("returnBtn");
        returnBtn.setSize(Constants.BAR_BTN_SIZE, Constants.BAR_BTN_SIZE);
        returnBtn.setPosition(Constants.BAR_BTN1+ Constants.CAMERA_VIEWPORT_X_OFFSET, Constants.BAR_HEIGHT);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ppCallback.onReturnPressed();
                }
            }
        );

        textureMenu = new Texture(Gdx.files.internal("btn_menu.png"));
        regionMenu = new TextureRegion(textureMenu);
        drawableMenu = new TextureRegionDrawable(regionMenu);

        menuBtn = new Button(drawableMenu);
        menuBtn.setName("menuBtn");
        menuBtn.setSize(Constants.BAR_BTN_SIZE, Constants.BAR_BTN_SIZE);
        menuBtn.setPosition(xLeft + width - 100+ Constants.CAMERA_VIEWPORT_X_OFFSET, Constants.BAR_HEIGHT);

        menuBtn.addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                  System.out.println("returnBtn clicked");
                  ppCallback.onMenuPressed();
              }
          }
        );

        this.addActor(returnBtn);
        this.addActor(menuBtn);

        switch (ModeTracker.getMode()) {
            case MODE_COMPOSITION:
                textureRecordStop = new Texture(Gdx.files.internal("btn_record.png"));
                regionRecordStop = new TextureRegion(textureRecordStop);
                drawableRecordStop = new TextureRegionDrawable(regionRecordStop);

                textureRecordStop = new Texture(Gdx.files.internal("btn_stop.png"));
                regionRecordStop = new TextureRegion(textureRecordStop);
                drawableRecordStopChecked = new TextureRegionDrawable(regionRecordStop);

                recordStopBtn = new Button(drawableRecordStop, drawableRecordStop, drawableRecordStopChecked);
                recordStopBtn.setName("recordStopBtn");
                recordStopBtn.setSize(Constants.BAR_BTN_SIZE, Constants.BAR_BTN_SIZE);
                recordStopBtn.setPosition(Constants.BAR_BTN2+ Constants.CAMERA_VIEWPORT_X_OFFSET, Constants.BAR_HEIGHT);
                recordStopBtn.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (!isRecording()) {
                            //PianoStage pianoStage = new PianoStage();
                            FileDialog listener = new FileDialog();
                            Gdx.input.getTextInput(listener, "Compose MIDI file", "", "My composition");
                        } else {
                            EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_RECORD_STOP, null));
                            setRecording(false);
                        }
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
//                        ((TextButton) recordStopBtn).getLabel().setText(isRecording() ? "Stop" : "Record");
                    }
                });

                this.addActor(recordStopBtn);
                break;
            case MODE_GAME:
                // Game
                texturePlayPause = new Texture(Gdx.files.internal("btn_play.png"));
                regionPlayPause = new TextureRegion(texturePlayPause);
                drawablePlayPause = new TextureRegionDrawable(regionPlayPause);

                texturePlayPause = new Texture(Gdx.files.internal("btn_pause.png"));
                regionPlayPause = new TextureRegion(texturePlayPause);
                drawablePlayPauseChecked = new TextureRegionDrawable(regionPlayPause);

                playPauseBtn = new Button(drawablePlayPause, drawablePlayPause, drawablePlayPauseChecked);
                playPauseBtn.setName("playPauseBtn");
                playPauseBtn.setSize(Constants.BAR_BTN_SIZE, Constants.BAR_BTN_SIZE);
                playPauseBtn.setPosition(Constants.BAR_BTN2+ Constants.CAMERA_VIEWPORT_X_OFFSET, Constants.BAR_HEIGHT);

                playPauseBtn.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (isPlaying()) {
                            EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_FILE_PAUSE, null));
                        }
                        else {
                            EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                        }
                        setPlaying(!isPlaying());
                        return true;
                    }
                });

                this.addActor(playPauseBtn);

                textureReset = new Texture(Gdx.files.internal("btn_rewind.png"));
                regionReset = new TextureRegion(textureReset);
                drawableReset = new TextureRegionDrawable(regionReset);

                resetBtn = new Button(drawableReset);
                resetBtn.setName("resetBtn");
                resetBtn.setSize(Constants.BAR_BTN_SIZE, Constants.BAR_BTN_SIZE);
                resetBtn.setPosition(Constants.BAR_BTN3+ Constants.CAMERA_VIEWPORT_X_OFFSET, Constants.BAR_HEIGHT);
                resetBtn.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        EventBus.getInstance().dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                        setPlaying(!isPlaying());
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
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

    public class FileDialog implements Input.TextInputListener {
        @Override
        public void input(String fileName) {
            if (!fileName.isEmpty()) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_RECORD_START, fileName));
                setRecording(true);
            }
        }

        @Override
        public void canceled() {
        }
    }
}