package com.marketguardians.hexagonbuilder.model;

import java.util.ArrayList;

public class LocationPolygon {
    ArrayList<LocationCoordinate2D> points;
    String name;
    String objectId;

    public LocationPolygon(ArrayList<LocationCoordinate2D> points, String name, String objectId) {
        this.points = points;
        this.name = name;
        this.objectId = objectId;
    }

    public ArrayList<LocationCoordinate2D> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<LocationCoordinate2D> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
