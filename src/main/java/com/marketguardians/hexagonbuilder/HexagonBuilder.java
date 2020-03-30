package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class HexagonBuilder {

//    private ArrayList<ArrayList<Optional<Hexagon>>> matrix = new ArrayList<>(new ArrayList<>());
    private ArrayList<ArrayList<Hexagon>> hexagonLayout = new ArrayList<>();

    private ArrayList<Hexagon> handledHexagons = new ArrayList<>();

    public void handleMatrixConf(MatrixConfiguration matrixConfiguration) {
        int rows = matrixConfiguration.getLayout().getRows();
        int columns = matrixConfiguration.getLayout().getColumns();
//        initConfMatrix(rows, columns);
        initCustomLocMatrix(rows, columns);

        for (LocationInMatrix location : matrixConfiguration.getLocations()) {
            Hexagon realHex = hexagonLayout.get(location.getPosition().getRow()).get(location.getPosition().getColumn());
            realHex.setName(location.getLocation().getName());
            realHex.setId(location.getLocation().getId());
            handledHexagons.add(realHex);
        }

        for (Hexagon h : handledHexagons) {
            h.printPoints();
        }

    }

    public HashMap<String, ArrayList<Hexagon>> gpsPointsInPolygon(double vzdialenostJednehoBodu, double vyska, ArrayList<LocationPolygon> polygons) {
        LocationCoordinate2D mostEast = findMostEastCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostNorth = findMostNorthCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostWest = findMostWestCoordFromPolygonArray(polygons);
        LocationCoordinate2D mostSouth = findMostSouthCoordFromPolygonArray(polygons);

        LocationCoordinate2D newLocE = mostEast.newLoc(90, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na vychod (* 2 je rezerva)
        LocationCoordinate2D newLocW = mostWest.newLoc(270, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na zapad
        LocationCoordinate2D newLocN = mostNorth.newLoc(0, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na sever
        LocationCoordinate2D newLocS = mostSouth.newLoc(180, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na juh

        LocationCoordinate2D matrixTopLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixTopRight = new LocationCoordinate2D(newLocE.getLongitude(), newLocN.getLatitude());
        LocationCoordinate2D matrixBottomRight = new LocationCoordinate2D(newLocE.getLongitude(), newLocS.getLatitude());
        LocationCoordinate2D matrixBottomLeft = new LocationCoordinate2D(newLocW.getLongitude(), newLocS.getLatitude());

        double width = matrixTopLeft.distance(matrixTopRight);
        double height = matrixBottomLeft.distance(matrixTopLeft);

        int numberOfColumns = (int) (width / vzdialenostJednehoBodu);
        int numberOfRows = (int) (height / vyska);

        ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix = pointsMatrix(matrixTopLeft, numberOfColumns, numberOfRows, vzdialenostJednehoBodu, vyska);

//        pointsMatrix.forEach(row -> {
//            row.forEach(point -> {
//                point.print();
//            });
//        });

        ArrayList<ArrayList<Hexagon>> hexMatrix = initHexMatrix(numberOfRows, numberOfColumns, matrixTopLeft, vzdialenostJednehoBodu / 1.5);

        hexMatrix.forEach(row -> {
//            row.forEach(Hexagon::printPoints);
        });

        ArrayList<Hexagon> hexMap = new ArrayList<Hexagon>();
        HashMap<String, ArrayList<Hexagon>> hexHasMap = new HashMap<String, ArrayList<Hexagon>>();

        for (int i=0; i < pointsMatrix.size(); i++) {
            for (int j=0; j < pointsMatrix.get(i).size(); j++) {
                System.out.println("I: " + i + " /" + pointsMatrix.size() + " J: " + j + " /" + pointsMatrix.get(i).size());
                LocationCoordinate2D point = pointsMatrix.get(i).get(j);
                for (LocationPolygon polygon : polygons) {
                    if (GFGPointCheck.isInside(polygon.getPoints(), polygon.getPoints().size(), point)) {
                        Hexagon h = hexMatrix.get(i).get(j);
                        h.setName(polygon.getName());
                        h.setObjectId(polygon.getObjectId());
                        hexMap.add(h);

                        if (!hexHasMap.containsKey(polygon.getObjectId())) {
                            hexHasMap.put(polygon.getObjectId(), new ArrayList<>());
                        }
                        hexHasMap.get(polygon.getObjectId()).add(h);
                    }
                }
            }
        }

        System.out.println("Hexhasmap: " + hexHasMap.size());

        hexHasMap.forEach((key, value) -> {
            System.out.println(key + ": " + value.size());
            value.forEach(h -> {
                System.out.println(h.getPoints());
            });
        });

                hexMap.forEach(Hexagon::printPoints);

        return hexHasMap;
    }

    public void gpsPointsIsInPolygonHex(double vzdialenostJednehoBodu, double vyska, LocationPolygon locationPolygon) {

        LocationCoordinate2D mostEast = findMostEastCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostNorth = findMostNorthCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostWest = findMostWestCoordinate(locationPolygon.getPoints());
        LocationCoordinate2D mostSouth = findMostSouthCoordinate(locationPolygon.getPoints());

        LocationCoordinate2D newLocE = mostEast.newLoc(90, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na vychod (* 2 je rezerva)
        LocationCoordinate2D newLocW = mostWest.newLoc(270, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na zapad
        LocationCoordinate2D newLocN = mostNorth.newLoc(0, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na sever
        LocationCoordinate2D newLocS = mostSouth.newLoc(180, vzdialenostJednehoBodu * 2); //chod vzdialenost bodu este na juh
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

    private ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix(LocationCoordinate2D matrixTopLeft, int numberOfColumns, int numberOfRows, double vzdialenostJednehoBodu, double vyska) {
        ArrayList<ArrayList<LocationCoordinate2D>> pointsMatrix = new ArrayList<>();
        LocationCoordinate2D previousValue = matrixTopLeft;
        for (int i = 0; i < numberOfRows; i++) {
            pointsMatrix.add(new ArrayList<>()); //novy riadok
            for (int j = 0; j < numberOfColumns; j++) {
                LocationCoordinate2D shiftedLoc = previousValue.newLoc(90, vzdialenostJednehoBodu); //posun na vychod
                previousValue = shiftedLoc;
                if (i == 0 && j == 0) {
                    pointsMatrix.get(i).add(previousValue); //ked je uplny zaciatok tak pridaj topLeft
                } else {
                    pointsMatrix.get(i).add(shiftedLoc); //inak pridaj shif
                }
            }
            previousValue = previousValue.newLoc(180, vyska); //posun na juh (novy riadok)
            previousValue.setLongitude(matrixTopLeft.getLongitude());
        }
        return pointsMatrix;
    }

    private ArrayList<ArrayList<Hexagon>> initHexMatrix(int rows, int columns, LocationCoordinate2D startingPoint, double distance) {
        ArrayList<ArrayList<Hexagon>> matrix = new ArrayList<>();

        for (int i=0; i<rows; i++) {
            matrix.add(new ArrayList<>());
            for (int j=0; j<columns; j++) {
                if (i == 0 && j == 0) {
                    Hexagon init = initHex(startingPoint, "eh", 1, distance);
                    matrix.get(i).add(init);
                } else if (j==0) {
                    if (i%2 == 0) { //parna rada
                        Hexagon susedHore = matrix.get(i-1).get(0);
                        Hexagon novyHex = buildBottomLeftHex("bl", susedHore.getBottomLeftPoint(), 1, distance);
                        matrix.get(i).add(novyHex);
                    } else { //neparna rada
                        Hexagon susedHore = matrix.get(i-1).get(0);
                        Hexagon novyHex = buildBottomRight("bl", susedHore.getBottomRightPoint(), 1, distance);
                        matrix.get(i).add(novyHex);
                    }
                } else {
                    Hexagon susedZlava = matrix.get(i).get(j-1);
                    Hexagon novyHex = buildRight("r", susedZlava.getRightPoint(), 0, distance);
                    matrix.get(i).add(novyHex);
                }
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
                    Hexagon newHex = buildBottomRight(chybneMeno, topNeighbour.getBottomRightPoint(), chybneId, distance);
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

    public static Hexagon buildBottomRight(String name, LocationCoordinate2D bottomRightPoint, long id, double distance) {
        ArrayList<LocationCoordinate2D> coords = buildHex(120.0, bottomRightPoint, distance);
        return new Hexagon(coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5), name, id);
    }


    public static ArrayList<LocationCoordinate2D> buildHex(Double startingBearing, LocationCoordinate2D start, double distance) {
        LocationCoordinate2D from = start;
        ArrayList<LocationCoordinate2D> coordinates = new ArrayList<>();
        coordinates.add(from);
        double b = startingBearing;
        for (int i = 1; i <= 6; i++) {
            LocationCoordinate2D next = from.newLoc(b, distance);
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
        if (lhs.getLatitude() - rhs.getLatitude() < 0) {
            return true;
        }
        return false;
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
