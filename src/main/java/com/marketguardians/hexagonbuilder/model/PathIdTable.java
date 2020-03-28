package com.marketguardians.hexagonbuilder.model;

public class PathIdTable {
    private String path;
    private long id;

    public PathIdTable(String path, long id) {
        this.path = path;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public long getId() {
        return id;
    }
}
