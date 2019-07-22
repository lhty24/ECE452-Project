package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.morpheme.palmpiano.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class ScoreLabelActor extends Actor implements EventListener {

    private HashSet<Event.EventType> monitoredEvents;
    private long score;
    private float accuracy;
    private BitmapFont bmFont;
    OrthographicCamera camera;

    public ScoreLabelActor() {
        this.score = 0;
        this.accuracy = 0.0f;
        this.bmFont = new BitmapFont();
        // Font made by gomarice: https://www.1001fonts.com/game-music-love-font.html
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("gomarice_game_music_love.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        bmFont = generator.generateFont(parameter);
        generator.dispose();
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.UPDATE_SCORE);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        camera = (OrthographicCamera) this.getParent().getStage().getCamera();
        float posX = camera.position.x - camera.viewportWidth / 2;
        // FIXME: value 50 is for the ToolbarGroup height in the future
        bmFont.draw(batch, "Score: " + score + "\nAccuracy: " + accuracy + "%", 10 + posX, Gdx.graphics.getHeight() - Constants.WK_HEIGHT - 50);
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case UPDATE_SCORE:
                score = (long) ((float []) event.getData())[0];
                accuracy = ((float []) event.getData())[1];
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }
}
