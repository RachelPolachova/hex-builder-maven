package com.marketguardians.hexagonbuilder;

import org.json.simple.JSONArray;
import org.xml.sax.helpers.LocatorImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class App  {
    public static void main(String[] args) {
//        testGetDataFromJSON();
        testDataFromHandledJSON();
//        testDataFromJSONJHM();
//        testSusediaDoMatrixu();
    }

    public static void testSusediaDoMatrixu() {
        JSONReader jsonReader = new JSONReader();
        ArrayList<Location> locations = jsonReader.parseHandledJSON("kraje-spracovane.json");
        locations = HexagonBuilder.spracujBezSuseda(locations);
        HexagonBuilder builder = new HexagonBuilder();
        HexagonBuilder.checkniSusedov(locations);
        builder.susediaDoMatrixu(locations);
    }

    public static void testDataFromHandledJSON() {
        JSONReader jsonReader = new JSONReader();
        ArrayList<Location> locations = jsonReader.parseHandledJSON("moravskoslezsky-center.json");
        HexagonBuilder.checkniSusedov(HexagonBuilder.spracujBezSuseda(locations));

        Location mostNorth = HexagonBuilder.findMostNorth(locations);
        Location mostWest = HexagonBuilder.findMostWest(locations);
        Location mostEast = HexagonBuilder.findMostEast(locations);
        Location mostSouth = HexagonBuilder.findMostSouth(locations);

        System.out.println("NORTH: " + mostNorth.getName());
        System.out.println("WEST: " + mostWest.getName());
        System.out.println("EAST: " + mostEast.getName());
        System.out.println("SOUTH: " + mostSouth.getName());

        HexagonBuilder.buildFromArray(locations);
    }

    public static void hexagonPrinter(Hexagon start) {

        start.printPoints();
    }

    public static void testDataFromJSONJHM() {
        JSONReader jsonReader = new JSONReader();
        ArrayList<RUIANLocation> ruianLocations = jsonReader.locations;
        jsonReader.readOnlyRefWith("kraje-hranice.geojson", "CZ080");
//        jsonReader.read("jednotlive-kraje.geojson");
        ArrayList<Location> locations = new ArrayList<>();
        ruianLocations.forEach(location -> locations.add(new Location(location.getName(), location.getId(), location.getCenter(), location.getNeighboursIds())));
        jsonReader.write("moravskoslezsky-center.json", locations);
    }

    public static void testGetDataFromJSON() {
        JSONReader jsonReader = new JSONReader();

        jsonReader.read("kraje.geojson");
        ArrayList<RUIANLocation> ruianLocations = jsonReader.locations;
        System.out.println("Locations: " + ruianLocations.size());
        System.out.println("----------------------------");
        ArrayList<Location> locations = new ArrayList<>();
        ruianLocations.forEach(location -> locations.add(new Location(location.getName(), location.getId(), location.getCenter(), location.getNeighboursIds())));
        locations.forEach(l -> {
            System.out.println("-----" + l.getName() + ", center: " + l.getCenterLocation());
            l.printNeighbours();
        });
        jsonReader.write("kraje-spracovane-center.json", locations);
    }
}
