package com.morpheme.palmpiano;

import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.morpheme.palmpiano.util.Constants;

public class ToolbarGroup extends Group {
    private Button returnBtn, playPauseBtn, resetBtn, recordStopBtn;
    private boolean playing, recording;


    public ToolbarGroup(int posX) {
        super();

        this.setPosition(0, 0);

        this.playing = false;
        this.recording = false;

        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        this.setPosition(posX, Gdx.graphics.getHeight()-100);

        // Prep res for buttons
        Texture textureReturn, textureRecordStop, texturePlayPause, textureReset;
        TextureRegion regionReturn, regionRecordStop, regionPlayPause, regionReset;
        TextureRegionDrawable drawableReturn, drawableRecordStop, drawablePlayPause, drawableReset;
        TextureRegionDrawable drawableRecordStopChecked, drawablePlayPauseChecked;

        textureReturn = new Texture(Gdx.files.internal("btn_return.png"));
        regionReturn = new TextureRegion(textureReturn);
        drawableReturn = new TextureRegionDrawable(regionReturn);

        returnBtn = new Button(drawableReturn);
        returnBtn.setSize(50, 50);
        returnBtn.setPosition(Constants.BAR_BTN1, Constants.BAR_HEIGHT);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("returnBtn clicked");
                // TBD
                }
            }
        );

//        //returnBtn.addListener() {};
//        private void configureButtonBack() {
//            android.widget.Button buttonBack = findViewById(R.id.buttonBack);
//            buttonBack.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    System.out.println("Going back to menu screen");
//                    setContentView(R.layout.activity_main_menu);
//                    ModeTracker.setMode(Constants.PianoMode.MODE_MENU);
//                    initializeModules();
//                    configureButtonComposition();
//                    configureButtonPlayback();
//                    configureButtonGame();
//                }
//            });
//        }


        this.addActor(returnBtn);

        switch (ModeTracker.getMode()) {
            case MODE_COMPOSITION:
                textureRecordStop = new Texture(Gdx.files.internal("btn_record.png"));
                regionRecordStop = new TextureRegion(textureRecordStop);
                drawableRecordStop = new TextureRegionDrawable(regionRecordStop);

                textureRecordStop = new Texture(Gdx.files.internal("btn_stop.png"));
                regionRecordStop = new TextureRegion(textureRecordStop);
                drawableRecordStopChecked = new TextureRegionDrawable(regionRecordStop);

                recordStopBtn = new Button(drawableRecordStop, drawableRecordStop, drawableRecordStopChecked);
                recordStopBtn.setSize(50, 50);
                recordStopBtn.setPosition(Constants.BAR_BTN2, Constants.BAR_HEIGHT);
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
                playPauseBtn.setSize(50, 50);
                playPauseBtn.setPosition(Constants.BAR_BTN2, Constants.BAR_HEIGHT);


//                playPauseBtn = new TextButton("Start", uiSkin, "default");
//                playPauseBtn.setSize(300, 100);
//                playPauseBtn.setPosition(Constants.BAR_BTN1, 0);
                playPauseBtn.addListener(new InputListener() {
//                    @Override
//                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
////                        ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
//                        EventBus.getInstance().dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
//                        setPlaying(!isPlaying());
//                        return true;
//                    }

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

//                    @Override
//                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
//                    }
                });

                this.addActor(playPauseBtn);

                textureReset = new Texture(Gdx.files.internal("btn_rewind.png"));
                regionReset = new TextureRegion(textureReset);
                drawableReset = new TextureRegionDrawable(regionReset);

                resetBtn = new Button(drawableReset);
                resetBtn.setSize(50, 50);
                resetBtn.setPosition(Constants.BAR_BTN3, Constants.BAR_HEIGHT);
                resetBtn.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                        ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Pause" : "Play");
                        EventBus.getInstance().dispatch(isPlaying() ? new Event<>(Event.EventType.MIDI_FILE_PAUSE, null) : new Event<>(Event.EventType.MIDI_FILE_PLAY, null));
                        setPlaying(!isPlaying());
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
//                        ((TextButton) playPauseBtn).getLabel().setText(isPlaying() ? "Playing" : "Paused");
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

//    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {}
//    @Override
//    public boolean touchDragged(int screenX, int screenY, int pointer) {
//        super.touchDragged(screenX, screenY, pointer);
//        OrthographicCamera cam = ((OrthographicCamera)this.getCamera());
//        float x = Gdx.input.getDeltaX();
//        if (cam.position.x - x > getViewport().getScreenWidth() / 2 && cam.position.x - x < Constants.WK_WIDTH * 49 - getViewport().getScreenWidth() / 2) {
//            cam.position.set(cam.position.x - x, cam.position.y, 0);
//            switch (ModeTracker.getMode()) {
//                case MODE_COMPOSITION:
//                    recordStopBtn.setPosition(recordStopBtn.getX() - x, Gdx.graphics.getHeight() - 200);
//                    break;
//                case MODE_GAME:
//                    playPauseBtn.setPosition(playPauseBtn.getX() - x, Gdx.graphics.getHeight() - 200);
//                    resetBtn.setPosition(resetBtn.getX() - x, Gdx.graphics.getHeight() - 320);
//                    break;
//            }
//        }
//        this.getCamera().update();
//        return true;
//    }

    public class FileDialog implements Input.TextInputListener {
        @Override
        public void input(String fileName) {
            if (!fileName.isEmpty()) {
                EventBus.getInstance().dispatch(new Event<>(Event.EventType.MIDI_RECORD_START, fileName));
//                ((TextButton) recordStopBtn).getLabel().setText("Stop");
                setRecording(true);
            }
        }

        @Override
        public void canceled() {
        }
    }
}