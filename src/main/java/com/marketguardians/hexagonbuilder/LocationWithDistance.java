package com.marketguardians.hexagonbuilder;

public class LocationWithDistance {
    private Location location;
    private Double distance;

    public LocationWithDistance(Location location, Double distance) {
        this.location = location;
        this.distance = distance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
