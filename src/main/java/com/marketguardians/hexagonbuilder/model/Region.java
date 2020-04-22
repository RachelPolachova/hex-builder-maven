package com.marketguardians.hexagonbuilder.model;

public class Region {
    private String id;
    private String name;
    private LocationCoordinate2D center;

    public Region(String id, String name, LocationCoordinate2D center) {
        this.id = id;
        this.name = name;
        this.center = center;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationCoordinate2D getCenter() {
        return center;
    }

    public void setCenter(LocationCoordinate2D center) {
        this.center = center;
    }
}
