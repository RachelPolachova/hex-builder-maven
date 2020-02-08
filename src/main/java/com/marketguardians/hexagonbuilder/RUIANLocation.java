package com.marketguardians.hexagonbuilder;

import java.util.ArrayList;

public class RUIANLocation {
    private ArrayList<LocationCoordinate2D> points = new ArrayList<>();
    private ArrayList<String> neighboursIds = new ArrayList<>();
    private String name;
    private String id;

    public RUIANLocation(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public void addPoint(LocationCoordinate2D coordinates) {
        points.add(coordinates);
    }

    public void printPoints() {
        points.forEach(p -> {
            System.out.println(p.getLongitude() + ", " + p.getLatitude());
        });
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<LocationCoordinate2D> getPoints() {
        return points;
    }

    public void addNeighboursId(String id) {
        neighboursIds.add(id);
    }

    public void printNeighbours() {
        neighboursIds.forEach(n -> {
            System.out.println(n);
        });
    }

    public ArrayList<String> getNeighboursIds() {
        return neighboursIds;
    }

    public String getId() {
        return id;
    }
}
