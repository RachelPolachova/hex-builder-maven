package com.marketguardians.hexagonbuilder;

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
    private String id;

    private Optional<Hexagon> topLeftHex = Optional.empty();
    private Optional<Hexagon> topRightHex = Optional.empty();
    private Optional<Hexagon> leftHex = Optional.empty();
    private Optional<Hexagon> rightHex = Optional.empty();
    private Optional<Hexagon> bottomLeftHex = Optional.empty();
    private Optional<Hexagon> bottomRightHex = Optional.empty();

    public Hexagon(LocationCoordinate2D topRightPoint, LocationCoordinate2D rightPoint, LocationCoordinate2D bottomRightPoint, LocationCoordinate2D bottomLeftPoint, LocationCoordinate2D leftPoint, LocationCoordinate2D topLeftPoint, String name, String id) {
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

    public void addToBottomLeft(Location location) {
        if (bottomLeftHex.isPresent()) {
            Hexagon bl = bottomLeftHex.get();
            System.out.println("Bottom left wasn't free in: " + name + " there is: " + bl.name);
            while (bl.bottomLeftHex.isPresent()) {
                System.out.println("Old bl: " + bl.getName() + " new bl: " + bl.bottomLeftHex.get().getName());
                bl = bl.bottomLeftHex.get();
            }
            System.out.println("Found free in: " + bl.name + " for: " + location.getName());
            Hexagon bottomLeft = HexagonBuilder.buildBottomLeftHex(location.getName(), bl.getBottomLeftPoint(), location.getId());
            bottomLeft.topRightHex = Optional.of(this);
            bl.bottomLeftHex = Optional.of(bottomLeft);
//            HexagonBuilder.handleNeighbours(bl.bottomLeftHex.get());
        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon bottomLeft = HexagonBuilder.buildBottomLeftHex(location.getName(), bottomLeftPoint, location.getId());
            bottomLeft.topRightHex = Optional.of(this);
            this.bottomLeftHex = Optional.of(bottomLeft);
//            HexagonBuilder.handleNeighbours(bottomLeftHex.get());
        }
    }

    public void addToLeft(Location location) {
        if (leftHex.isPresent()) {
            Hexagon left = leftHex.get();
            System.out.println("Left wasn't free in: " + name + " there is: " + left.name);
            while (left.leftHex.isPresent()) {
                System.out.println("Old bl: " + left.getName() + " new bl: " + left.leftHex.get().getName());
                left = left.leftHex.get();
            }
            System.out.println("Found free in: " + left.name + " for: " + location.getName());
            Hexagon newLeft = HexagonBuilder.buildLeftHex(location.getName(), left.getLeftPoint(), location.getId());
            newLeft.rightHex = Optional.of(this);
            left.leftHex = Optional.of(newLeft);
//            HexagonBuilder.handleNeighbours(left.leftHex.get());

        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon leftHex = HexagonBuilder.buildLeftHex(location.getName(), leftPoint, location.getId());
            leftHex.rightHex = Optional.of(this);
            this.leftHex = Optional.of(leftHex);
//            HexagonBuilder.handleNeighbours(this.leftHex.get());
        }
    }

    public ArrayList<LocationCoordinate2D> getPoints() {
        ArrayList<LocationCoordinate2D> points = new ArrayList<>();
        points.add(topRightPoint);
        points.add(rightPoint);
        points.add(bottomRightPoint);
        points.add(bottomLeftPoint);
        points.add(leftPoint);
        points.add(topLeftPoint);
        return points;
    }

    public void addToTopLeft(Location location) {
        if (topLeftHex.isPresent()) {
            Hexagon topLeft = topLeftHex.get();
            System.out.println("Top left wasn't free in: " + name + " there is: " + topLeft.name);
            while (topLeft.topLeftHex.isPresent()) {
                System.out.println("Old bl: " + topLeft.getName() + " new bl: " + topLeft.topLeftHex.get().getName());
                topLeft = topLeft.topLeftHex.get();
            }
            System.out.println("Found free in: " + topLeft.name + " for: " + location.getName());
            Hexagon newTopLeft = HexagonBuilder.buildTopLeft(location.getName(), topLeft.getTopLeftPoint(), location.getId());
            newTopLeft.bottomRightHex = Optional.of(this);
            topLeft.topLeftHex = Optional.of(newTopLeft);
//            HexagonBuilder.handleNeighbours(topLeft.topLeftHex.get());
        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon leftHex = HexagonBuilder.buildTopLeft(location.getName(), topLeftPoint, location.getId());
            leftHex.bottomRightHex = Optional.of(this);
            this.topLeftHex = Optional.of(leftHex);
//            HexagonBuilder.handleNeighbours(this.topLeftHex.get());

        }
    }

    public void addToTopRight(Location location) {
        if (topRightHex.isPresent()) {
            Hexagon topRight = topRightHex.get();
            System.out.println("Top right wasn't free in: " + name + " there is: " + topRight.name);
            while (topRight.topRightHex.isPresent()) {
                System.out.println("Old bl: " + topRight.getName() + " new bl: " + topRight.topRightHex.get().getName());
                topRight = topRight.topRightHex.get();
            }
            System.out.println("Found free in: " + topRight.name + " for: " + location.getName());
            Hexagon newTopRight = HexagonBuilder.buildTopRight(location.getName(), topRight.getTopRightPoint(), location.getId());
            newTopRight.bottomLeftHex = Optional.of(this);
            topRight.topRightHex = Optional.of(newTopRight);
//            HexagonBuilder.handleNeighbours(topRight.topRightHex.get());
        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon topRight = HexagonBuilder.buildTopRight(location.getName(), topRightPoint, location.getId());
            topRight.bottomLeftHex = Optional.of(this);
            this.topRightHex = Optional.of(topRight);
//            HexagonBuilder.handleNeighbours(this.topRightHex.get());
        }
    }

    public void addToRight(Location location) {
        if (rightHex.isPresent()) {
            Hexagon right = rightHex.get();
            System.out.println("Right wasn't free in: " + name + " there is: " + right.name);
            while (right.rightHex.isPresent()) {
                System.out.println("Old bl: " + right.getName() + " new bl: " + right.rightHex.get().getName());
                right = right.rightHex.get();
            }
            System.out.println("Found free in: " + right.name + " for: " + location.getName());
            Hexagon newRight = HexagonBuilder.buildRight(location.getName(), right.getRightPoint(), location.getId());
            newRight.leftHex = Optional.of(this);
            right.rightHex = Optional.of(newRight);
//            HexagonBuilder.handleNeighbours(right.rightHex.get());

        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon right = HexagonBuilder.buildRight(location.getName(), rightPoint, location.getId());
            right.leftHex = Optional.of(this);
            this.rightHex = Optional.of(right);
//            HexagonBuilder.handleNeighbours(this.rightHex.get());

        }
    }

    public void addToBottomRight(Location location) {
        if (bottomRightHex.isPresent()) {
            Hexagon bottomRight = bottomRightHex.get();
            System.out.println("Bottom right wasn't free in: " + name + " there is: " + bottomRight.name);
            while (bottomRight.bottomRightHex.isPresent()) {
                System.out.println("Old bl: " + bottomRight.getName() + " new bl: " + bottomRight.bottomRightHex.get().getName());
                bottomRight = bottomRight.bottomRightHex.get();
            }
            System.out.println("Found free in: " + bottomRight.name + " for: " + location.getName());
            Hexagon newBottomRight = HexagonBuilder.buildBottomRight(location.getName(), bottomRight.getBottomRightPoint(), location.getId());
            newBottomRight.topLeftHex = Optional.of(this);
            bottomRight.bottomRightHex = Optional.of(newBottomRight);
//            HexagonBuilder.handleNeighbours(bottomRight.bottomRightHex.get());
        } else {
            System.out.println("Left was free in: " + name + " for: " + location.getName());
            Hexagon bottomRight = HexagonBuilder.buildBottomRight(location.getName(), bottomRightPoint, location.getId());
            bottomRight.topLeftHex = Optional.of(this);
            this.bottomRightHex = Optional.of(bottomRight);
//            HexagonBuilder.handleNeighbours(this.bottomRightHex.get());
        }
    }

    public void addToBottom(Location location, boolean preferedRight) {
        System.out.println("!!!!!! ADDING TO BOTTOM !!!!" + location.getName() + " to: " + name);
        if (bottomRightHex.isPresent()) {
            System.out.println("RB is present: " + bottomRightHex.get().name);
            bottomRightHex.get().addToBottom(location, false);
        } else if (bottomLeftHex.isPresent()) {
            System.out.println("LB is present: " + bottomLeftHex.get().name);
            bottomLeftHex.get().addToBottom(location, true);
        } else {
            if (preferedRight) {
                System.out.println("!! non of bottom L/R were present, adding as BR");
                addToBottomRight(location);
            } else {
                System.out.println("!! non of bottom L/R were present, adding as BL");
                addToBottomLeft(location);
            }
        }
    }

    public void addToTop(Location location) {
        System.out.println("!!!!!! ADDING TO TOP !!!!" + location.getName() + " to: " + name);
        if (topRightHex.isPresent()) {
            topRightHex.get().addToTop(location);
        } else if (topLeftHex.isPresent()) {
            topLeftHex.get().addToTop(location);
        } else {
            System.out.println("!! non of bottom L/R were present, adding as TR");
            addToTopRight(location);
        }
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

    public Optional<Hexagon> getTopLeftHex() {
        return topLeftHex;
    }

    public Optional<Hexagon> getTopRightHex() {
        return topRightHex;
    }

    public Optional<Hexagon> getLeftHex() {
        return leftHex;
    }

    public Optional<Hexagon> getRightHex() {
        return rightHex;
    }

    public Optional<Hexagon> getBottomLeftHex() {
        return bottomLeftHex;
    }

    public Optional<Hexagon> getBottomRightHex() {
        return bottomRightHex;
    }

    public void setTopLeftHex(Optional<Hexagon> topLeftHex) {
        this.topLeftHex = topLeftHex;
    }

    public void setTopRightHex(Optional<Hexagon> topRightHex) {
        this.topRightHex = topRightHex;
    }

    public void setLeftHex(Optional<Hexagon> leftHex) {
        this.leftHex = leftHex;
    }

    public void setRightHex(Optional<Hexagon> rightHex) {
        this.rightHex = rightHex;
    }

    public void setBottomLeftHex(Optional<Hexagon> bottomLeftHex) {
        this.bottomLeftHex = bottomLeftHex;
    }

    public void setBottomRightHex(Optional<Hexagon> bottomRightHex) {
        this.bottomRightHex = bottomRightHex;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
