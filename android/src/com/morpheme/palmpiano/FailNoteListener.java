package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.midi.Note;

import java.util.HashSet;
import java.util.Set;

public class FailNoteListener implements EventListener {
    private Stage stage;
    private GameVisualsGroup gameGroup;
    private HashSet<Event.EventType> monitoredEvents;

    public FailNoteListener() {
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.NEW_STAGE);
        monitoredEvents.add(Event.EventType.FAIL_NOTE);
    }

    private void createFailNote(byte note) {
        FailNoteActor failNote = new FailNoteActor(note);
        try {
            gameGroup.getGameVisualsMutex().acquire();
            gameGroup.addActor(failNote);
            gameGroup.getGameVisualsMutex().release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case NEW_STAGE:
                FailNoteActor.setTextures();
                this.stage = (Stage) event.getData();
                this.gameGroup = stage.getRoot().findActor("gameGroup");
                break;
            case FAIL_NOTE:
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
