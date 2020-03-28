package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.Location;
import com.marketguardians.hexagonbuilder.model.LocationCoordinate2D;

import java.util.ArrayList;
import java.util.Optional;

public class Hexagon {
    private LocationCoordinate2D topRightPoint;
    private LocationCoordinate2D rightPoint;
    private LocationCoordinate2D bottomRightPoint;
    private LocationCoordinate2D bottomLeftPoint;
    private LocationCoordinate2D leftPoint;
    private LocationCoordinate2D topLeftPoint;
    private String name;
    private long id;
    private String objectId;

    public Hexagon(LocationCoordinate2D topRightPoint, LocationCoordinate2D rightPoint, LocationCoordinate2D bottomRightPoint, LocationCoordinate2D bottomLeftPoint, LocationCoordinate2D leftPoint, LocationCoordinate2D topLeftPoint, String name, long id) {
        this.topRightPoint = topRightPoint;
        this.rightPoint = rightPoint;
        this.bottomRightPoint = bottomRightPoint;
        this.bottomLeftPoint = bottomLeftPoint;
        this.leftPoint = leftPoint;
        this.topLeftPoint = topLeftPoint;
        this.name = name;
        this.id = id;
    }


    public void printPoints() {
        System.out.println("Printing: " + name);
        System.out.println(topRightPoint.getLongitude() + ", " + topRightPoint.getLatitude());
        System.out.println(rightPoint.getLongitude() + ", " + rightPoint.getLatitude());
        System.out.println(bottomRightPoint.getLongitude() + ", " + bottomRightPoint.getLatitude());
        System.out.println(bottomLeftPoint.getLongitude() + ", " + bottomLeftPoint.getLatitude());
        System.out.println(leftPoint.getLongitude() + ", " + leftPoint.getLatitude());
        System.out.println(topLeftPoint.getLongitude() + ", " + topLeftPoint.getLatitude());
        System.out.println(topRightPoint.getLongitude() + ", " + topRightPoint.getLatitude()); // ONCE AGAIN!
    }

    public ArrayList<LocationCoordinate2D> getPoints() {
        ArrayList<LocationCoordinate2D> points = new ArrayList<>();
        points.add(topRightPoint);
        points.add(rightPoint);
        points.add(bottomRightPoint);
        points.add(bottomLeftPoint);
        points.add(leftPoint);
        points.add(topLeftPoint);
        points.add(topRightPoint);
        return points;
    }

    public LocationCoordinate2D getTopRightPoint() {
        return topRightPoint;
    }

    public LocationCoordinate2D getRightPoint() {
        return rightPoint;
    }

    public LocationCoordinate2D getBottomRightPoint() {
        return bottomRightPoint;
    }

    public LocationCoordinate2D getBottomLeftPoint() {
        return bottomLeftPoint;
    }

    public LocationCoordinate2D getLeftPoint() {
        return leftPoint;
    }

    public LocationCoordinate2D getTopLeftPoint() {
        return topLeftPoint;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
