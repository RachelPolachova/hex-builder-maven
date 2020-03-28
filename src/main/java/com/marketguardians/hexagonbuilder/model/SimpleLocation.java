package com.marketguardians.hexagonbuilder.model;

public class SimpleLocation {
    private String name;
    private long id;

    public SimpleLocation(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
