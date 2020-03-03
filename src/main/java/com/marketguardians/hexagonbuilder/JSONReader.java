package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
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
}

