package com.marketguardians.hexagonbuilder.model;

public class Region {
    private String id;
    private String name;

    public Region(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
