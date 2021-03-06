package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.morpheme.palmpiano.util.Constants;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class RhythmBoxListener implements EventListener {
    private HashSet<Event.EventType> monitoredEvents;
    private Stage stage;
    private GameVisualsGroup gameGroup;

    public RhythmBoxListener() {
        this.stage = null;
        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.NEW_STAGE);
        monitoredEvents.add(Event.EventType.PAUSE);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PLAY);
        monitoredEvents.add(Event.EventType.MIDI_FILE_PAUSE);
        monitoredEvents.add(Event.EventType.MIDI_DATA_GAMEPLAY);
    }

    @Override
    public Set<Event.EventType> getMonitoredEvents() {
        return monitoredEvents;
    }

    @Override
    public void handleEvent(Event event) {
        byte[] data;

        switch (event.getEventType()) {
            case NEW_STAGE:
                this.stage = (Stage) event.getData();
                this.gameGroup = this.stage.getRoot().findActor("gameGroup");
                RhythmBox.setTextures();
                break;
            case MIDI_FILE_PLAY:
                RhythmBox.setIsRunning(true);
                break;
            case MIDI_FILE_PAUSE:
                RhythmBox.setIsRunning(false);
                break;
            case PAUSE:
                RhythmBox.setIsRunning(false);
                break;
            case MIDI_DATA_GAMEPLAY:
                data = (byte[]) event.getData();
                if ((((int) data[0]) & ((int) Constants.MIDI_NOTE_ON )) != 0 && data[2] != 0) {
                    byte[] lengthBytes = new byte[data.length - 3];
                    for(int i = 3; i < data.length; i++) {
                        lengthBytes[i - 3] = data[i];
                    }
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.put(lengthBytes);
                    buffer.flip();

                    createRhythmBox(data[1], buffer.getLong());
                }
            default:
                break;
        }
    }

    private void createRhythmBox(byte note, long len) {
        RhythmBox rhythmBox = new RhythmBox(KeyboardGroup.getNoteBk(note), note, len);
        try {
            gameGroup.getGameVisualsMutex().acquire();
            gameGroup.addActor(rhythmBox);
            gameGroup.getGameVisualsMutex().release();
        }
        catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }
}
