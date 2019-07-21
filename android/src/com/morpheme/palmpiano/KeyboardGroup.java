package com.morpheme.palmpiano;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.morpheme.palmpiano.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class KeyboardGroup extends Group {
    // Order of notes in octave
    public String[] notes = {"A", "AS", "B", "C", "CS", "D", "DS", "E", "F", "FS", "G", "GS"};

    public static final int wkInterval = Constants.WK_WIDTH + Constants.WK_GAP;
    public static final int[] offsetMap = {
            0,
            wkInterval - Constants.BK_WIDTH / 2,
            wkInterval,
            2 * wkInterval,
            3 * wkInterval - Constants.BK_WIDTH / 2,
            3 * wkInterval,
            4 * wkInterval - Constants.BK_WIDTH / 2,
            4 * wkInterval,
            5 * wkInterval,
            6 * wkInterval - Constants.BK_WIDTH / 2,
            6 * wkInterval,
            7 * wkInterval - Constants.BK_WIDTH / 2
    };

    private List<PianoKey> wks;
    private List<PianoKey> bks;

    public KeyboardGroup() {
        super();

        this.setPosition(0, 0);

        wks = new ArrayList<>();
        bks = new ArrayList<>();

        for (int oc = 0; oc < 7; oc++) {
            boolean bk;
            for (int i = 0; i < notes.length; i++) {
                if ( i == 1 || i == 4|| i == 6 || i == 9 || i == 11 ) {
                    bk = true;
                } else {
                    bk = false;
                }
                PianoKey k = new PianoKey(bk, (byte) (Constants.MIDI_OFFSET + i + oc*12),  offsetMap[i] + oc* (7* wkInterval));
                k.setTouchable(Touchable.enabled);
                if (bk) {
                    bks.add(k);
                    continue;
                } else {
                    wks.add(k);
                }
            }
        }

        for (PianoKey wk : wks) {
            this.addActor(wk);
        }

        for (PianoKey bk : bks) {
            this.addActor(bk);
        }
    }

    // Given a midi byte, return the corresponding x position of the key in the game engine
    public static int getNotePosition(Byte midiByte) {
        int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
        int octave = shifted / 12;
        int keyIndex = shifted % 12;
        return ((7 * octave) * wkInterval + offsetMap[keyIndex]); //hardcoded
    }

    public static boolean getNoteBk(Byte midiByte) {
        int shifted = midiByte.intValue() - Constants.MIDI_OFFSET;
        int i = shifted % 12;
        return (i == 1 || i == 4 || i == 6 || i == 9 || i == 11); //hardcoded
    }

}
