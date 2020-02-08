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

public class JSONReader {
    public ArrayList<RUIANLocation> locations = new ArrayList<>();
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

