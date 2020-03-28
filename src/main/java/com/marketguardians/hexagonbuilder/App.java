package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.LocationCoordinate2D;
import com.marketguardians.hexagonbuilder.model.LocationPolygon;
import com.marketguardians.hexagonbuilder.model.Region;
import com.marketguardians.hexagonbuilder.model.MatrixConfiguration;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class App {
    public static void main(String[] args) {
//        vsetkoNaraz();
//        spracujGeojsonKrajov();
        spracujGeojsonOrp();
//        spracujGeojsonObce();
    }

    public static void vsetkoNaraz() {
        JSONReader reader = new JSONReader();
        reader.fetchAllRegionsAndTheirRelations(2L);
    }

    public static void spracujGeojsonKrajov() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujKrajGeojson();
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.gpsPointsInPolygon(14, 11, polygons);
        reader.hexIntoJsonArray(hexagons, "db-kraje.json", "kraje-paths.json");
    }

    public static void spracujGeojsonOrp() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujViaceroGeojsonSuborov(10, 200, "orp");
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.gpsPointsInPolygon(10, 11, polygons);
        reader.hexIntoJsonArray(hexagons, "db-orp.json", "orp-paths.json");
    }

    public static void spracujGeojsonObce() {
        JSONReader reader = new JSONReader();
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        ArrayList<LocationPolygon> polygons = reader.spracujViaceroGeojsonSuborov(10, 6250, "obec");
        System.out.println("Size: " + polygons.size());
        HashMap<String, ArrayList<Hexagon>> hexagons = hexagonBuilder.gpsPointsInPolygon(5, 11, polygons);
        reader.hexIntoJsonArray(hexagons, "db-obec.json", "obec-paths.json");
    }

    public static void okresyJsonDostanId() {
        JSONReader jsonReader = new JSONReader();
        jsonReader.multipleRegionJsonGetIds("orp-related.json", "orp");
    }

    public static void obceJsonDostanId() {
        JSONReader jsonReader = new JSONReader();
        jsonReader.multipleRegionJsonGetIds("related-obce.json", "obec");
    }

    public static void spracujMultipleHexOkresJsonDoJedneho() {
        JSONReader reader = new JSONReader();

        ArrayList<String> ids = new ArrayList<>(
                Arrays.asList("3", "4", "10", "13", "21", "415", "817", "2015", "2016", "3216", "3615", "3616", "4015", "4016")
        );
        reader.okresyMultipleHexDoJednehoJson(ids);
    }


    public static void novySpusobMapOkresy() {
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        JSONReader reader = new JSONReader();

        ArrayList<String> ids = new ArrayList<>(
                Arrays.asList("3", "4", "10", "13", "21", "415", "817", "2015", "2016", "3216", "3615", "3616", "4015", "4016")
        );

//        ids.forEach(id -> {
//            ArrayList<LocationPolygon> polygons = reader.getOkresLocPol("okresy/" + id + ".json");
//            System.out.println("Size: " + polygons.size());
//            reader.saveToJson(hexagonBuilder.gpsPointsInPolygon(13, 13, polygons), id + "-okresy-multiplehex.json", JSONReader.RegionType.OKRES, id);
//        });
    }

    public static void novySpusobMapOkresDetailne() {
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        JSONReader reader = new JSONReader();

        ArrayList<String> ids = new ArrayList<>(
                Arrays.asList("3", "4", "10", "13", "21", "415", "817", "2015", "2016", "3216", "3615", "3616", "4015", "4016")
        );

//        ids.forEach(id -> {
//            ArrayList<LocationPolygon> polygons = reader.getOkresLocPol("okresy/" + id + ".json");
//            System.out.println("Size: " + polygons.size());
//            reader.saveToJson(hexagonBuilder.gpsPointsInPolygon(13, 13, polygons), id + "-okresy-multiplehex.json", JSONReader.RegionType.OKRES, id);
//        });
    }

    public static void novySpusobMapKraje() {
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        JSONReader reader = new JSONReader();

        ArrayList<String> ids = new ArrayList<>(
                Arrays.asList("3", "4", "10", "13", "21", "415", "817", "2015", "2016", "3216", "3615", "3616", "4015", "4016")
        );
//        ArrayList<LocationPolygon> polygons = reader.spracujKraje(ids);
//        reader.saveToJson(hexagonBuilder.gpsPointsInPolygon(14, 11, polygons), "kraje-multiplehex.json", JSONReader.RegionType.KRAJ, "-");
    }

    public static void regionIdPath() {
        RUIANService ruianService = new RUIANService();
        ruianService.createRegionIdPathJson("./pou-db.json");
    }
}
