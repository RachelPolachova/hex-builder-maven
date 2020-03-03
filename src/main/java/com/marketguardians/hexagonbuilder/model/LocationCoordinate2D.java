package com.marketguardians.hexagonbuilder.model;

public class LocationCoordinate2D {
    private double longitude;
    private double latitude;

    public LocationCoordinate2D(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double distance(LocationCoordinate2D that) {
        final int R = 6371;
        double latDistance = Math.toRadians(latitude - that.latitude);
        double lngDistance = Math.toRadians(longitude - that.longitude);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(latitude))) *
                        (Math.cos(Math.toRadians(that.latitude))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double bearing(LocationCoordinate2D that) {
        double long1 = Math.toRadians(longitude);
        double long2 = Math.toRadians(that.longitude);
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(that.latitude);
        double dLon = (long2 - long1);

        double degree = Math.toDegrees(Math.atan2(Math.sin(dLon) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)));
//        double y = Math.sin(long2 - long1) * Math.cos(lat2);
//        double x = Math.cos(lat1) * Math.sin(lat2);
//        double brng = Math.atan2(y, x);
//        return Math.toDegrees(brng);
        if (degree >= 0) {
            return degree;
        } else {
            return 360 + degree;
        }
//        return degree;
    }

    public LocationCoordinate2D newLoc(double bear, double distance) {
        double brng = Math.toRadians(bear);
        final int R = 6371;
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
}
