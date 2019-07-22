package com.morpheme.palmpiano;

import com.google.gson.Gson;
import com.morpheme.palmpiano.util.Constants;
import com.morpheme.palmpiano.util.LeaderboardData;
import com.morpheme.palmpiano.util.LeaderboardDataSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class LeaderboardFileIO implements EventListener {
    public static final String leaderboardDataFileName = "leaderboardDataSet.json";

    private PalmPiano.PalmPianoCallback ppCallback;
    private HashSet<Event.EventType> monitoredEvents;
    private Gson gson;
    private LeaderboardDataSet leaderboardDataSet;
    private String currentFileName;

    public LeaderboardFileIO() {
        gson = new Gson();
        this.leaderboardDataSet = new LeaderboardDataSet();
        currentFileName = null;
        loadDataSet();

        this.monitoredEvents = new HashSet<>();
        monitoredEvents.add(Event.EventType.NEW_MIDI_FILE);
        monitoredEvents.add(Event.EventType.FINAL_SCORE);
        monitoredEvents.add(Event.EventType.PP_CALLBACK);
    }

    public LeaderboardDataSet getLeaderboardDataSet() {
        return leaderboardDataSet;
    }

    public void setPPCallback(PalmPiano.PalmPianoCallback ppCallback) {
        this.ppCallback = ppCallback;
    }

    public void loadDataSet() {
        String dataString = null;
        File f1 = new File(Constants.localPath, leaderboardDataFileName);
        // FIXME: temporary constant false condition
        if (f1.exists()) {
            try {
                byte [] byteArray = new byte[32000];
                FileInputStream stream = new FileInputStream(f1.getPath());
                stream.read(byteArray);
                stream.close();
                dataString = new String(byteArray);
            }
            catch (Exception e) {
                System.err.println(e.toString());
            }

            System.out.println("dataString len: " + dataString.length() + "codeAt(1000): " + (int) dataString.charAt(1000) + ", data: " + dataString + ")");

            dataString = dataString.split("" + (char) 0)[0];

            leaderboardDataSet = gson.fromJson(dataString, LeaderboardDataSet.class);
            System.out.println(leaderboardDataSet + ", " + dataString);
        }
        else {
            leaderboardDataSet = new LeaderboardDataSet();
            LeaderboardData data;

            // FIXME: temporary
//            for (int i = 0; i < 10; i++) {
            data = new LeaderboardData();
            data.songName = "Test_Song.mid";
            data.scores.add(new Float(0.8430));
            data.scores.add(new Float(0.60));
            leaderboardDataSet.leaderboardData.add(data);
//            }
        }
    }

    public void writeToFile() {
        String jsonString = gson.toJson(leaderboardDataSet, LeaderboardDataSet.class);
        File f1 = new File(Constants.localPath, leaderboardDataFileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(f1);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public void updateDataSet(float score) {
        LeaderboardData dataToSort = null;
        boolean updated = false;
        for (Object dataObject : leaderboardDataSet.leaderboardData) {
            LeaderboardData data = (LeaderboardData) dataObject;
            if (data.songName.equals(currentFileName)) {
                data.scores.add(score);
                dataToSort = data;
                updated = true;
                break;
            }
        }
        if (!updated) {
            LeaderboardData data = new LeaderboardData();
            data.songName = currentFileName;
            data.scores.add(score);
            dataToSort = data;
            leaderboardDataSet.leaderboardData.add(data);
        }

        Collections.sort(dataToSort.scores, new Comparator<Float>() {
            @Override
            public int compare(Float o1, Float o2) {
                if (o1 < o2) return 1;
                if (o1 > o2) return -1;
                return 0;
            }
        });

        writeToFile();
    }

    @Override
    public void handleEvent(Event<?> event) {
        switch (event.getEventType()) {
            case NEW_MIDI_FILE:
                currentFileName = (String) event.getData();
                break;
            case FINAL_SCORE:
                updateDataSet((Float) event.getData());
                ppCallback.onGameEnd();
                break;
            case PP_CALLBACK:
                setPPCallback((PalmPiano.PalmPianoCallback) event.getData());
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
