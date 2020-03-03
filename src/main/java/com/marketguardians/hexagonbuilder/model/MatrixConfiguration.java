package com.marketguardians.hexagonbuilder.model;

import java.util.ArrayList;

public class MatrixConfiguration {
    private MatrixLayout layout;
    private ArrayList<LocationInMatrix> locations;

    public MatrixConfiguration(MatrixLayout layout, ArrayList<LocationInMatrix> locations) {
        this.layout = layout;
        this.locations = locations;
    }

    public MatrixLayout getLayout() {
        return layout;
    }

    public ArrayList<LocationInMatrix> getLocations() {
        return locations;
    }
}
