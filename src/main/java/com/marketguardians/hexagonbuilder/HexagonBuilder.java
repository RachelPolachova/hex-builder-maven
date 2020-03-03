package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.Location;
import com.marketguardians.hexagonbuilder.model.LocationCoordinate2D;
import com.marketguardians.hexagonbuilder.model.LocationInMatrix;
import com.marketguardians.hexagonbuilder.model.MatrixConfiguration;

import java.util.ArrayList;
import java.util.Optional;

public class HexagonBuilder {

    private ArrayList<ArrayList<Optional<Hexagon>>> matrix = new ArrayList<>(new ArrayList<>());
    private ArrayList<ArrayList<Hexagon>> hexagonLayout = new ArrayList<>();

    private ArrayList<Hexagon> handledHexagons = new ArrayList<>();

    public void handleMatrixConf(MatrixConfiguration matrixConfiguration) {
        int rows = matrixConfiguration.getLayout().getRows();
        int columns = matrixConfiguration.getLayout().getColumns();
        initConfMatrix(rows, columns);
        initCustomLocMatrix(rows, columns);

        for (LocationInMatrix location: matrixConfiguration.getLocations()) {
            Hexagon realHex = hexagonLayout.get(location.getPosition().getRow()).get(location.getPosition().getColumn());
            realHex.setName(location.getLocation().getName());
            realHex.setId(location.getLocation().getId());
            handledHexagons.add(realHex);
        }

        for (Hexagon h: handledHexagons) {
            h.printPoints();
        }

    }

    private void initConfMatrix(int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < columns; j++) {
                matrix.get(i).add(Optional.empty());
            }
        }
    }

    private void initCustomLocMatrix(int rows, int columns) {
        String chybneMeno = "chyba";
        String chybneId = "chyba";
        for (int i=0; i < rows; i++) {
            hexagonLayout.add(new ArrayList<>());
            for (int j=0; j < columns; j++) {
                if (i==0 && j==0) {
                    Hexagon init = initHex(new LocationCoordinate2D(16.6113382, 49.1922443), chybneMeno, chybneId);
                    hexagonLayout.get(i).add(j, init);
                } else if (i==0) {
                    Hexagon leftNeighbour = hexagonLayout.get(i).get(j-1);
                    Hexagon newHex = buildRight(chybneMeno, leftNeighbour.getRightPoint(), chybneId);
                    hexagonLayout.get(i).add(j, newHex);
                } else {
                    Hexagon topNeighbour = hexagonLayout.get(i-1).get(j);
                    Hexagon newHex = buildBottomRight(chybneMeno, topNeighbour.getBottomRightPoint(), chybneId);
                    hexagonLayout.get(i).add(j, newHex);
                }
            }
        }
    }

    public static Hexagon buildBottomLeftHex(String name, LocationCoordinate2D bottomLeftPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(180.0, bottomLeftPoint);
        return new Hexagon(coords.get(5), coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), name, id);
    }

    public static Hexagon buildLeftHex(String name, LocationCoordinate2D leftPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(240.0, leftPoint);
        return new Hexagon(coords.get(4),coords.get(5),coords.get(0),coords.get(1),coords.get(2),coords.get(3), name, id);
    }

    public static Hexagon buildTopLeft(String name, LocationCoordinate2D topLeftPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(300.0, topLeftPoint);
        return new Hexagon(coords.get(3),coords.get(4),coords.get(5),coords.get(0),coords.get(1),coords.get(2), name, id);
    }

    public static Hexagon buildTopRight(String name, LocationCoordinate2D topRightPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(0.0, topRightPoint);
        return new Hexagon(coords.get(2),coords.get(3),coords.get(4),coords.get(5),coords.get(0),coords.get(1), name, id);
    }

    public static Hexagon buildRight(String name, LocationCoordinate2D rightPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(60.0, rightPoint);
        return new Hexagon(coords.get(1),coords.get(2),coords.get(3),coords.get(4),coords.get(5),coords.get(0), name, id);
    }

    public static Hexagon buildBottomRight(String name, LocationCoordinate2D bottomRightPoint, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(120.0, bottomRightPoint);
        return new Hexagon(coords.get(0),coords.get(1),coords.get(2),coords.get(3),coords.get(4),coords.get(5), name, id);
    }


    public static ArrayList<LocationCoordinate2D> buildHex(Double startingBearing, LocationCoordinate2D start) {
        LocationCoordinate2D from = start;
        ArrayList<LocationCoordinate2D> coordinates = new ArrayList<>();
        coordinates.add(from);
        double b = startingBearing;
        for(int i = 1; i<=6; i++) {
            LocationCoordinate2D next = from.newLoc(b,5);
            coordinates.add(next);
            from = next;
            b+=60;
        }
        return coordinates;
    }

    public static Hexagon initHex(LocationCoordinate2D start, String name, String id) {
        ArrayList<LocationCoordinate2D> coords = buildHex(120.0, start);
        return new Hexagon(coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5), name, id);
    }

    private static Boolean lhsIsOnWest(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        if (lhs.getLongitude() - rhs.getLongitude() < 0) {
            return  true;
        }
        return false;
    }

    private static Boolean lhsIsOnEast(LocationCoordinate2D lhs, LocationCoordinate2D rhs) {
        if (lhs.getLongitude() - rhs.getLongitude() > 0) {
            return  true;
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
        for (Location l: locations) {
            if (lhsInOnNorth(l.getCenterLocation(),max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostSouth(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l: locations) {
            if (lhsInOnSouth(l.getCenterLocation(),max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostWest(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l: locations) {
            if (lhsIsOnWest(l.getCenterLocation(),max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    static Location findMostEast(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l: locations) {
            if (lhsIsOnEast(l.getCenterLocation(),max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

    public ArrayList<Hexagon> getHandledHexagons() {
        return handledHexagons;
    }
}
