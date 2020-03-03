package com.marketguardians.hexagonbuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class MatrixLayout {
    private int columns;
    private int rows;

    public MatrixLayout(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }
}

class CustomPosition {
    private int column;
    private int row;

    public CustomPosition(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}

class CustomLocation {
    private String name;
    private String id;
    private CustomPosition position;

    public CustomLocation(String name, String id, CustomPosition position) {
        this.name = name;
        this.id = id;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public CustomPosition getPosition() {
        return position;
    }
}

class MatrixConf {
    private MatrixLayout layout;
    private ArrayList<CustomLocation> locations;

    public MatrixConf(MatrixLayout layout, ArrayList<CustomLocation> locations) {
        this.layout = layout;
        this.locations = locations;
    }

    public MatrixLayout getLayout() {
        return layout;
    }

    public ArrayList<CustomLocation> getLocations() {
        return locations;
    }
}

public class JSONReader {
    public ArrayList<RUIANLocation> locations = new ArrayList<>();

    public void readOnlyRefWith(String fileName, String ref) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(fileName);
            Object obj = jsonParser.parse(reader);
            JSONObject object = (JSONObject) obj;
            JSONArray elements = (JSONArray) object.get("features");

            elements.forEach(el -> {
                parseJSONElementOnlyWithRef( (JSONObject) el, ref);
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public MatrixConf readOwnJson(String filename) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(filename);
            JSONObject object =  (JSONObject) jsonParser.parse(reader);
            if (object.containsKey("layout")) {
                JSONObject layout = (JSONObject) object.get("layout");
                MatrixLayout matrixLayout = new MatrixLayout(Math.toIntExact((long) layout.get("columns")), Math.toIntExact((long) layout.get("rows")));
                JSONArray elements = (JSONArray) object.get("locations");
                ArrayList<CustomLocation> locations = new ArrayList<>();
                for (Object el: elements) {
                    JSONObject location = (JSONObject) el;
                    JSONObject position = (JSONObject) ((JSONObject) el).get("position");
                    CustomPosition customPosition = new CustomPosition(Math.toIntExact((long) position.get("column")), Math.toIntExact((long) position.get("row")));
                    locations.add(new CustomLocation((String) location.get("name"), (String) location.get("id"), customPosition));
                }
                return new MatrixConf(matrixLayout, locations);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void read(String fileName) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(fileName);
            Object obj = jsonParser.parse(reader);
            JSONObject object = (JSONObject) obj;
            JSONArray elements = (JSONArray) object.get("features");

            elements.forEach(el -> {
                parseJSONElement( (JSONObject) el);
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Location> parseHandledJSON(String fileName) {
        ArrayList<Location> locations = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(fileName);
            JSONArray array = (JSONArray) jsonParser.parse(reader);
            for (Object el: array) {
                JSONObject jsonObject = (JSONObject) el;
                JSONObject centerLoc = (JSONObject) jsonObject.get("centerLocation");
                Location newLocation = new Location( (String) jsonObject.get("name"),
                        (String) jsonObject.get("id"),
                        new LocationCoordinate2D((double) centerLoc.get("lon"), (double)centerLoc.get("lat")),
                        new ArrayList<>());
                JSONArray neighbours = (JSONArray) jsonObject.get("neighboursIds");
                neighbours.forEach(n -> {
                    newLocation.addNeighbour((String) n);
                });
                locations.add(newLocation);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

    public void write(String fileName, ArrayList<Location> locations) {
        JSONArray array = createJSON(locations);
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(array.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray createJSON(ArrayList<Location> locations) {
        JSONArray locationList = new JSONArray();
        locations.forEach(loc -> {
            JSONObject object = new JSONObject();
            object.put("id", loc.getId());
            object.put("name", loc.getName());
            JSONObject centerLoc = new JSONObject();
            centerLoc.put("lon", loc.getCenterLocation().getLongitude());
            centerLoc.put("lat", loc.getCenterLocation().getLatitude());
            object.put("centerLocation", centerLoc);
            object.put("neighboursIds", loc.getNeighboursIds());
            locationList.add(object);
        });
        return locationList;
    }

    private void parseJSONElementOnlyWithRef(JSONObject element, String ref) {
        if (element.containsKey("properties")) {
                JSONObject properties = (JSONObject) element.get("properties");
                if (properties.containsKey("ref")) {
                    String refObject = (String) properties.get("ref");
                    if (refObject.contains(ref)) {
                        if (properties.containsKey("name")) { //we are sure that it's region's object, not area admin
                            RUIANLocation ruianLocation = new RUIANLocation((String) properties.get("name"), (String) properties.get("ref"));
                            System.out.println("Ruian loc: " + ruianLocation.getName());
                            JSONObject geometry = (JSONObject) element.get("geometry");
                            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                            JSONArray coordFixed = (JSONArray) coordinates.get(0);
                            for (Object c :
                                    coordFixed) {
                                JSONArray points = (JSONArray) c;
                                ruianLocation.addPoint(new LocationCoordinate2D((double) points.get(0), (double) points.get(1)));
                            }
                            locations.add(ruianLocation);
                        }
                    }
                }
        }
    }

    private void parseJSONElement(JSONObject element) {
        if (element.containsKey("properties")) {
            JSONObject properties = (JSONObject) element.get("properties");
            if (properties.containsKey("name")) { //we are sure that it's region's object, not area admin
                RUIANLocation ruianLocation = new RUIANLocation((String) properties.get("name"), (String) properties.get("ref:NUTS"));
                System.out.println("Ruian loc: " + ruianLocation.getName());
                JSONObject geometry = (JSONObject) element.get("geometry");
                JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                JSONArray coordFixed = (JSONArray) coordinates.get(0);
                for (Object c :
                        coordFixed) {
                    JSONArray points = (JSONArray) c;
                    ruianLocation.addPoint(new LocationCoordinate2D((double) points.get(0), (double) points.get(1)));
                }
                for (RUIANLocation loc : locations) {
                    for (LocationCoordinate2D coords: loc.getPoints()) {
                        if (ruianLocation.getNeighboursIds().contains(loc.getId())) {
                            break;
                        }
                        for (LocationCoordinate2D coordsNew: ruianLocation.getPoints()) {
                            if (!ruianLocation.getNeighboursIds().contains(loc.getId())) { // one gps point is enough
                                if (coords.getLatitude() == coordsNew.getLatitude() && coords.getLongitude() == coordsNew.getLongitude()) {
                                    loc.addNeighboursId(ruianLocation.getId());
                                    ruianLocation.addNeighboursId(loc.getId());
                                    break;
                                }
                            }
                        }
                    }
                }
                locations.add(ruianLocation);
            }
        }
    }
}

