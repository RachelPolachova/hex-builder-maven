package com.marketguardians.hexagonbuilder;

import java.util.ArrayList;

public class Location {
    private String name;
    private String id; //ref:NUTS
    private LocationCoordinate2D centerLocation;
    private ArrayList<String> neighboursIds;

    public Location(String name, String id, LocationCoordinate2D centerLocation, ArrayList<String> neighboursIds) {
        this.name = name;
        this.id = id;
        this.centerLocation = centerLocation;
        this.neighboursIds = neighboursIds;
    }

    public Location(String name, String id, LocationCoordinate2D centerLocation) {
        this.name = name;
        this.id = id;
        this.centerLocation = centerLocation;
    }

    public String getName() {
        return name;
    }

    public LocationCoordinate2D getCenterLocation() {
        return centerLocation;
    }

    public void setCenterLocation(LocationCoordinate2D centerLocation) {
        this.centerLocation = centerLocation;
    }

    public ArrayList<String> getNeighboursIds() {
        return neighboursIds;
    }

    public void printNeighbours() {
        neighboursIds.forEach(n -> {
            System.out.println(n);
        });
    }

    public void addNeighbour(String id) {
        neighboursIds.add(id);
    }

    public String getId() {
        return id;
    }
}
