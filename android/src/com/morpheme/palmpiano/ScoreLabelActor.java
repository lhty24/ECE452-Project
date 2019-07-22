package com.morpheme.palmpiano;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.morpheme.palmpiano.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class ScoreLabelActor extends Actor implements EventListener {

    private HashSet<Event.EventType> monitoredEvents;
//    private long score;
    private float accuracy;
    private BitmapFont bmFont;
    OrthographicCamera camera;

    private static Texture perfectLine;
    private static Texture goodLine;

    public ScoreLabelActor() {
//        this.score = 0;
        this.accuracy = 1.0f;
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

    public static void setTextures() {
        perfectLine = new Texture(Gdx.files.internal("t1.png"));
        goodLine = new Texture(Gdx.files.internal("t2.png"));
    }

    @Override
    public void draw(Batch batch, float alpha) {
        camera = (OrthographicCamera) this.getParent().getStage().getCamera();
        float posX = camera.position.x - camera.viewportWidth / 2;
        String accuracyString = String.format("Accuracy: %.2f", accuracy * 100.0);
        // 100 is for the ToolbarGroup height, 48 is for the font size
        bmFont.draw(batch, accuracyString, 10 + posX, Gdx.graphics.getHeight() - Constants.WK_HEIGHT - 100 - 48);
        batch.draw(goodLine, 0, ScoreSystem.difficulty * 2, 15000, 3);
        batch.draw(perfectLine, 0, ScoreSystem.difficulty, 15000, 3);
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case UPDATE_SCORE:
//                score = (long) ((float []) event.getData())[0];
                accuracy = (Float) event.getData();
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
