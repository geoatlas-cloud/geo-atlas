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
