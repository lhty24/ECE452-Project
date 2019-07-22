package com.morpheme.palmpiano.util;

import com.badlogic.gdx.Gdx;

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
//    public static final int BAR_BTN_RIGHT = Gdx.graphics.getWidth()-1;
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
}
