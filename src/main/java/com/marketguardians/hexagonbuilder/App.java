package com.marketguardians.hexagonbuilder;

import com.marketguardians.hexagonbuilder.model.MatrixConfiguration;

public class App  {
    public static void main(String[] args) {
        handleOwnJSON();
    }


    public static void handleOwnJSON() {
        JSONReader jsonReader = new JSONReader();
        MatrixConfiguration matrixConfiguration = jsonReader.readOwnJson("republika.json");
        HexagonBuilder hexagonBuilder = new HexagonBuilder();
        hexagonBuilder.handleMatrixConf(matrixConfiguration);
    }
}
