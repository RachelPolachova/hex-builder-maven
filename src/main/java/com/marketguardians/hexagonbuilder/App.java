package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.concaveHullFromProf.ConcaveHull;
import com.marketguardians.hexagonbuilder.model.LocationCoordinate2D;
import com.marketguardians.hexagonbuilder.model.LocationPolygon;
import com.marketguardians.hexagonbuilder.model.Region;
import com.marketguardians.hexagonbuilder.model.RegionToJson;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class App {
    public static void main(String[] args) {
//        vsetkoNaraz();
//        spracujGeojsonKrajov();
//        spracujGeojsonOrp();
        spracujGeojsonObce();
//        testConvextHull();
    }

    public static void vsetkoNaraz() {
        JSONReader reader = new JSONReader();
//        reader.fetchAllRegionsAndTheirRelations(402L);
//        reader.zacatOdOrp();
//        reader.createPathFiles();
        reader.checkPathDuplicates();
    }

    public static void spracujGeojsonKrajov() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujKrajGeojson();
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.getHexagonsFromRealPolygons(14, 11, polygons);
        ArrayList<RegionToJson> regions = new ArrayList<>();
        hexagons.forEach((key, value) -> {
            regions.add(new RegionToJson(key, value));
        });
        checkCovers(regions);
        regions.forEach(reg -> {
            System.out.print(reg.getHexagons().get(0).getName() + ": ");
            getCenter(reg.getBorder()).print();
        });
        reader.hexIntoJsonArray(regions, "db-kraje-start-offset.json", "kraje-paths.json");
    }

    public static void spracujGeojsonOrp() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujViaceroGeojsonSuborov(10, 200, "orp");
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.getHexagonsFromRealPolygons(5, 3.9285714285, polygons);
        ArrayList<RegionToJson> regions = new ArrayList<>();
        checkCovers(regions);
        hexagons.forEach((key, value) -> {
            regions.add(new RegionToJson(key, value));
        });
        reader.hexIntoJsonArray(regions, "db-orp-5x3_928.json", "orp-paths.json");
    }

    private static void checkCovers(ArrayList<RegionToJson> regions) {
        regions.forEach(region -> {
            Polygon polygon = getPolygonFromRegion(region);
            for (RegionToJson regionToJson : regions) {
                Polygon other = getPolygonFromRegion(regionToJson);
                if (!region.equals(regionToJson)) {
                    if (polygon.contains(other)) {
                        System.out.println("COVERS: " + region.getHexagons().get(0).getName() + " " + regionToJson.getHexagons().get(0).getName());
                        region.getHoles().add(regionToJson.getBorder());
                    }
                }
            }
        });
    }

    private static Polygon getPolygonFromRegion(RegionToJson region) {
        GeometryFactory factory = new GeometryFactory();
        ArrayList<Coordinate> coordinateArrayList = new ArrayList<>();
        region.getBorder().forEach(point -> {
            Coordinate coordinate = new Coordinate(point.getLongitude(), point.getLatitude());
            coordinateArrayList.add(coordinate);
        });
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinateArrayList.toArray(new Coordinate[0]));
        LinearRing linearRing = new LinearRing(coordinateSequence, factory);
        return new Polygon(linearRing, new LinearRing[0], factory);
    }

    public static void spracujGeojsonObce() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujViaceroGeojsonSuborov(10, 6250, "obec");
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.oneHexPerRegion(polygons);
        AtomicInteger i = new AtomicInteger();
        hexagons.forEach((key, value) -> {
            i.addAndGet(value.size());
        });

        System.out.println("I: " + i.intValue() + " hashmap size: " + hexagons.size());
        ArrayList<RegionToJson> regions = new ArrayList<>();
        hexagons.forEach((key, value) -> {
            regions.add(new RegionToJson(key, value));
        });

        reader.hexIntoJsonArray(regions, "db-obec-1hex.json", "obec-paths.json");
    }

//    private static void pointsInOrp() {
//        JSONReader reader = new JSONReader();
//        ArrayList<LocationPolygon> polygonsOrp = reader.spracujViaceroGeojsonSuborov(10, 200, "orp");
//        ArrayList<LocationPolygon> polygonsObce = reader.spracujViaceroGeojsonSuborov(10, 6250, "obec");
//    }

    public static LocationCoordinate2D getCenter(ArrayList<LocationCoordinate2D> points) {
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
