package org.geoatlas.pyramid.index;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/20 15:27
 * @since: 1.0
 **/
public class TileMatrixSet implements Description{
    private String identifier;
    private String title;
    private String description;

    private CoordinateReferenceSystem crs;

    private BoundingBox extent;

    // 瓦片矩阵原点
    private CornerOfOrigin cornerOfOrigin;

    private TileMatrix[] matrixLevels;

    /**
     * {@code true} if the resolutions are preserved and the scaleDenominators calculated, {@code
     * false} if the resolutions are calculated based on the scale denominators.
     */
    private boolean resolutionsPreserved;

    protected TileMatrixSet() {
    }

    public TileMatrixSet(TileMatrixSet t) {
        this.identifier = t.identifier;
        this.title = t.title;
        this.description = t.description;
        this.crs = t.crs;
        this.extent = t.extent;
        this.cornerOfOrigin = t.cornerOfOrigin;
        this.matrixLevels = t.matrixLevels;
        this.resolutionsPreserved = t.resolutionsPreserved;
    }

    public BoundingBox boundsFromIndex(long[] tileIndex) {
        final int tileZ = (int) tileIndex[2];
        TileMatrix matrix = getMatrix(tileZ);
        long tileX = tileIndex[0];
        long tileY;

        double width = matrix.getResolution() * (double) matrix.getTileWidth();
        double height = matrix.getResolution() * (double) matrix.getTileHeight();

        double[] tileOrigin = this.tileOrigin();
        BoundingBox tileBounds;
        // 此处假定传递的瓦片坐标属于google体系的瓦片坐标系，即原点在左上角
        if (CornerOfOrigin.TOP_LEFT.equals(this.cornerOfOrigin)) {
            tileY = tileIndex[1];
            tileBounds = new BoundingBox(tileOrigin[0] + width * (double) tileX, tileOrigin[1] - height * (double) (tileY + 1L),
                    tileOrigin[0] + width * (double) (tileX + 1L), tileOrigin[1] - height * (double) tileY);
        } else if (CornerOfOrigin.BOTTOM_LEFT.equals(this.cornerOfOrigin)){
            tileY = matrix.getMatrixHeight() - tileIndex[1];
            tileBounds = new BoundingBox(tileOrigin[0] + width * (double) tileX, tileOrigin[1] + height * (double) tileY,
                    tileOrigin[0] + width * (double) (tileX + 1L), tileOrigin[1] + height * (double) (tileY + 1L));
        }else {
            throw new IllegalArgumentException("Unsupported corner of origin: " + this.cornerOfOrigin);
        }
        return tileBounds;
    }

    /**
     * 默认原点为左上角 已修正, 左下角未测试
     * Finds the spatial bounding box of a rectangular group of tiles.
     *
     * @param rectangleExtent the rectangle of tiles. {minx, miny, maxx, maxy} in tile coordinates
     * @return the spatial bounding box in the coordinates of the SRS used by the GridSet
     */
    protected BoundingBox boundsFromRectangle(long[] rectangleExtent) {
        TileMatrix grid = getMatrix((int) rectangleExtent[4]);

        double width = grid.getResolution() * grid.getTileWidth();
        double height = grid.getResolution() * grid.getTileHeight();

        long bottomY = rectangleExtent[3];
        long topY = rectangleExtent[1];

        // 此处假定传递的瓦片坐标属于google体系的瓦片坐标系，即原点在左上角
        if (CornerOfOrigin.BOTTOM_LEFT.equals(this.cornerOfOrigin)) {
            bottomY = rectangleExtent[1] - grid.getMatrixHeight() -1;
            topY = rectangleExtent[3] - grid.getMatrixHeight() + 1;
        }

        double[] tileOrigin = tileOrigin();
        double minx = tileOrigin[0] + width * rectangleExtent[0];
        double miny = tileOrigin[1] - height * (bottomY + 1);
        double maxx = tileOrigin[0] + width * (rectangleExtent[2] + 1);
        double maxy = tileOrigin[1] - height * (topY);
        BoundingBox rectangleBounds = new BoundingBox(minx, miny, maxx, maxy);

        return rectangleBounds;
    }

    protected long[] closestIndex(BoundingBox tileBounds) throws MatrixMismatchException {
        double wRes = tileBounds.getWidth() / getMatrix(0).getTileWidth();

        double bestError = Double.MAX_VALUE;
        int bestLevel = -1;
        double bestResolution = -1.0;

        for (int i = 0; i < getNumLevels(); i++) {
            TileMatrix grid = getMatrix(i);

            double error = Math.abs(wRes - grid.getResolution());

            if (error < bestError) {
                bestError = error;
                bestResolution = grid.getResolution();
                bestLevel = i;
            } else {
                break;
            }
        }

        if (Math.abs(wRes - bestResolution) > (0.1 * wRes)) {
            throw new ResolutionMismatchException(wRes, bestResolution);
        }

        return closestIndex(bestLevel, tileBounds);
    }

    /**
     * 默认原点为左上角 修正未测试, 左下角未测试
     * @param level
     * @param tileBounds
     * @return
     * @throws MatrixAlignmentMismatchException
     */
    protected long[] closestIndex(int level, BoundingBox tileBounds)
            throws MatrixAlignmentMismatchException {
        TileMatrix grid = getMatrix(level);

        double width = grid.getResolution() * grid.getTileWidth();
        double height = grid.getResolution() * grid.getTileHeight();

        double x = (tileBounds.getMinX() - tileOrigin()[0]) / width;

        double y = (tileOrigin()[1] - tileBounds.getMaxY()) / height;

        long posX = Math.round(x);

        long posY = Math.round(y);

        if (Math.abs(x - posX) > 0.1 || Math.abs(y - posY) > 0.1) {
            throw new MatrixAlignmentMismatchException(x, posX, y, posY);
        }

        if (CornerOfOrigin.BOTTOM_LEFT.equals(this.cornerOfOrigin)) {
//            posY = posY + grid.getNumTilesHigh();
            posY = posY + grid.getMatrixHeight();
        }

        long[] ret = {posX, posY, level};

        return ret;
    }


    public long[] closestRectangle(BoundingBox rectangleBounds) {
        double rectWidth = rectangleBounds.getWidth();
        double rectHeight = rectangleBounds.getHeight();

        double bestError = Double.MAX_VALUE;
        int bestLevel = -1;

        // Now we loop over the resolutions until
        for (int i = 0; i < getNumLevels(); i++) {
            TileMatrix grid = getMatrix(i);

            double countX = rectWidth / (grid.getResolution() * grid.getTileWidth());
            double countY = rectHeight / (grid.getResolution() * grid.getTileHeight());

            double error =
                    Math.abs(countX - Math.round(countX)) + Math.abs(countY - Math.round(countY));

            if (error < bestError) {
                bestError = error;
                bestLevel = i;
            } else if (error >= bestError) {
                break;
            }
        }

        return closestRectangle(bestLevel, rectangleBounds);
    }

    /**
     * 默认原点为左上角 已修正, 左下角未测试
     * Find the rectangle of tiles that most closely covers the given rectangle
     *
     * @param level integer zoom level to consider tiles at
     * @param rectangeBounds rectangle to match
     * @return Array of long, the rectangle of tiles in tile coordinates: {minx, miny, maxx, maxy,
     *     level}
     */
    protected long[] closestRectangle(int level, BoundingBox rectangeBounds) {
        TileMatrix grid = getMatrix(level);

        double width = grid.getResolution() * grid.getTileWidth();
        double height = grid.getResolution() * grid.getTileHeight();

        long minX = (long) Math.floor((rectangeBounds.getMinX() - tileOrigin()[0]) / width);
        long minY = (long) Math.floor((tileOrigin()[1] - rectangeBounds.getMaxY()) / height);
        long maxX = (long) Math.ceil(((rectangeBounds.getMaxX() - tileOrigin()[0]) / width));
        long maxY = (long) Math.ceil(((tileOrigin()[1] - rectangeBounds.getMinY()) / height));

        if (CornerOfOrigin.BOTTOM_LEFT.equals(this.cornerOfOrigin)) {
//            minY = minY + grid.getNumTilesHigh();
            minY = minY + grid.getMatrixHeight();
//            maxY = maxY + grid.getNumTilesHigh();
            maxY = maxY + grid.getMatrixHeight();
        }

        // We substract one, since that's the tile at that position
        long[] ret = {minX, minY, maxX - 1, maxY - 1, level};

        return ret;
    }

    public BoundingBox getBounds() {
        int i;
        long tilesWide, tilesHigh;

        for (i = (getNumLevels() - 1); i > 0; i--) {
            tilesWide = getMatrix(i).getMatrixWidth();
            tilesHigh = getMatrix(i).getMatrixHeight();

            if (tilesWide == 1 && tilesHigh == 0) {
                break;
            }
        }

        tilesWide = getMatrix(i).getMatrixWidth();
        tilesHigh = getMatrix(i).getMatrixHeight();
        long[] ret = {0, 0, tilesWide - 1, tilesHigh - 1, i};

        return boundsFromRectangle(ret);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public BoundingBox getExtent() {
        return extent;
    }

    public void setExtent(BoundingBox extent) {
        this.extent = extent;
    }

    public CornerOfOrigin getCornerOfOrigin() {
        return cornerOfOrigin;
    }

    public void setCornerOfOrigin(CornerOfOrigin cornerOfOrigin) {
        this.cornerOfOrigin = cornerOfOrigin;
    }

    public TileMatrix[] getMatrixLevels() {
        return matrixLevels;
    }

    public void setMatrixLevels(TileMatrix[] matrixLevels) {
        this.matrixLevels = matrixLevels;
    }

    public void setMatrix(final int zLevel, final TileMatrix tileMatrix) {
        matrixLevels[zLevel] = tileMatrix;
    }

    public boolean isResolutionsPreserved() {
        return resolutionsPreserved;
    }

    public void setResolutionsPreserved(boolean resolutionsPreserved) {
        this.resolutionsPreserved = resolutionsPreserved;
    }

    public int getNumLevels() {
        return matrixLevels.length;
    }

    public TileMatrix getMatrix(int zLevel) {
        return this.matrixLevels[zLevel];
    }

    public double[] tileOrigin() {
        BoundingBox extent = this.getExtent();
        double[] tileOrigin;
        if (CornerOfOrigin.TOP_LEFT.equals(this.cornerOfOrigin)) {
            tileOrigin = new double[]{extent.getMinX(), extent.getMaxY()};
        }else if (CornerOfOrigin.BOTTOM_LEFT.equals(this.cornerOfOrigin)) {
            tileOrigin = new double[]{extent.getMinX(), extent.getMinY()};
        }else {
            throw new IllegalArgumentException("Unsupported corner of origin: " + this.cornerOfOrigin);
        }
        return tileOrigin;
    }
}
