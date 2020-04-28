package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class HexagonBuilder {

//    private ArrayList<ArrayList<Optional<Hexagon>>> matrix = new ArrayList<>(new ArrayList<>());
    private ArrayList<ArrayList<Hexagon>> hexagonLayout = new ArrayList<>();

    private ArrayList<Hexagon> handledHexagons = new ArrayList<>();

    public HashMap<String, ArrayList<Hexagon>> oneHexPerRegion(ArrayList<LocationPolygon> polygons) {
        HashMap<String, ArrayList<Hexagon>> hashmap = new HashMap<>();
        HashMap<String, ArrayList<LocationPolygon>> polygonsByOrp = new HashMap<>();
        JSONReader jsonReader = new JSONReader();
        polygons.forEach(pol -> {
            String parentPath = jsonReader.findPath(pol.getObjectId(), "obec-paths.json");
            if (parentPath.isEmpty()) {
                System.out.println("ajajaj");
            } else {
                String path = parentPath.substring(0, 4);
                if (!polygonsByOrp.containsKey(path)) {
                    polygonsByOrp.put(path, new ArrayList<>());
                }
                polygonsByOrp.get(path).add(pol);
            }
        });

        polygonsByOrp.forEach((key, value) -> {
            int columns = (int) Math.round(Math.sqrt(value.size()));
            ArrayList<ArrayList<Hexagon>> hexMatrix = initHexMatrix(columns + 1, columns, value.get(0).getPoints().get(0), 5.0);
            for (int i=0; i < value.size(); i++) {
                Hexagon hexagon;
                if (i >= columns) {
                    int x = i;
                    int counter = 0;
                    System.out.println("getting: " + x + " columns: " + columns + " size: " + value.size());
                    while (x >= columns) {
                        x = x - columns;
                        counter++;
                    }
                    System.out.println("counter: " + counter + " x - 1: " + (x));
                    hexagon = hexMatrix.get(counter).get(x);
                } else {
                    hexagon = hexMatrix.get(0).get(i);
                }
                if (!hashmap.containsKey(value.get(i).getObjectId())) {
                    hashmap.put(value.get(i).getObjectId(), new ArrayList<>());
                }
                hexagon.setName(value.get(i).getName());
                hexagon.setId(Long.parseLong(value.get(i).getObjectId()));
                hashmap.get(value.get(i).getObjectId()).add(hexagon);
            }

            System.out.println("SQRT: " + key + ": " + (int) Math.sqrt(value.size()) + " for size: " + value.size());
        });
        System.out.println("hasmap size: " + polygonsByOrp.size());
        return hashmap;
    }

    public ArrayList<Region> getCenters(ArrayList<LocationPolygon> polygons) {
        ArrayList<Region> regions = new ArrayList<>();
        polygons.forEach(pol -> {
            regions.add(new Region(pol.getObjectId(), pol.getName(), getCenter(pol.getPoints())));
        });
        return regions;
    }

    public LocationCoordinate2D getCenter(ArrayList<LocationCoordinate2D> points) {
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

    public HashMap<String, ArrayList<Hexagon>> getHexagonsFromRealPolygons(double widthBetweenPoints, double heightBetweenPoints, ArrayList<LocationPolygon> polygons) {
        // 1. nájdi extrémne body
        LocationCoordinate2D mostEast = findMostEastCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostNorth = findMostNorthCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostWest = findMostWestCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostSouth = findMostSouthCoordFromPolygonArray(polygons);

        // 2. pridaj rezervu k extrémnym bodom
        LocationCoordinate2D newLocE = mostEast.getNewLocation(90, widthBetweenPoints);
        LocationCoordinate2D newLocW = mostWest.getNewLocation(270, widthBetweenPoints);
        LocationCoordinate2D newLocN = mostNorth.getNewLocation(0, widthBetweenPoints);
        LocationCoordinate2D newLocS = mostSouth.getNewLocation(180, widthBetweenPoints);

        // 3. rohové body
        LocationCoordinate2D matrixTopLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixTopRight = new LocationCoordinate2D(newLocE.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixBottomLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocS.getLatitude());

        // 4. výpočet počtu stĺpcov a riadkov
        double width = matrixTopLeft.distance(matrixTopRight);
        double height = matrixBottomLeft.distance(matrixTopLeft);
        int numberOfColumns = (int) (width / widthBetweenPoints);
        int numberOfRows = (int) (height / heightBetweenPoints);

        // 5. vytvorenie matice GPS bodov
        ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix = pointsMatrix(matrixTopLeft, numberOfColumns, numberOfRows, widthBetweenPoints, heightBetweenPoints);

        // 6. vytvorenie matice šesťuholníkov
        ArrayList<ArrayList<Hexagon>> hexMatrix = initHexMatrix(numberOfRows, numberOfColumns, matrixTopLeft, widthBetweenPoints / 1.5);

        // 7. hash mapa so vybranými šesťuholníkmi
        HashMap<String, ArrayList<Hexagon>> hexHasMap = new HashMap<>();

        // 8. for cyklus na kontrolu prítomnosti každého bodu v polygóne
        for (int i=0; i < pointsMatrix.size(); i++) {
            for (int j=0; j < pointsMatrix.get(i).size(); j++) {
                System.out.println("I: " + i + " /" + pointsMatrix.size() + " J: " + j + " /" + pointsMatrix.get(i).size());
                LocationCoordinate2D point = pointsMatrix.get(i).get(j);
                for (LocationPolygon polygon : polygons) {
                    if (GFGPointCheck.isInside(polygon.getPoints(), polygon.getPoints().size(), point)) {
                        Hexagon h = hexMatrix.get(i).get(j);
                        h.setName(polygon.getName());
                        h.setId(Long.parseLong(polygon.getObjectId()));

                        if (!hexHasMap.containsKey(polygon.getObjectId())) {
                            hexHasMap.put(polygon.getObjectId(), new ArrayList<>());
                        }
                        hexHasMap.get(polygon.getObjectId()).add(h);
                    }
                }
            }
        }
        return hexHasMap;
    }

    public void gpsPointsIsInPolygonHex(double vzdialenostJednehoBodu, double vyska, LocationPolygon locationPolygon) {

        LocationCoordinate2D mostEast = findMostEastCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostNorth = findMostNorthCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostWest = findMostWestCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostSouth = findMostSouthCoordinate(locationPolygon.getPoints());

        LocationCoordinate2D newLocE = mostEast.getNewLocation(90, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na vychod (* 2 je rezerva)
        LocationCoordinate2D newLocW = mostWest.getNewLocation(270, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na zapad
        LocationCoordinate2D newLocN = mostNorth.getNewLocation(0, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na sever
        LocationCoordinate2D newLocS = mostSouth.getNewLocation(180, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na juh
        LocationCoordinate2D matrixTopLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixTopRight = new LocationCoordinate2D(newLocE.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixBottomRight = new LocationCoordinate2D(newLocE.getLongitude(), newLocS.getLatitude());
        LocationCoordinate2D matrixBottomLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocS.getLatitude());
        System.out.println(
                "Most east: " + mostEast.getLongitude() + ", " + mostEast.getLatitude() + "\n" +
                        mostNorth.getLongitude() + ", " + mostNorth.getLatitude() + "\n" +
                        mostWest.getLongitude() + ", " + mostWest.getLatitude() + "\n" +
                        mostSouth.getLongitude() + ", " + mostSouth.getLatitude() + "\n" +
                        " new loc: " + newLocE.getLongitude() + ", " + newLocE.getLatitude() + "\n" +
                        newLocN.getLongitude() + ", " + newLocN.getLatitude() + "\n" +
                        newLocW.getLongitude() + ", " + newLocW.getLatitude() + "\n" +
                        newLocS.getLongitude() + ", " + newLocS.getLatitude() + "\n" +
                        matrixTopLeft.getLongitude() + ", " + matrixTopLeft.getLatitude() + "\n" +
                        matrixTopRight.getLongitude() + ", " + matrixTopRight.getLatitude() + "\n" +
                        matrixBottomRight.getLongitude() + ", " + matrixBottomRight.getLatitude() + "\n" +
                        matrixBottomLeft.getLongitude() + ", " + matrixBottomLeft.getLatitude()
        );

        double width = matrixTopLeft.distance(matrixTopRight);
        double height = matrixBottomLeft.distance(matrixTopLeft);

        int numberOfColumns = (int) (width / vzdialenostJednehoBodu);
        int numberOfRows = (int) (height / vyska);



        System.out.println("----" + numberOfColumns + " * " + numberOfRows);
        ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix = pointsMatrix(matrixTopLeft, numberOfColumns, numberOfRows, vzdialenostJednehoBodu, vyska);
        pointsMatrix.forEach(row -> {
            row.forEach(locationCoordinate2D -> {
//                System.out.println(locationCoordinate2D.getLongitude() + ", " + locationCoordinate2D.getLatitude());
            });
        });
        ArrayList<ArrayList<Hexagon>> hexMatrix = initHexMatrix(numberOfRows, numberOfColumns, matrixTopLeft, vzdialenostJednehoBodu / 1.5);
        hexMatrix.forEach(row -> {
//            row.forEach(Hexagon::printPoints);
        });

        GFGPointCheck pointChecker = new GFGPointCheck();

        for (int i = 0; i < pointsMatrix.size(); i++) {
            for (int j=0; j < pointsMatrix.get(i).size(); j++) {
                LocationCoordinate2D loc = pointsMatrix.get(i).get(j);
                if (GFGPointCheck.isInside(locationPolygon.getPoints(),locationPolygon.getPoints().size(),loc)) {
                    hexMatrix.get(i).get(j).printPoints();
                }
            }
        }
    }

    private ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix(LocationCoordinate2D matrixTopLeft, int numberOfColumns, int numberOfRows, double widthBetweenPoints, double heightBetweenPoints) {
        ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix = new ArrayList<>();
        LocationCoordinate2D previousValue = matrixTopLeft; // počiatočný bod
        for (int i = 0; i < numberOfRows; i++) {
            pointsMatrix.add(new ArrayList<>()); // nový riadok
            for (int j = 0; j < numberOfColumns; j++) {
                LocationCoordinate2D shiftedLoc = previousValue.getNewLocation(90, widthBetweenPoints); // posun na východ
                previousValue = shiftedLoc;
                if (i == 0 && j == 0) {
                    pointsMatrix.get(i).add(previousValue); // v prípade úplne prvého bodu pridaj topLeft
                } else {
                    pointsMatrix.get(i).add(shiftedLoc); // v opačnom prípade pridaj posunutý bod
                }
            }
            previousValue = previousValue.getNewLocation(180, heightBetweenPoints); // posun na juh (nový riadok)
            previousValue.setLongitude(matrixTopLeft.getLongitude());
        }
        return pointsMatrix;
    }

    private ArrayList<ArrayList<Hexagon>> initHexMatrix(int rows, int columns, LocationCoordinate2D startingPoint, double distance) {
        ArrayList<ArrayList<Hexagon>> matrix = new ArrayList<>();

        for (int i=0; i<rows; i++) {
            matrix.add(new ArrayList<>()); // nový riadok
            for (int j=0; j<columns; j++) {
                Hexagon newHex;

                if (i == 0 && j == 0) { // počiatočný šesťuholník
                    newHex = initHex(startingPoint, "", 1, distance);
                } else if (j==0) { // prvý stĺpec

                    Hexagon neighbourTop = matrix.get(i-1).get(0);
                    if (i%2 == 0) { // párna rada
                        newHex = buildBottomLeftHex("", neighbourTop.getBottomLeftPoint(), 1, distance);
                    } else { // nepárna rada
                        newHex = buildBottomRight("", neighbourTop, 1, distance);
                    }
                } else {
                    Hexagon neighbourLeft = matrix.get(i).get(j-1);
                    newHex = buildRight("", neighbourLeft.getRightPoint(), 1, distance);
                }
                matrix.get(i).add(newHex);
            }
        }
        return matrix;
    }

    private void initCustomLocMatrix(int rows, int columns) {
        double distance = 10;
        String chybneMeno = "chyba";
        long chybneId = 999;
        for (int i = 0; i < rows; i++) {
            hexagonLayout.add(new ArrayList<>());
            for (int j = 0; j < columns; j++) {
                if (i == 0 && j == 0) {
                    Hexagon init = initHex(new LocationCoordinate2D((float) 16.6113382, (float) 49.1922443), chybneMeno, chybneId, distance);
                    hexagonLayout.get(i).add(j, init);
                } else if (i == 0) {
                    Hexagon leftNeighbour = hexagonLayout.get(i).get(j - 1);
                    Hexagon newHex = buildRight(chybneMeno, leftNeighbour.getRightPoint(), chybneId, distance);
                    hexagonLayout.get(i).add(j, newHex);
                } else {
                    Hexagon topNeighbour = hexagonLayout.get(i - 1).get(j);
                    Hexagon newHex = buildBottomRight(chybneMeno, topNeighbour, chybneId, distance);
                    hexagonLayout.get(i).add(j, newHex);
                }
            }
        }
    }

    public static Hexagon buildBottomLeftHex(String name, LocationCoordinate2D bottomLeftPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(180.0, bottomLeftPoint, distance);
        return new Hexagon(coords.get(5), coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), name, id);
    }

    public static Hexagon buildLeftHex(String name, LocationCoordinate2D leftPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(240.0, leftPoint, distance);
        return new Hexagon(coords.get(4), coords.get(5), coords.get(0), coords.get(1), coords.get(2), coords.get(3), name, id);
    }

    public static Hexagon buildTopLeft(String name, LocationCoordinate2D topLeftPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(300.0, topLeftPoint, distance);
        return new Hexagon(coords.get(3), coords.get(4), coords.get(5), coords.get(0), coords.get(1), coords.get(2), name, id);
    }

    public static Hexagon buildTopRight(String name, LocationCoordinate2D topRightPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(0.0, topRightPoint, distance);
        return new Hexagon(coords.get(2), coords.get(3), coords.get(4), coords.get(5), coords.get(0), coords.get(1), name, id);
    }

    public static Hexagon buildRight(String name, LocationCoordinate2D rightPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(60.0, rightPoint, distance);
        return new Hexagon(coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5), coords.get(0), name, id);
    }

    public static Hexagon buildBottomRight(String name, Hexagon hexagon, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(120.0, hexagon.getBottomRightPoint(), distance);
        return new Hexagon(coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5), name, id);
    }


    public static ArrayList<LocationCoordinate2D> buildHex(Double startingBearing, LocationCoordinate2D start, double distance) {
        LocationCoordinate2D from = start;
        ArrayList<LocationCoordinate2D> coordinates = new ArrayList<>();
        coordinates.add(from);
        double b = startingBearing;
        for (int i = 1; i <= 6; i++) {
            LocationCoordinate2D next = from.getNewLocation(b, distance);
            coordinates.add(next);
            from = next;
            b += 60;
        }
        return coordinates;
    }

    public static Hexagon initHex(LocationCoordinate2D start, String name, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(120.0, start, distance);
        return new Hexagon(coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5), name, id);
    }

    private static Boolean lhsIsOnWest(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        if (lhs.getLongitude() - rhs.getLongitude() < 0) {
            return true;
        }
        return false;
    }

    private static Boolean lhsIsOnEast(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        if (lhs.getLongitude() - rhs.getLongitude() > 0) {
            return true;
        }
        return false;
    }

    private static Boolean lhsInOnNorth(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        if (lhs.getLatitude() - rhs.getLatitude() > 0) {
            return true;
        }
        return false;
    }

    private static Boolean lhsInOnSouth(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        return lhs.getLatitude() - rhs.getLatitude() < 0;
    }

    static Location findMostNorth(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l : locations) {
            if (lhsInOnNorth(l.getCenterLocation(), max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostSouth(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l : locations) {
            if (lhsInOnSouth(l.getCenterLocation(), max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostWest(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l : locations) {
            if (lhsIsOnWest(l.getCenterLocation(), max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostEast(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l : locations) {
            if (lhsIsOnEast(l.getCenterLocation(), max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostEastCoordFromPolygonArray(ArrayList<LocationPolygon> polygons) {
        LocationCoordinate2D max = polygons.get(0).getPoints().get(0);
        for (LocationPolygon polygon : polygons) {
            LocationCoordinate2D polMax = findMostEastCoordinate(polygon.getPoints());
            if (lhsIsOnEast(polMax, max)) {
                max = polMax;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostWestCoordFromPolygonArray(ArrayList<LocationPolygon> polygons) {
        LocationCoordinate2D max = polygons.get(0).getPoints().get(0);
        for (LocationPolygon polygon : polygons) {
            LocationCoordinate2D polMax = findMostWestCoordinate(polygon.getPoints());
            if (lhsIsOnWest(polMax, max)) {
                max = polMax;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostNorthCoordFromPolygonArray(ArrayList<LocationPolygon> polygons) {
        LocationCoordinate2D max = polygons.get(0).getPoints().get(0);
        for (LocationPolygon polygon : polygons) {
            LocationCoordinate2D polMax = findMostNorthCoordinate(polygon.getPoints());
            if (lhsInOnNorth(polMax, max)) {
                max = polMax;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostSouthCoordFromPolygonArray(ArrayList<LocationPolygon> polygons) {
        LocationCoordinate2D max = polygons.get(0).getPoints().get(0);
        for (LocationPolygon polygon : polygons) {
            LocationCoordinate2D polMax = findMostSouthCoordinate(polygon.getPoints());
            if (lhsInOnSouth(polMax, max)) {
                max = polMax;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostEastCoordinate(ArrayList<LocationCoordinate2D> points) {
        LocationCoordinate2D max = points.get(0);
        for (LocationCoordinate2D l : points) {
            if (lhsIsOnEast(l, max)) {
                max = l;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostWestCoordinate(ArrayList<LocationCoordinate2D> points) {
        LocationCoordinate2D max = points.get(0);
        for (LocationCoordinate2D l : points) {
            if (lhsIsOnWest(l, max)) {
                max = l;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostNorthCoordinate(ArrayList<LocationCoordinate2D> points) {
        LocationCoordinate2D max = points.get(0);
        for (LocationCoordinate2D l : points) {
            if (lhsInOnNorth(l, max)) {
                max = l;
            }
        }
        return max;
    }

    private LocationCoordinate2D findMostSouthCoordinate(ArrayList<LocationCoordinate2D> points) {
        LocationCoordinate2D max = points.get(0);
        for (LocationCoordinate2D l : points) {
            if (lhsInOnSouth(l, max)) {
                max = l;
            }
        }
        return max;
    }

    public ArrayList<Hexagon> getHandledHexagons() {
        return handledHexagons;
    }
}
