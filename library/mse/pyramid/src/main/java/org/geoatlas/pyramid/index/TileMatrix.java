package org.geoatlas.pyramid.index;

import java.io.Serializable;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/20 15:09
 * @since: 1.0
 **/
public class TileMatrix implements Description, Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String identifier;

    private String title;

    private String description;

    // scale denominator 即比例尺分母
    private double scaleDenominator;

    private double resolution;

    // 像元大小
    private double cellSize;

    // 瓦片矩阵原点
    private CornerOfOrigin cornerOfOrigin;

    // 瓦片大小 宽度(像素单位)
    private int tileWidth;

    // 瓦片大小 高度(像素单位)
    private int tileHeight;

    // 瓦片矩阵宽度(瓦片数量 从1开始)
    private int matrixWidth;

    // 瓦片矩阵高度(瓦片数量 从1开始)
    private int matrixHeight;

    @Override
    public TileMatrix clone() {
        try {
            return (TileMatrix) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    public void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    public double getCellSize() {
        return cellSize;
    }

    public void setCellSize(double cellSize) {
        this.cellSize = cellSize;
    }

    public CornerOfOrigin getCornerOfOrigin() {
        return cornerOfOrigin;
    }

    public void setCornerOfOrigin(CornerOfOrigin cornerOfOrigin) {
        this.cornerOfOrigin = cornerOfOrigin;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public void setMatrixWidth(int matrixWidth) {
        this.matrixWidth = matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public void setMatrixHeight(int matrixHeight) {
        this.matrixHeight = matrixHeight;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
