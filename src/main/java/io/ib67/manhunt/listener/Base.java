package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;

public abstract class Base {
    private final ManHunt mh;

    public Base() {
        mh = ManHunt.getPlugin(ManHunt.class);
    }

    public ManHunt getMh() {
        return mh;
    }
}
