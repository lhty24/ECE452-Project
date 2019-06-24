package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.Event;
import com.morpheme.palmpiano.EventBus;
import com.morpheme.palmpiano.EventListener;
import com.morpheme.palmpiano.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class RhythmBoxListener implements EventListener {
    private HashSet<Event.EventType> monitoredEvents;
    private Stage stage;

    public RhythmBoxListener(Stage stage) {
        this.stage = stage;
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.MIDI_DATA_GAMEPLAY);
        EventBus.getInstance().register(this);
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    @Override
    public void handleEvent(Event event) {
        System.out.println("RhythmBoxListener received event: " + event.toString());
        byte[] data;
        switch (event.getEventType()) {
            case MIDI_DATA_GAMEPLAY:
                data = (byte[]) event.getData();
                if ((((int) data[0]) & ((int) Constants.MIDI_NOTE_ON )) != 0 && data[2] != 0) {
                    createRhythmBox(data[1]);
                }
            default:
                break;
        }
    }

    private void createRhythmBox(byte note) {
        PalmPiano.RhythmBox rhythmBox = new PalmPiano.RhythmBox(true, note);
        stage.addActor(rhythmBox);
    }
}
