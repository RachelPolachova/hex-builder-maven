package com.marketguardians.hexagonbuilder;

import java.util.ArrayList;

public class HexagonBuilder {

    enum HexagonsSide {
        TOPRIGHT,
        RIGHT,
        BOTTOMRIGHT,
        BOTTOMLEFT,
        LEFT,
        TOPLEFT,
        TOP,
        BOTTOM,
        BTRIGHT, //between top right
        BBRIGHT, //between bottom right
        BBLEFT,
        BTLEFT
    }

    public static ArrayList<Location> allLocations = new ArrayList<>();
    public static ArrayList<String> handledLocations = new ArrayList<>();

    public static Hexagon buildHexBasedOnNeighbours(ArrayList<Location> locations) {
        allLocations = locations;
        Location mostNorth = findMostNorth(locations);
        Hexagon start = initHex(mostNorth.getCenterLocation(), mostNorth.getName(), mostNorth.getId());
        handledLocations.add(mostNorth.getId());
        locations.remove(mostNorth);
        Location neighbour = null;
        if (mostNorth.getNeighboursIds().isEmpty()) {
            addNeighbour(start, findClosest(mostNorth, locations), mostNorth);
        } else {
            for (String id: mostNorth.getNeighboursIds()) {
                for (Location possibleNeighbour: locations) {
                    if (possibleNeighbour.getId().equals(id)) {
                        neighbour = possibleNeighbour;
                        addNeighbour(start, neighbour, mostNorth);
                        break;
                    }
                }
            }
        }
        handleNeighbours(start);
        System.out.println("returned start");
        return start;
    }

    private static void addHexToStart(Hexagon start) {
        Location loc = findLocation(start.getId());
        Location neighbour = null;
        System.out.println("NEIGH HERE " + start.getName());
        if (!loc.getNeighboursIds().isEmpty()) {
//            System.out.println("NEIGH NOT EMPTY");
            for (String id: loc.getNeighboursIds()) {
//                System.out.println("NEIGH GOING TRU IDS");
                for (Location possibleNeighbour: allLocations) {
//                    System.out.println("NEIGH GOING TRU ALLLOCS");
                    if (possibleNeighbour.getId().equals(id) && !handledLocations.contains(id)) {
//                        System.out.println("ADDIND NEIGHBORU");
                        neighbour = possibleNeighbour;
                        addNeighbour(start, neighbour, loc);
                        break;
                    }
                }
            }
        }
    }

    private static Location findLocation(String id) {
        for (Location l: allLocations) {
            if (l.getId().equals(id)) {
                return l;
            }
        }
        return null;
    }

    public static void handleNeighbours(Hexagon start) {
        System.out.println("handle neighborus called for " + start.getName());
        if (start.getTopRightHex().isPresent()) {
            addHexToStart(start.getTopRightHex().get());
        }

        if (start.getRightHex().isPresent()) {
            addHexToStart(start.getRightHex().get());
        }

        if (start.getBottomRightHex().isPresent()) {
            addHexToStart(start.getBottomRightHex().get());
        }

        if (start.getBottomLeftHex().isPresent()) {
            addHexToStart(start.getBottomLeftHex().get());
        }

        if (start.getLeftHex().isPresent()) {
            addHexToStart(start.getLeftHex().get());
        }

        if (start.getTopLeftHex().isPresent()) {
            addHexToStart(start.getTopLeftHex().get());
        }
    }

    private static void addNeighbour(Hexagon start, Location neighbour, Location mostNorth) {
        handledLocations.add(neighbour.getId());
        handleNeighbours(start);
        double bearing = mostNorth.getCenterLocation().bearing(neighbour.getCenterLocation());
        HexagonsSide neighboursSide = getBasicHexagonSide(bearing);
        System.out.println("Adding " + neighbour.getName() + " to " + mostNorth.getName() + " at " + neighboursSide + " bearing: " + bearing);
        if (neighboursSide == HexagonsSide.TOPRIGHT) {
            start.addToTopRight(neighbour);
        } else if (neighboursSide == HexagonsSide.RIGHT) {
            start.addToRight(neighbour);
        } else if (neighboursSide == HexagonsSide.BOTTOMRIGHT) {
            start.addToBottomRight(neighbour);
        } else if (neighboursSide == HexagonsSide.BOTTOMLEFT) {
            start.addToBottomLeft(neighbour);
        } else if (neighboursSide == HexagonsSide.LEFT) {
            start.addToLeft(neighbour);
        } else if (neighboursSide == HexagonsSide.TOPLEFT) {
            start.addToTopLeft(neighbour);
        }
    }

    private static Location findClosest(Location toLoc, ArrayList<Location> fromLocations) {
        ArrayList<LocationWithDistance> locationsWithDistance = sortByDistance(toLoc, fromLocations);
        return locationsWithDistance.get(0).getLocation();
    }

    public static Hexagon buildHexFromLocations(ArrayList<Location> locations) {
        // find most north location
        Location mostNorth = findMostNorth(locations);
//                locations.get(0); // findMostNorth(locations);
        System.out.println("Most north is: " + mostNorth.getName());
        Hexagon start = initHex(mostNorth.getCenterLocation(), mostNorth.getName(), mostNorth.getId());
        start.printPoints();
        // remove most north from location array and sort it by the distance
        locations.remove(mostNorth);
        System.out.println("------- Sorted:");
        ArrayList<LocationWithDistance> locationsWithDistance = sortByDistance(mostNorth, locations);
        locationsWithDistance.forEach(l -> {
            System.out.println(l.getLocation().getName() + " distance: " + l.getDistance());
        });
        // go one by one from sorted array and add it to start (most north) hexagon
        for (LocationWithDistance loc : locationsWithDistance) {
            double bearing = mostNorth.getCenterLocation().bearing(loc.getLocation().getCenterLocation());
            HexagonsSide side = getHexagonsSide(bearing); // + 60 is optimization
            if (side == HexagonsSide.BOTTOMLEFT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side + ", " + bearing);
                start.addToBottomLeft(loc.getLocation());
            } else if (side == HexagonsSide.LEFT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side + ", " + bearing);
                start.addToLeft(loc.getLocation());
            } else if (side == HexagonsSide.TOPLEFT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side + ", " + bearing);
                start.addToTopLeft(loc.getLocation());
            } else if (side == HexagonsSide.TOPRIGHT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side + ", " + bearing);
                start.addToTopRight(loc.getLocation());
            } else if (side == HexagonsSide.RIGHT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side + ", " + bearing);
                start.addToRight(loc.getLocation());
            } else if (side == HexagonsSide.BOTTOMRIGHT) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side+ ", " + bearing);
                start.addToBottomRight(loc.getLocation());
            } else if (side == HexagonsSide.TOP) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side+ ", " + bearing);
                start.addToTop(loc.getLocation());
            } else if (side == HexagonsSide.BOTTOM) {
                System.out.println("---- " + loc.getLocation().getName() + " ----");
                System.out.println("Adding: " + loc.getLocation().getName() + " to: " + side+ ", " + bearing);
                start.addToBottom(loc.getLocation(), true);
            }
        }
        return start;
    }

    public static HexagonsSide getBasicHexagonSide(double bearing) {
        double optimazedB = bearing + 30;
        if (optimazedB >= 60 && optimazedB < 120) {
            return HexagonsSide.TOPRIGHT;
        } else if (optimazedB >= 120 && optimazedB < 180) {
            return HexagonsSide.RIGHT;
        } else if (optimazedB >= 180 && optimazedB < 240) {
            return HexagonsSide.BOTTOMRIGHT;
        } else if (optimazedB >= 240 && optimazedB < 300) {
            return HexagonsSide.BOTTOMLEFT;
        } else if (optimazedB >= 300 && optimazedB < 160) {
            return HexagonsSide.LEFT;
        }
        return HexagonsSide.TOPLEFT;
    }

    public static HexagonsSide getHexagonsSide(Double bearing) {
        double optimizedB = bearing;
        double diff = 22.5; //45 / 2
        if (optimizedB >= 360-diff || (optimizedB >= 0 && optimizedB < 0 + diff) ) {
            return HexagonsSide.TOP;
        }

        if (optimizedB >= 45 - diff && optimizedB < 45 + diff) {
            return HexagonsSide.TOPRIGHT;
        }

        if (optimizedB >= 90 - diff && optimizedB < 90 + diff) {
            return HexagonsSide.RIGHT;
        }

        if (optimizedB >= 135 - diff && optimizedB < 135 + diff ) {
            return HexagonsSide.BOTTOMRIGHT;
        }

        if (optimizedB >= 180 - diff && optimizedB < 180 + diff) {
            return HexagonsSide.BOTTOM;
        }

        if (optimizedB >= 225 - diff && optimizedB < 225 + diff) {
            return HexagonsSide.BOTTOMLEFT;
        }

        if (optimizedB >= 270 - diff && optimizedB < 270 + diff) {
            return HexagonsSide.LEFT;
        }

//        if (optimizedB >= 0 && optimizedB < 60) {
//            return HexagonsSide.TOPLEFT;
//        }

        return HexagonsSide.TOPLEFT;

//        return HexagonsSide.LEFT;
    }

    public static ArrayList<LocationWithDistance> sortByDistance(Location fromLoc, ArrayList<Location> locations) {
        ArrayList<LocationWithDistance> locationsWithDistance = new ArrayList<>();
        for (Location loc : locations) {
            locationsWithDistance.add(new LocationWithDistance(loc, fromLoc.getCenterLocation().distance(loc.getCenterLocation())));
        }
        locationsWithDistance.sort((LocationWithDistance l1, LocationWithDistance l2) -> {
            if (l1.getDistance() > l2.getDistance()) {
                return 1;
            } else if (l1.getDistance() < l2.getDistance()) {
                return -1;
            }
            return 0;
        });
        return locationsWithDistance;
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

    private static Location findMostNorth(ArrayList<Location> locations) {
        Location max = locations.get(0);
        for (Location l: locations) {
            if (lhsInOnNorth(l.getCenterLocation(),max.getCenterLocation())) {
                max = l;
            }
        }
        return max;
    }

}
