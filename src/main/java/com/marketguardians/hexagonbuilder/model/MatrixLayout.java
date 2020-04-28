package com.marketguardians.hexagonbuilder.model;

public class MatrixLayout {
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
