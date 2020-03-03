package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.*;
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


    public MatrixConfiguration readOwnJson(String filename) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(filename);
            JSONObject object =  (JSONObject) jsonParser.parse(reader);
            if (object.containsKey("layout")) {
                JSONObject layout = (JSONObject) object.get("layout");
                MatrixLayout matrixLayout = new MatrixLayout(Math.toIntExact((long) layout.get("columns")), Math.toIntExact((long) layout.get("rows")));
                JSONArray elements = (JSONArray) object.get("locations");
                ArrayList<LocationInMatrix> locations = new ArrayList<>();
                for (Object el: elements) {
                    JSONObject location = (JSONObject) el;
                    JSONObject position = (JSONObject) ((JSONObject) el).get("position");
                    Position customPosition = new Position(Math.toIntExact((long) position.get("column")), Math.toIntExact((long) position.get("row")));
                    locations.add(new LocationInMatrix(new SimpleLocation((String) location.get("name"), (String) location.get("id")), customPosition));
                }
                return new MatrixConfiguration(matrixLayout, locations);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createDistrictFiles(ArrayList<Region> regions, String fileName) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader fileReader = new FileReader(fileName);
            JSONObject object = (JSONObject) jsonParser.parse(fileReader);
            regions.forEach(region -> {
                 ArrayList<SimpleLocation> districts = parseRegion(region, object);
                 writeCustomJSON(region, districts);
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String fileName, ArrayList<Hexagon> hexagons) {
        JSONArray hexagonList = createJSON(hexagons);
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(hexagonList.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCustomJSON(Region region, ArrayList<SimpleLocation> locations) {
        JSONObject object = createDistrictJSON(locations);
        try {
            FileWriter fileWriter = new FileWriter(region.getName() + ".json");
            fileWriter.write(object.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject createDistrictJSON(ArrayList<SimpleLocation> locations) {
        JSONObject object = new JSONObject();
        JSONObject layout = new JSONObject();
        layout.put("columns", 0);
        layout.put("rows", 0);
        object.put("layout", layout);
        JSONArray locationArray = new JSONArray();
        locations.forEach(loc -> {
            JSONObject location = new JSONObject();
            location.put("name", loc.getName());
            location.put("id", loc.getId());
            JSONObject position = new JSONObject();
            position.put("column", 0);
            position.put("row", 0);
            location.put("position", position);
            locationArray.add(location);
        });
        object.put("locations", locationArray);
        return object;
    }

    private ArrayList<SimpleLocation> parseRegion(Region region, JSONObject object) {
        ArrayList<SimpleLocation> locations = new ArrayList<>();
        if (object.containsKey("features")) {
            JSONArray features = (JSONArray) object.get("features");
            features.forEach(f -> {
                JSONObject featureObject = (JSONObject) f;
                if (featureObject.containsKey("properties")) {
                    JSONObject properties = (JSONObject) featureObject.get("properties");
                    if (properties.containsKey("ref") && properties.containsKey("name")) {
                        String ref = (String) properties.get("ref");
                        if (ref.contains(region.getId())) {
                            locations.add(new SimpleLocation((String) properties.get("name"), ref));
                        }
                    }
                }
            });
        }
        return locations;
    }

    private JSONArray createJSON(ArrayList<Hexagon> hexagons) {
        JSONArray hexagonList = new JSONArray();
        hexagons.forEach(hex -> {
            JSONObject object = new JSONObject();
            object.put("id", hex.getId());
            object.put("name", hex.getName());
            JSONObject points = new JSONObject();

            JSONObject topLeft = new JSONObject();
            topLeft.put("lat", hex.getTopLeftPoint().getLatitude());
            topLeft.put("lon", hex.getTopLeftPoint().getLongitude());

            JSONObject topRight = new JSONObject();
            topRight.put("lat", hex.getTopRightPoint().getLatitude());
            topRight.put("lon", hex.getTopRightPoint().getLongitude());

            JSONObject right = new JSONObject();
            right.put("lat", hex.getRightPoint().getLatitude());
            right.put("lon", hex.getRightPoint().getLongitude());

            JSONObject bottomRight = new JSONObject();
            bottomRight.put("lat", hex.getBottomRightPoint().getLatitude());
            bottomRight.put("lon", hex.getBottomRightPoint().getLongitude());

            JSONObject bottomLeft = new JSONObject();
            bottomLeft.put("lat", hex.getBottomLeftPoint().getLatitude());
            bottomLeft.put("lon", hex.getBottomLeftPoint().getLongitude());

            JSONObject left = new JSONObject();
            left.put("lat", hex.getLeftPoint().getLatitude());
            left.put("lon", hex.getLeftPoint().getLongitude());

            points.put("topLeft", topLeft);
            points.put("topRight", topRight);
            points.put("right", right);
            points.put("bottomRight", bottomRight);
            points.put("bottomLeft", bottomLeft);
            points.put("left", left);
            object.put("points", points);
            hexagonList.add(object);
        });
        return hexagonList;
    }
}

