package com.marketguardians.hexagonbuilder;

import org.json.simple.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App  {
    public static void main(String[] args) {
//        testGetDataFromJSON();
        testDatafromHandledJSON();
    }

    public static void testDatafromHandledJSON() {
        JSONReader jsonReader = new JSONReader();
        ArrayList<Location> locations = jsonReader.parseHandledJSON("kraje-spracovane.json");
        locations.forEach(location -> {
            System.out.println("-----------");
            System.out.println(location.getName() + ": ");
            location.printNeighbours();
        });
        Hexagon start = HexagonBuilder.buildHexBasedOnNeighbours(locations);
        System.out.println("ALL POINTS:");
        start.printAllPoints();
    }

    public static void testGetDataFromJSON() {

        // TODO:
        /*
        Find location's neighbour
        Neighbours in Location class, but not array of Location again, because of nested neighbours etc.
        Maybe create ID for each neighbour?

         */

        JSONReader jsonReader = new JSONReader();

        jsonReader.read("kraje.geojson");
        ArrayList<RUIANLocation> ruianLocations = jsonReader.locations;
        System.out.println("Locations: " + ruianLocations.size());
        System.out.println("----------------------------");
        ArrayList<Location> locations = new ArrayList<>();
//        ruianLocations.forEach(l -> {
//            System.out.println("-----" + l.getName() + ": ");
//            l.printNeighbours();
//        });
        ruianLocations.forEach(location -> locations.add(new Location(location.getName(), location.getId(), getCenter(location.getPoints()), location.getNeighboursIds())));
        locations.forEach(l -> {
            System.out.println("-----" + l.getName() + ", center: " + l.getCenterLocation());
            l.printNeighbours();
        });
        jsonReader.write("kraje-spracovane.json", locations);
    }

    public static LocationCoordinate2D getCenter(ArrayList<LocationCoordinate2D> coords) {
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
        for (i=0; i<coords.size()-1; ++i)
        {
            x0 = coords.get(i).getLongitude();
            y0 = coords.get(i).getLatitude();
            x1 = coords.get(i + 1).getLongitude();
            y1 = coords.get(i + 1).getLatitude();
            a = x0*y1 - x1*y0;
            signedArea += a;
            center.setLongitude(center.getLongitude() + (x0 + x1)*a);
            center.setLatitude(center.getLatitude() + (y0 + y1)*a);
        }

        // Do last vertex separately to avoid performing an expensive
        // modulus operation in each iteration.
        x0 = coords.get(i).getLongitude();
        y0 = coords.get(i).getLatitude();
        x1 = coords.get(0).getLongitude();
        y1 = coords.get(0).getLatitude();
        a = x0*y1 - x1*y0;
        signedArea += a;
        center.setLongitude(center.getLongitude() + (x0 + x1)*a);
        center.setLatitude(center.getLatitude() + (y0 + y1)*a);
//        centroid.x += (x0 + x1)*a;
//        centroid.y += (y0 + y1)*a;

        signedArea *= 0.5;
        center.setLongitude(center.getLongitude() / (6.0*signedArea));
        center.setLatitude(center.getLatitude() / (6.0*signedArea));
//        centroid.x /= ;
//        centroid.y /= ;

        return center;
    }
}
