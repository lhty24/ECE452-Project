package com.morpheme.palmpiano;

import com.badlogic.gdx.Game;
import com.morpheme.palmpiano.screen.GameScreen;

public class PalmPiano extends Game {
	
	@Override
	public void create () {
		setScreen(new GameScreen());
	}
}
