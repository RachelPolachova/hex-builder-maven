package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.Region;
import com.marketguardians.hexagonbuilder.model.MatrixConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

public class App  {
    public static void main(String[] args) {
//        spracujKraje();
        handleOwnJSON();
    }


    public static void handleOwnJSON() {
        JSONReader jsonReader = new JSONReader();
        MatrixConfiguration matrixConfiguration = jsonReader.readOwnJson("kralovehradecky.json");
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        hexagonBuilder.handleMatrixConf(matrixConfiguration);
        jsonReader.write("kralovehradecky-hexagons.json", hexagonBuilder.getHandledHexagons());
    }

    public static void spracujKraje() {
        ArrayList<Region> regions = new ArrayList<Region>(
                Arrays.asList(
                        new Region("CZ041", "karlovarsky"),
                        new Region("CZ042", "ustecky"),
                        new Region("CZ051", "liberecky"),
                        new Region("CZ010", "praha"),
                        new Region("CZ052", "kralovehradecky"),
                        new Region("CZ053", "pardubicky"),
                        new Region("CZ080", "moravskoslezsky"),
                        new Region("CZ032", "plzensky"),
                        new Region("CZ020", "stredocesky"),
                        new Region("CZ063", "vysocina"),
                        new Region("CZ071", "olomoucky"),
                        new Region("CZ072", "zlinsky"),
                        new Region("CZ031", "jihocesky"),
                        new Region("CZ064", "jihomoravsky")
                        )
        );
        JSONReader jsonReader = new JSONReader();
        jsonReader.createDistrictFiles(regions, "jednotlive-kraje.geojson");
    }
}
