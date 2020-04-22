package com.marketguardians.hexagonbuilder.model;

import java.util.Objects;

public class LocationCoordinate2D implements Comparable<LocationCoordinate2D> {
    private Double longitude; // zemepisná dĺžka
    private Double latitude; // zemepisná šírka

    public LocationCoordinate2D(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double distance(LocationCoordinate2D that) {
        final int R = 6371; // polomer Zeme v kilometroch
        double deltaLat = Math.toRadians(latitude - that.latitude);
        double deltaLong = Math.toRadians(longitude - that.longitude);

        // Haversine metóda
        double a = (Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)) +
                (Math.cos(Math.toRadians(latitude))) *
                        (Math.cos(Math.toRadians(that.latitude))) *
                        (Math.sin(deltaLong / 2)) *
                        (Math.sin(deltaLong / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public LocationCoordinate2D getNewLocation(double bearing, double distance) {
        double brng = Math.toRadians(bearing); // smer v radiánoch
        final int R = 6371; // polomer Zeme v kilometroch
        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance/R) + Math.cos(lat1) * Math.sin(distance/R) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(distance/R)*Math.cos(lat1), Math.cos(distance/R)-Math.sin(lat1)*Math.sin(lat2));

        return new LocationCoordinate2D(Math.toDegrees(lon2), Math.toDegrees(lat2));
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void print() {
        System.out.println(longitude + ", " + latitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public int compareTo(LocationCoordinate2D other) {
        if (longitude != other.getLongitude()) {
            return Double.compare(longitude, other.longitude);
        } else {
            return Double.compare(latitude, other.latitude);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }

    public Double getX() {
        return longitude;
    }

    public Double getY() {
        return latitude;
    }
}
