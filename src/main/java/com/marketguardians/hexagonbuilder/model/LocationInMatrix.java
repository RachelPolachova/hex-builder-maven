package com.marketguardians.hexagonbuilder.model;

public class LocationInMatrix {
    private SimpleLocation location;
    private Position position;

    public LocationInMatrix(SimpleLocation location, Position position) {
        this.location = location;
        this.position = position;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public Position getPosition() {
        return position;
    }
}
