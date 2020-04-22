package com.marketguardians.hexagonbuilder.model;

import com.marketguardians.hexagonbuilder.Hexagon;
import com.marketguardians.hexagonbuilder.concaveHullFromProf.ConcaveHull;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;

public class RegionToJson {
    private ArrayList<Hexagon> hexagons;
    private ArrayList<LocationCoordinate2D> border;
    private String key;
    private ArrayList<ArrayList<LocationCoordinate2D>> holes;

    public RegionToJson(String key, ArrayList<Hexagon> hexagons) {
        this.hexagons = hexagons;
        this.key = key;
        this.holes = new ArrayList<>();
        this.border = getBorderCoordinates(hexagons);
    }

    private ArrayList<LocationCoordinate2D> getBorderCoordinates(ArrayList<Hexagon> hexagons) {
        if (hexagons.size() < 2) {
            return hexagons.get(0).getPoints();
        }
        GeometryFactory factory = new GeometryFactory();
        ArrayList<Polygon> polygonArrayList = new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<>();
        hexagons.forEach(hexagon -> {
            ArrayList<Coordinate> coordinateArrayList = new ArrayList<>();
            hexagon.getPoints().forEach(point -> {
                Coordinate coordinate = new Coordinate(point.getLongitude(), point.getLatitude());
                coordinateArrayList.add(coordinate);
            });
            CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinateArrayList.toArray(new Coordinate[0]));
            LinearRing linearRing = new LinearRing(coordinateSequence, factory);
            Polygon polygon = new Polygon(linearRing, new LinearRing[0], factory);
            Coordinate topRight = new Coordinate(hexagon.getTopRightPoint().getLongitude(), hexagon.getTopRightPoint().getLatitude());
            Coordinate topLeft = new Coordinate(hexagon.getTopLeftPoint().getLongitude(), hexagon.getTopLeftPoint().getLatitude());
            Double distance = topRight.distance(topLeft);
            Coordinate right = new Coordinate(hexagon.getRightPoint().getLongitude(), hexagon.getRightPoint().getLatitude());
            Coordinate bottomRight = new Coordinate(hexagon.getBottomRightPoint().getLongitude(), hexagon.getBottomRightPoint().getLatitude());
            double distanceRBr = right.distance(bottomRight);
            System.out.println(distance + " vs " + distanceRBr);
//            System.out.println(distance);
            distances.add(distance);
            polygonArrayList.add(polygon);
        });
        double max = 0;
        for (Double distance : distances) {
            if (distance > max) {
                max = distance;
            }
        }
        double distance = max;
        GeometryCollection geometryCollection = new GeometryCollection(polygonArrayList.toArray(new Polygon[0]), factory);

        polygonArrayList.get(0).contains(polygonArrayList.get(1));
        ConcaveHull concaveHull = new ConcaveHull(geometryCollection, distance);
        Geometry geometry = concaveHull.getConcaveHull();
        Coordinate[] coords =  geometry.getCoordinates();

        ArrayList<LocationCoordinate2D> coordinate2DArrayList = new ArrayList<>();
        for (Coordinate coord : coords) {
            coordinate2DArrayList.add(new LocationCoordinate2D(coord.x, coord.y));
        }
        return coordinate2DArrayList;
    }

    public ArrayList<Hexagon> getHexagons() {
        return hexagons;
    }

    public void setHexagons(ArrayList<Hexagon> hexagons) {
        this.hexagons = hexagons;
    }

    public ArrayList<LocationCoordinate2D> getBorder() {
        return border;
    }

    public void setBorder(ArrayList<LocationCoordinate2D> border) {
        this.border = border;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<ArrayList<LocationCoordinate2D>> getHoles() {
        return holes;
    }

    public void setHoles(ArrayList<ArrayList<LocationCoordinate2D>> holes) {
        this.holes = holes;
    }
}
