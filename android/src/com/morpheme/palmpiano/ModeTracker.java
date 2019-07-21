package com.morpheme.palmpiano;

import com.morpheme.palmpiano.util.Constants;

public class ModeTracker {
    private static Constants.PianoMode mode;

    static public Constants.PianoMode getMode() {
        return mode;
    }

    static public void setMode(Constants.PianoMode new_mode) {
        mode = new_mode;
    }
}
