package com.marketguardians.hexagonbuilder.model;

public class Region {
    private long id;
    private String name;

    public Region(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
