package com.marketguardians.hexagonbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CSVReader {
//    public static ArrayList<Location> getLocationsFromFile(String name) {
//        ArrayList<Location> locationList = new ArrayList<>();
//        Path pathToFile = Paths.get(name);
//
//        try {
//            BufferedReader br = Files.newBufferedReader(pathToFile);
//            String line = br.readLine();
//            while (line != null) {
//                String[] attributes = line.split(";");
//                Location location = new Location(attributes[0], new LocationCoordinate2D(Double.parseDouble(attributes[1]), Double.parseDouble(attributes[2])));
//                locationList.add(location);
//                line = br.readLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return locationList;
//    }
}
