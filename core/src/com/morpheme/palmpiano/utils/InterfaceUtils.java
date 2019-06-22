package com.morpheme.palmpiano.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.List;

public class InterfaceUtils {

    public static World createWorld() {
        return new World(Constants.WORLD_GRAVITY, true);
    }

    public static List<Body> createKeyboard(World world) {
        BodyDef bodyDef = new BodyDef();

        // White keys
        List<Body> kb = new ArrayList<Body>();
        for(int i = 0; i < 11; i++) {
            bodyDef.position.set(new Vector2(i*Constants.WKEY_WIDTH, 0));
            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(Constants.WKEY_WIDTH / 2, Constants.WKEY_HEIGHT / 2);
            body.createFixture(shape, Constants.KEY_DENSITY);
            shape.dispose();
            kb.add(body);
        }

        // Black keys
        for(int i = 0; i < 11; i++) {
            if (i == 0 || i == 4 || i == 7 || i == 11 )
                continue;
            bodyDef.position.set(new Vector2(i*Constants.WKEY_WIDTH-(Constants.BKEY_WIDTH), (Constants.WKEY_HEIGHT-Constants.BKEY_HEIGHT)/2f));
            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(Constants.BKEY_WIDTH / 2, Constants.BKEY_HEIGHT / 2);
            body.createFixture(shape, Constants.KEY_DENSITY);
            shape.dispose();
            kb.add(body);
        }


        return kb;
    }

}
