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

    public LocationCoordinate2D getCenter() {
        LocationCoordinate2D center = new LocationCoordinate2D(0, 0);
//        Point2D centroid = {0, 0};
        double signedArea = 0.0;
        double x0 = 0.0; // Current vertex X
        double y0 = 0.0; // Current vertex Y
        double x1 = 0.0; // Next vertex X
        double y1 = 0.0; // Next vertex Y
        double a = 0.0;  // Partial signed area

        // For all vertices except last
        int i=0;
        for (i=0; i<points.size()-1; ++i)
        {
            x0 = points.get(i).getLongitude();
            y0 = points.get(i).getLatitude();
            x1 = points.get(i + 1).getLongitude();
            y1 = points.get(i + 1).getLatitude();
            a = x0*y1 - x1*y0;
            signedArea += a;
            center.setLongitude(center.getLongitude() + (x0 + x1)*a);
            center.setLatitude(center.getLatitude() + (y0 + y1)*a);
        }

        // Do last vertex separately to avoid performing an expensive
        // modulus operation in each iteration.
        x0 = points.get(i).getLongitude();
        y0 = points.get(i).getLatitude();
        x1 = points.get(0).getLongitude();
        y1 = points.get(0).getLatitude();
        a = x0*y1 - x1*y0;
        signedArea += a;
        center.setLongitude(center.getLongitude() + (x0 + x1)*a);
        center.setLatitude(center.getLatitude() + (y0 + y1)*a);

        signedArea *= 0.5;
        center.setLongitude(center.getLongitude() / (6.0*signedArea));
        center.setLatitude(center.getLatitude() / (6.0*signedArea));

        return center;
    }
}
