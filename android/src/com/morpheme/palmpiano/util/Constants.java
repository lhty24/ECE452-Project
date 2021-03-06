package com.morpheme.palmpiano.util;

public final class Constants {
    public static final int BK_WIDTH = 90;
    public static final int BK_HEIGHT = 320;

    public static final int WK_WIDTH = 128;
    public static final int WK_HEIGHT = 512;
    public static final int WK_GAP = 2;

    public static final int BAR_BTN_SIZE = 60;
    public static final int BAR_BTN1 = 50;
    public static final int BAR_BTN2 = 150;
    public static final int BAR_BTN3 = 250;

    public static final int BAR_HEIGHT = 0;

    public static final byte MIDI_NOTE_OFF          = (byte) 0x80;
    public static final byte MIDI_NOTE_ON           = (byte) 0x90;
    public static final byte MIDI_OFFSET            = (byte) 21;
    public static final byte MIDI_PIANO_MEZZO_FORTE = (byte) 64;

    public enum PianoMode {
        MODE_MENU,
        MODE_COMPOSITION,
        MODE_PLAYBACK,
        MODE_GAME
    }

    // For intent destination via serializable extras
    public static final String MENU_LEADERBOARD = "leaderboard";
    public static final String MENU_SETTINGS = "settings";

    // Shift the camera X position on initialize so it is around C3/ C4 on the keyboard
    public static final int CAMERA_VIEWPORT_X_OFFSET = 2000;
    public static final String localPath = "/data/user/0/com.morpheme.palmpiano/files/midi/";
}
