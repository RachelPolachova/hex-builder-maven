package com.marketguardians.hexagonbuilder;

import javax.print.event.PrintJobAdapter;
import java.util.ArrayList;
import java.util.Optional;

class Pozicia {
    private int i;
    private int j;
    private HexagonBuilder.HexagonsSide side;

    public Pozicia(int i, int j, HexagonBuilder.HexagonsSide side) {
        this.i = i;
        this.j = j;
        this.side = side;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public HexagonBuilder.HexagonsSide getSide() {
        return side;
    }
}

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
    }

    public ArrayList<ArrayList<LocationCoordinate2D>> hexagons = new ArrayList<>();
    public static ArrayList<Hexagon> hexs = new ArrayList<>();

    public static ArrayList<Location> allLocations = new ArrayList<>();
    public static ArrayList<String> handledLocations = new ArrayList<>();

    public static ArrayList<ArrayList<Optional<Hexagon>>> hexagonMatrix = new ArrayList<>(new ArrayList<>());

    public void getHexArray(ArrayList<Location> locations) {
        Location mostNorth = findMostNorth(locations);
        locations.remove(mostNorth);
        buildIntoArray(mostNorth, locations);
    }

    private ArrayList<String> handled = new ArrayList<>();

    public void susediaDoMatrixu(ArrayList<Location> locations) {
        Location mosthNorth = findMostNorth(locations);
        allLocations = locations;
        locations.remove(mosthNorth);
        initMatrix(4, 5);
        Hexagon first = initHex(mosthNorth.getCenterLocation(), mosthNorth.getName(), mosthNorth.getId()); //zodpoveda liberci
        hexagonMatrix.get(0).set(2, Optional.of(first));
        locations = sortByDistanceAsLocation(mosthNorth,locations); //bacha, bez mostNorth
        pridajLeftRight(0, 2, mosthNorth);

        for (ArrayList<Optional<Hexagon>> riadok: hexagonMatrix) {
            for (Optional<Hexagon> hex: riadok) {
                hex.ifPresent(Hexagon::printPoints);
            }
        }
    }

    private void pridajDalsiRiadok(int i) {
//        ArrayList<Optional<Hexagon>> riadok = hexagonMatrix.get(i);
//        for (int x = 0; x < riadok.size(); x++) {
//            Optional<Hexagon> hex = riadok.get(x);
//            if (hex.isPresent()) {
//                Location loc = getNeighbour(hex.get().getId());
//                if (loc != null) {
//                    for (String id: loc.getNeighboursIds()) {
//                        Location neighbour = getNeighbour(id);
//                        if (neighbour != null) {
//                            if (!handled.contains(neighbour.getId())) {
//                                double bearing = loc.getCenterLocation().bearing(neighbour.getCenterLocation());
//                                HexagonsSide side = getBasicHexagonSide(bearing);
//                                if (side == HexagonsSide.RIGHT) {
//                                    //najdi najblizsiu poziciu napravo
//                                    for (int pom = x; pom < riadok.size(); pom++) {
////                                        if (riadok.get(i).get());
//                                    }
//                                } else if (side == HexagonsSide.LEFT) {
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }


    private Location getNeighbour(String id) {
        for (Location loc: allLocations) {
            if (loc.getId().equals(id)) {
                return loc;
            }
        }
        return null;
    }

    private void pridajLeftRight(int i, int j, Location location) {
        for (String id: location.getNeighboursIds()) {
            Location pridavana = getNeighbour(id);
            if (pridavana != null) {
                double bearing = location.getCenterLocation().bearing(pridavana.getCenterLocation());
                HexagonsSide side = getBasicHexagonSide(bearing);
                if (side == HexagonsSide.RIGHT) {
                    handled.add(pridavana.getId());
                    Hexagon novy = buildRight(pridavana.getName(), hexagonMatrix.get(i).get(j).get().getRightPoint(), pridavana.getId());
                    hexagonMatrix.get(i).set(j+1, Optional.of(novy));
                } else if (side == HexagonsSide.LEFT) {
                    handled.add(pridavana.getId());
                    Hexagon novy = buildRight(pridavana.getName(), hexagonMatrix.get(i).get(j).get().getRightPoint(), pridavana.getId());
                    hexagonMatrix.get(i).set(j-1, Optional.of(novy));
                }
            }
        }
    }

    private static int getPoradieI(double vyska, double jedenRiadok, int riadky) {
        for (int i=1; i<=riadky; i++) {
            if (vyska <= jedenRiadok * i) {
                return riadky - i;
            }
        }
        return 99;
    }

    private static int getPoradieJ(double sirka, double jedenStlpec, int stlpce) {
        for (int i=1; i<=stlpce; i++) {
            if (sirka <= jedenStlpec * i) {
                return stlpce - i;
            }
        }

        return 99;
    }

    static public void checkniSusedov(ArrayList<Location> locations) {
        for (Location loc: locations) {
            for (String id: loc.getNeighboursIds()) {
                locations.forEach(possibleN -> {
                    if (possibleN.getId().equals(id)) {
                        HexagonsSide side = getBasicHexagonSide(loc.getCenterLocation().bearing(possibleN.getCenterLocation()));
                        System.out.println(loc.getName() + " ma " + possibleN.getName() + " na strane: " + side);
                    }
                });
            }
        }
    }

    static public void buildFromArray(ArrayList<Location> locations) {
        Location mostNorth = findMostNorth(locations);
        Location mostSouth = findMostSouth(locations);
        Location mostWest = findMostWest(locations);
        Location mostEast = findMostEast(locations);
        int riadky = 5;
        int stlpce = 5;
        double diffVyska = mostNorth.getCenterLocation().getLatitude() - mostSouth.getCenterLocation().getLatitude();
        double diffSirka = mostEast.getCenterLocation().getLongitude() - mostWest.getCenterLocation().getLongitude();
        System.out.println("diffVyska: " + diffVyska + " diffSirka: " + diffSirka);
        double jedenRiadok = diffVyska / riadky;
        double jedenStlpec = diffSirka / stlpce;
        System.out.println("most north lat: " + mostNorth.getCenterLocation().getLatitude() + " riadok: " + jedenRiadok);
        locations.remove(mostNorth);

        initMatrix(riadky, stlpce);
        locations = sortByDistanceAsLocation(mostNorth, locations);
        locations.add(0, mostNorth);
        for (Location loc: locations) {
            double vyska = loc.getCenterLocation().getLatitude() - mostSouth.getCenterLocation().getLatitude();
            int poradieI = getPoradieI(vyska, jedenRiadok, riadky);
            double sirka = mostEast.getCenterLocation().getLongitude() - loc.getCenterLocation().getLongitude();
            int poradieJ = getPoradieJ(sirka, jedenStlpec, stlpce);

            System.out.println("PRED obsadenost: " + loc.getName() + " i: " + poradieI + " j: " + poradieJ);
            Pozicia poziciaNovehoHexu = checkObsadenost(poradieI, poradieJ);
            System.out.println("PO obsadenost: " + loc.getName() + " i: " + poziciaNovehoHexu.getI() + " j: " + poziciaNovehoHexu.getJ());

            addToMatrix(loc, poziciaNovehoHexu.getI(), poziciaNovehoHexu.getJ());
//            System.out.println("Loc: " + loc.getName() + " poradie: " + poradieI + ", " + poradieJ + " lat: " + loc.getCenterLocation().getLatitude());
        }
        System.out.println("---- printing points -----");
        for (int i=0; i < hexagonMatrix.size(); i++) {
            for (int j=0; j<hexagonMatrix.get(i).size(); j++) {
                Optional<Hexagon> hex = hexagonMatrix.get(i).get(j);
                if (hex.isPresent()) {

                    System.out.println(hex.get().getName() + " at: " + i + ", " + j);
                    hex.get().printPoints();
                }
            }
        }
    }

    private static Pozicia checkObsadenost(int i, int j) {
        if (hexagonMatrix.get(i).get(j).isPresent()) {

            //sused napravo
            if (j < hexagonMatrix.get(i).size() - 1) {
                if (!hexagonMatrix.get(i).get(j + 1).isPresent()) {
                    return new Pozicia(i, j+1, HexagonsSide.RIGHT);
                }
            }

            //sused nalavo
            if (j > 0) {
                if (!hexagonMatrix.get(i).get(j-1).isPresent()) {
                    return new Pozicia(i, j-1, HexagonsSide.RIGHT);
                }
            }

            //sused dole
            if (i < hexagonMatrix.size() - 1) {
                if (!hexagonMatrix.get(i+1).get(j).isPresent()) {
                    return new Pozicia(i+1, j, HexagonsSide.RIGHT);
                }
            }

            //sused hore
            if (i > 0) {
                if (!hexagonMatrix.get(i-1).get(j).isPresent()) {
                    return new Pozicia(i-1, j, HexagonsSide.RIGHT);
                }
            }
        }
        return new Pozicia(i, j, HexagonsSide.RIGHT);
    }

    private static void initMatrix(int riadky, int stlpce) {
        for (int i = 0; i < riadky; i++) {
            hexagonMatrix.add(new ArrayList<>());
            for (int j = 0; j < stlpce; j++) {
                hexagonMatrix.get(i).add(Optional.empty());
            }

            System.out.println("one line: " + hexagonMatrix.get(i).size());
        }

        System.out.println("INIT HEX: " + hexagonMatrix.size());
    }

    private static void addToMatrix(Location location, int i, int j) {

        Pozicia najblizsi = najbizsi(i, j);

        System.out.println("meno: " + location.getName() + " i: " + i + " j: " + j + " najblizsi: " + najblizsi.getI() + " " + najblizsi.getJ());
        if (najblizsi.getI() != 99 && najblizsi.getJ() != 99) {

            if (hexagonMatrix.get(najblizsi.getI()).get(najblizsi.getJ()).isPresent()) {
                Hexagon najHex = hexagonMatrix.get(najblizsi.getI()).get(najblizsi.getJ()).get();

                HexagonsSide side = najblizsi.getSide();

                if (side == HexagonsSide.TOPRIGHT) {
                    Hexagon novy = buildBottomLeftHex(location.getName(), najHex.getBottomLeftPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()+1).set(najblizsi.getJ()-1, Optional.of(novy));
                } else if (side == HexagonsSide.RIGHT) {
                    Hexagon novy = buildLeftHex(location.getName(), najHex.getLeftPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()).set(najblizsi.getJ()-1, Optional.of(novy));
                } else if (side == HexagonsSide.BOTTOMRIGHT) {
                    Hexagon novy = buildTopLeft(location.getName(), najHex.getTopLeftPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()-1).set(najblizsi.getJ()-1, Optional.of(novy));
                } else if (side == HexagonsSide.BOTTOMLEFT) {
                    Hexagon novy = buildTopRight(location.getName(), najHex.getTopRightPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()-1).set(najblizsi.getJ()+1, Optional.of(novy));
                } else if (side == HexagonsSide.LEFT) {
                    Hexagon novy = buildRight(location.getName(), najHex.getRightPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()).set(najblizsi.getJ()+1, Optional.of(novy));
                } else if (side == HexagonsSide.TOPLEFT) {
                    Hexagon novy = buildBottomRight(location.getName(), najHex.getBottomRightPoint(), location.getId());
//                    hexagonMatrix.get(i).set(j, Optional.of(novy));
                    hexagonMatrix.get(najblizsi.getI()+1).set(najblizsi.getJ()+1, Optional.of(novy));
                } else if (side == HexagonsSide.TOP) {
                    if (j < hexagonMatrix.size() - 1) {
                        if (!hexagonMatrix.get(i).get(j+1).isPresent()) {
                            System.out.println("TOP, vlazam vpravo");
                            Hexagon novy = buildBottomRight(location.getName(), najHex.getBottomRightPoint(), location.getId());
                            hexagonMatrix.get(i).set(j+1, Optional.of(novy));
                        } else if (j > 0) {
                            if (!hexagonMatrix.get(i).get(j-1).isPresent()) {
                                System.out.println("TOP, vlazam vlavo");
                                Hexagon novy = buildBottomLeftHex(location.getName(), najHex.getBottomLeftPoint(), location.getId());
                                hexagonMatrix.get(i).set(j-1, Optional.of(novy));
                            } else {
                                System.out.println("CHYBA SE VLOUDILA, LEFT NENI VOLNE");
                            }
                        }
                    } else if (j > 0) {
                        if (!hexagonMatrix.get(i).get(j-1).isPresent()) {
                            System.out.println("TOP, vlazam vlavo");
                            Hexagon novy = buildBottomLeftHex(location.getName(), najHex.getBottomLeftPoint(), location.getId());
                            hexagonMatrix.get(i).set(j-1, Optional.of(novy));
                        } else {
                            System.out.println("CHYBA SE VLOUDILA, LEFT NENI VOLNE");
                        }
                    }
                } else { //bottom
                    if (j < hexagonMatrix.size() - 1) {
                        if (!hexagonMatrix.get(i).get(j+1).isPresent()) {
                            System.out.println("BOTTOM, vlazam vpravo");
                            Hexagon novy = buildTopRight(location.getName(), najHex.getTopRightPoint(), location.getId());
                            hexagonMatrix.get(i).set(j+1, Optional.of(novy));
                        } else if (j > 0) {
                            if (!hexagonMatrix.get(i).get(j-1).isPresent()) {
                                Hexagon novy = buildTopLeft(location.getName(), najHex.getTopLeftPoint(), location.getId());
                                hexagonMatrix.get(i).set(j-1, Optional.of(novy));
                            }
                        }
                    } else if (j < 0) {
                        Hexagon novy = buildTopLeft(location.getName(), najHex.getTopLeftPoint(), location.getId());
                        hexagonMatrix.get(i).set(j-1, Optional.of(novy));
                    }
                }
            } else {
                System.out.println("CHYBA, ZADNY HEX!");
            }
        } else {
            System.out.println("99: " + location.getName() + " adding at: " + i + ", " + j);
            Hexagon hex = initHex(location.getCenterLocation(), location.getName(), location.getId());
            hexagonMatrix.get(i).set(j, Optional.of(hex));
        }
    }

    private static Pozicia najbizsi(int i, int j) {
        Pozicia pozicia = new Pozicia(99, 99, HexagonsSide.RIGHT);

        int shift = 1;
        boolean outOfBoundsOnAllSides = false;
        boolean outOfOnTopRight = false;
        boolean outOfOnRight = false;
        boolean outOfOnBottomRight = false;
        boolean outOfOnBottomLeft = false;
        boolean outOfOnLeft = false;
        boolean outOfOnTopLeft = false;

        int diffBottom = hexagonMatrix.size() - 1 - i;
        int diffTop = hexagonMatrix.size() - 1 - diffBottom;
        int diffRight = hexagonMatrix.get(i).size() - 1 - j;
        int diffLeft = hexagonMatrix.size() - 1 - diffRight;

//        System.out.println("I: " + i + " J: " + j + " diffBottom: " + diffBottom + " diffTop: " + diffTop + " diffRight: " + diffRight + " diffLeft: " + diffLeft);

        while (!outOfBoundsOnAllSides) {

            //check right
            if (shift <= diffRight) {
                if (hexagonMatrix.get(i).get(j+shift).isPresent()) {
                    return new Pozicia(i, j+shift, HexagonsSide.RIGHT);
                }
            } else {
                outOfOnRight = true;
            }

            //check left
            if (shift <= diffLeft) {
                if (hexagonMatrix.get(i).get(j-shift).isPresent()) {
                    return new Pozicia(i, j-shift, HexagonsSide.LEFT);
                }
            } else {
                outOfOnLeft = true;
            }

            //check top left
            if (shift <= diffLeft && shift <= diffTop) {
                if (hexagonMatrix.get(i-shift).get(j-shift).isPresent()) {
                    return new Pozicia(i-shift, j-shift, HexagonsSide.TOPLEFT);
                }
            } else {
                outOfOnTopLeft = true;
            }

            //check top right
            if (shift <= diffRight && shift <= diffTop) {
                if (hexagonMatrix.get(i-shift).get(j+shift).isPresent()) {
                    return new Pozicia(i-shift, j+shift, HexagonsSide.TOPRIGHT);
                }
            } else {
                outOfOnTopRight = true;
            }

            //check bottom right
            if (shift <= diffRight && shift <= diffBottom) {
                if (hexagonMatrix.get(i+shift).get(j+shift).isPresent()) {
                    return new Pozicia(i+shift, j+shift, HexagonsSide.BOTTOMRIGHT);
                }
            } else {
                outOfOnBottomRight = true;
            }

            //check bottom left
            if (shift <= diffLeft && shift <= diffBottom) {
                if (hexagonMatrix.get(i+shift).get(j-shift).isPresent()) {
                    return new Pozicia(i+shift, j-shift, HexagonsSide.BOTTOMLEFT);
                }
            } else {
                outOfOnBottomLeft = true;
            }

            //check top
            if (shift <= diffTop) {
                if (hexagonMatrix.get(i-shift).get(j).isPresent()) {
                    return new Pozicia(i-shift, j, HexagonsSide.TOP);
                }
            }

            //check bottom
            if (shift <= diffBottom) {
                if (hexagonMatrix.get(i+shift).get(j).isPresent()) {
                    return new Pozicia(i+shift, j, HexagonsSide.BOTTOM);
                }
            }

            for (int x = j; x < hexagonMatrix.get(i).size(); x++) {
                if (hexagonMatrix.get(i).get(x).isPresent()) {
                    System.out.println("VRAIAM POZICIU Z FOR LOOP");
                    return new Pozicia(i, x, HexagonsSide.RIGHT);
                }
            }

            for (int x = j; x >= 0; x--) {
                if (hexagonMatrix.get(i).get(x).isPresent()) {
                    System.out.println("VRAIAM POZICIU Z FOR LOOP");
                    return new Pozicia(i, x, HexagonsSide.LEFT);
                }
            }

            shift++;
            outOfBoundsOnAllSides = (outOfOnTopRight && outOfOnRight && outOfOnBottomRight && outOfOnBottomLeft && outOfOnLeft && outOfOnTopLeft);
        }
        System.out.println("returning " + 99);
        return pozicia;
    }

    private static boolean blizkoZlava(int i, int j) {
        return hexagonMatrix.get(i).get(j-1).isPresent();
    }

    private static boolean blizkoZprava(int i, int j) {
        return hexagonMatrix.get(i).get(j+1).isPresent();
    }

    private void buildIntoArray(Location mostNorth, ArrayList<Location> locations) {
        locations.remove(mostNorth);
        ArrayList<Location> locationsToAddAtRight = findAllRight(mostNorth, locations);
        ArrayList<Location> locationsToAddAtLeft = findAllLeft(mostNorth, locations);
        Hexagon init = initHex(mostNorth.getCenterLocation(),mostNorth.getName(),mostNorth.getId());
        hexagons.add(init.getPoints());
        locationsToAddAtRight = sortByDistanceAsLocation(mostNorth, locationsToAddAtRight);
        locationsToAddAtLeft = sortByDistanceAsLocation(mostNorth, locationsToAddAtLeft);
        Hexagon previous = init;
        for (Location locToBuild: locationsToAddAtRight) {
            System.out.println("............. adding to right: " + locToBuild.getName());
            previous = buildRight(locToBuild.getName(), previous.getRightPoint(), locToBuild.getId());
            locations.remove(locToBuild);
            hexagons.add(previous.getPoints());
        }

        for (Location locToBuild: locationsToAddAtLeft) {
            System.out.println("............. adding to left: " + locToBuild.getName());
            previous = buildLeftHex(locToBuild.getName(), previous.getLeftPoint(), locToBuild.getId());
            locations.remove(locToBuild);
            hexagons.add(previous.getPoints());
        }
        Optional<Location> next = findBLorBR(mostNorth, locations);
        if (next.isPresent()) {
            locations.remove(next.get());
            buildIntoArray(next.get(), locations);
        }
    }

    private Optional<Location> findBLorBR(Location location, ArrayList<Location> locations) {
        Optional<Location> loc = Optional.empty();
        for (String id: location.getNeighboursIds()) {
            Optional<Location> neighbour = Optional.empty();
            for (Location possibleN: locations) {
                if (id.equals(possibleN.getId())) {
                    neighbour = Optional.of(possibleN);
                    break;
                }
            }
            if (neighbour.isPresent()) {
                double bearing = location.getCenterLocation().bearing(neighbour.get().getCenterLocation());
                HexagonsSide side = getBasicHexagonSide(bearing);
                if (side == HexagonsSide.BOTTOMRIGHT || side == HexagonsSide.BOTTOMLEFT) {
                    loc = Optional.of(neighbour.get());
                }
            }
        }
        return loc;
    }

    public ArrayList<Location> findAllRight(Location location, ArrayList<Location> locations) {
        ArrayList<Location> foundLocations = new ArrayList<>();
        for (Location possibleN: locations) {
            double bearing = location.getCenterLocation().bearing(possibleN.getCenterLocation());
            HexagonsSide side = getBasicHexagonSide(bearing);
            System.out.println("most north: " + location.getName() + " possible: " + possibleN.getName() + " bearing: " + bearing + " side: " + side);
            if (side == HexagonsSide.RIGHT && location.getNeighboursIds().contains(possibleN.getId())) {
                foundLocations.add(possibleN);
            }
        }
        return foundLocations;
    }

    public ArrayList<Location> findAllLeft(Location location, ArrayList<Location> locations) {
        ArrayList<Location> foundLocations = new ArrayList<>();
        for (Location possibleN: locations) {
            HexagonsSide side = getBasicHexagonSide(location.getCenterLocation().bearing(possibleN.getCenterLocation()));
            if (side == HexagonsSide.LEFT && location.getNeighboursIds().contains(possibleN.getId())) {
                foundLocations.add(possibleN);
            }
        }
        return foundLocations;
    }

    public static Hexagon buildHexBasedOnNeighbours(ArrayList<Location> locations) {
        allLocations = locations;
        Location mostNorth = findMostNorth(locations);
        Hexagon start = initHex(mostNorth.getCenterLocation(), mostNorth.getName(), mostNorth.getId());
        hexs.add(start);
        handledLocations.add(mostNorth.getId());
        for (String id: mostNorth.getNeighboursIds()) {
            for (Location n: locations) {
                if (n.getId().equals(id)) {
                    addNeighbour(start, n, mostNorth);
                }
            }
        }



//        locations.remove(mostNorth);
//        Location neighbour = null;
//        if (mostNorth.getNeighboursIds().isEmpty()) {
//            addNeighbour(start, findClosest(mostNorth, locations), mostNorth);
//        } else {
//            for (String id: mostNorth.getNeighboursIds()) {
//                for (Location possibleNeighbour: locations) {
//                    if (possibleNeighbour.getId().equals(id)) {
//                        neighbour = possibleNeighbour;
//                        addNeighbour(start, neighbour, mostNorth);
//                        break;
//                    }
//                }
//            }
//        }
        handleNeighbours(start);
        handleNeighbours(start);
        handleNeighbours(start);
        System.out.println("returned start");
        System.out.println("HANDLED LOCATIONS: " + handledLocations.size());
        return start;
    }

    public static ArrayList<Location> spracujBezSuseda(ArrayList<Location> locations) {
        for (int i=0; i<locations.size(); i++ ) {
            Location l = locations.get(i);
            if (l.getNeighboursIds().isEmpty()) {
                ArrayList<Location> list = new ArrayList<>(locations);
                list.remove(l);
                Location closest = findClosest(l,list);
                l.addNeighbour(closest.getId());
                locations.get(locations.indexOf(closest)).addNeighbour(l.getId());
                System.out.println("ID of closest: " + locations.indexOf(closest));
            }
        }
        return locations;
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
//        for (String id: mostNorth.getNeighboursIds()) {
//            for (Location n: locations) {
//                if (n.getId().equals(id)) {
//                    addNeighbour(start, n, mostNorth);
//                }
//            }
//        }

        ArrayList<Hexagon> hexagons = new ArrayList<>(hexs);

        for (Hexagon hex: hexagons) {
            Optional<Location> l = Optional.empty();
            for (Location loc: allLocations) {
                if (hex.getId().equals(loc.getId())) {
                    l = Optional.of(loc);
                    break;
                }
            }

            if (l.isPresent()) {
                for (String id: l.get().getNeighboursIds()) {
                    for (Location n: allLocations) {
                        if (n.getId().equals(id)) {
                            addNeighbour(hex, n, l.get());
                        }
                    }
                }
            }
        }
//        System.out.println("handle neighborus called for " + start.getName());
//        if (start.getTopRightHex().isPresent()) {
//            addHexToStart(start.getTopRightHex().get());
//        }
//
//        if (start.getRightHex().isPresent()) {
//            addHexToStart(start.getRightHex().get());
//        }
//
//        if (start.getBottomRightHex().isPresent()) {
//            addHexToStart(start.getBottomRightHex().get());
//        }
//
//        if (start.getBottomLeftHex().isPresent()) {
//            addHexToStart(start.getBottomLeftHex().get());
//        }
//
//        if (start.getLeftHex().isPresent()) {
//            addHexToStart(start.getLeftHex().get());
//        }
//
//        if (start.getTopLeftHex().isPresent()) {
//            addHexToStart(start.getTopLeftHex().get());
//        }
    }

    private static void addNeighbour(Hexagon start, Location neighbour, Location mostNorth) {
        if (!handledLocations.contains(neighbour.getId())) {
            handledLocations.add(neighbour.getId());
            double bearing = mostNorth.getCenterLocation().bearing(neighbour.getCenterLocation());
            HexagonsSide neighboursSide = getBasicHexagonSide(bearing);
            System.out.println("------ Adding " + neighbour.getName() + " to " + mostNorth.getName() + " at " + neighboursSide + " bearing: " + bearing);
            if (neighboursSide == HexagonsSide.TOPRIGHT) {
//            start.addToTopRight(neighbour);
                Hexagon hexagon = buildTopRight(neighbour.getName(), start.getTopRightPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
            } else if (neighboursSide == HexagonsSide.RIGHT) {
                Hexagon hexagon = buildRight(neighbour.getName(), start.getRightPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
//            start.addToRight(neighbour);
            } else if (neighboursSide == HexagonsSide.BOTTOMRIGHT) {
                Hexagon hexagon = buildBottomRight(neighbour.getName(), start.getBottomRightPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
//            start.addToBottomRight(neighbour);
            } else if (neighboursSide == HexagonsSide.BOTTOMLEFT) {
                Hexagon hexagon = buildBottomLeftHex(neighbour.getName(), start.getBottomLeftPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
//            start.addToBottomLeft(neighbour);
            } else if (neighboursSide == HexagonsSide.LEFT) {
                Hexagon hexagon = buildLeftHex(neighbour.getName(), start.getLeftPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
//            start.addToLeft(neighbour);
            } else if (neighboursSide == HexagonsSide.TOPLEFT) {
                Hexagon hexagon = buildTopLeft(neighbour.getName(), start.getTopLeftPoint(), neighbour.getId());
                hexs.add(checkIfIsFree(hexagon, neighboursSide));
//            start.addToTopLeft(neighbour);
            }
        }
    }

    private static Hexagon checkIfIsFree(Hexagon newHex, HexagonsSide side) {
        Optional<Hexagon> firstThere = Optional.empty();
        for (Hexagon h: hexs) {
            boolean equals = true;
            for (int i = 0; i < h.getPoints().size(); i++) {
                LocationCoordinate2D hLoc = h.getPoints().get(i);
                LocationCoordinate2D newLoc = newHex.getPoints().get(i);
                if (hLoc.getLatitude() != newLoc.getLatitude() || hLoc.getLongitude() != newLoc.getLongitude()) {
                    equals = false;
                }
                if (!equals) {
                    break;
                }
            }
            if (equals) {
                System.out.println("POSITION IS OCCUPIED! " + h.getName() + " new: " + newHex.getName());
                firstThere = Optional.of(h);
                break;
            }
        }
        if (firstThere.isPresent()) {
            System.out.println("Something was here first: " + firstThere.get().getName() + " ," + newHex.getName());
            if (side == HexagonsSide.RIGHT) {
                Hexagon shifted = buildRight(newHex.getName(), firstThere.get().getRightPoint(), newHex.getId());
                return checkIfIsFree(shifted, side);
            } else {
                Hexagon shifted = buildLeftHex(newHex.getName(), firstThere.get().getLeftPoint(), newHex.getId());
                return checkIfIsFree(shifted, side);
            }
        }
        System.out.println("First there was nil: " + newHex.getName());
        return newHex;
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

    static public ArrayList<Location> sortByDistanceAsLocation(Location fromLoc, ArrayList<Location> locations) {
        ArrayList<Location> sorted = new ArrayList<>();
        ArrayList<LocationWithDistance> sortedWithDistance = sortByDistance(fromLoc, locations);
        for (LocationWithDistance lwd: sortedWithDistance) {
            sorted.add(lwd.getLocation());
        }
        return sorted;
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


}
