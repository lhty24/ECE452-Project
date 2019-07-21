package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.midi.Note;

import java.util.HashSet;
import java.util.Set;

public class FailNoteListener implements EventListener {
    private Stage stage;
    private HashSet<Event.EventType> monitoredEvents;

    public FailNoteListener() {
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.NEW_STAGE);
        monitoredEvents.add(Event.EventType.PIANO_KEY_DOWN);
    }

    private void createFailNote(byte note) {
        FailNoteActor failNote = new FailNoteActor(note);
        stage.addActor(failNote);
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case NEW_STAGE:
                FailNoteActor.setTextures();
                this.stage = (Stage) event.getData();
                break;
            // TODO: currently X's on every piano key press; change to event from score system
            case PIANO_KEY_DOWN:
                createFailNote((Byte) event.getData());
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
