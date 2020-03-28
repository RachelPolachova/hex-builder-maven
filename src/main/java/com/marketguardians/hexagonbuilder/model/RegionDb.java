package com.marketguardians.hexagonbuilder.model;

public class RegionDb {
    private long id;
    private long code;
    private String name;
    private String path;

    public RegionDb(long id, long code, String name, String path) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
