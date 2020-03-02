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
    }

//    14.465998948021674, 50.06678953590457
//            16.635192309849344, 49.08016775779745
//            12.753140666493925, 50.1740780106054
//            14.432289503348901, 49.093103716166425
//            14.549549521132452, 50.003605104079966
//            17.74737600927628, 49.220465871679764
//            13.845656298301213, 50.53040788842697
//            15.671506608486823, 49.413447495525105
//            14.998644402157764, 50.704957583192055
//            17.191056228799326, 49.77734278959563
//            16.191218435959232, 49.90127909303319
//            17.979367873745712, 49.82068143415316
//            15.866472981287625, 50.38362976103177
//            13.228010575306186, 49.59818625974606

    public static void testDataFromHandledJSON() {
        JSONReader jsonReader = new JSONReader();
        ArrayList<Location> locations = jsonReader.parseHandledJSON("kraje-spracovane.json");
        System.out.println("-----------");
        double height = 0;
        double width = 0;
        for (Location location: locations) {
//            System.out.println("-----------");
            System.out.println(location.getName());
            location.getCenterLocation().print();
            height += location.getCenterLocation().getLatitude();
            width += location.getCenterLocation().getLongitude();
//            System.out.println(location.getName() + ": ");
//            location.printNeighbours();
        }
//        HexagonBuilder hexagonBuilder = new HexagonBuilder();
//        hexagonBuilder.getHexArray(locations);
//        System.out.println("PRINTING COORDS!");
//        for (ArrayList<LocationCoordinate2D> points: hexagonBuilder.hexagons) {
//            for (LocationCoordinate2D coord: points) {
//                System.out.println(coord.getLongitude() + ", " + coord.getLatitude());
//            }
//        }
//        Hexagon start = HexagonBuilder.buildHexBasedOnNeighbours(locations);
//        System.out.println("ALL POINTS:");
//        hexagonPrinter(start);

//        HexagonBuilder.buildHexBasedOnNeighbours(locations);
//        System.out.println("Hexs: " + HexagonBuilder.hexs.size());
//        for (Hexagon hex: HexagonBuilder.hexs) {
//            hex.printPoints();
//        }

        Location mostNorth = HexagonBuilder.findMostNorth(locations);
        Location mostWest = HexagonBuilder.findMostWest(locations);
        Location mostEast = HexagonBuilder.findMostEast(locations);
        Location mostSouth = HexagonBuilder.findMostSouth(locations);

        System.out.println("NORTH: " + mostNorth.getName());
        System.out.println("WEST: " + mostWest.getName());
        System.out.println("EAST: " + mostEast.getName());
        System.out.println("SOUTH: " + mostSouth.getName());

//        System.out.println("spolu: " + height + " priemer: " + height/locations.size() + " rozdil: " + (mostNorth.getCenterLocation().getLatitude() - mostSouth.getCenterLocation().getLatitude()));
//        System.out.println("spolu: " + width + " priemer: " + width/locations.size() + " rozdil: " + (mostEast.getCenterLocation().getLongitude() - mostWest.getCenterLocation().getLongitude()));
        HexagonBuilder.buildFromArray(locations);
    }

    public static void hexagonPrinter(Hexagon start) {

        start.printPoints();
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
        jsonReader.write("kraje-spracovane.json", locations);
    }
}
