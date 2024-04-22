package org.geoatlas.pyramid.index;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import si.uom.NonSI;
import si.uom.SI;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Angle;

/** repackage from org.geowebcache.grid.GridSetFactory
 *
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/20 17:14
 * @since: 1.0
 **/
public class TileMatrixSetFactory {

    /**
     * Default pixel size in meters, producing a default of 90.7 DPI
     *
     */
    public static final double DEFAULT_PIXEL_SIZE_METER = 0.00028;

    public static int DEFAULT_LEVELS = 22;

    public static final double EPSG_4326_TO_METERS = 6378137.0 * 2.0 * Math.PI / 360.0;

    public static final double EPSG3857_TO_METERS = 1;

    private static TileMatrixSet basTileMatrixSet(String title, CoordinateReferenceSystem crs) {
        TileMatrixSet matrixSet = new TileMatrixSet();

        matrixSet.setTitle(title);
        matrixSet.setCrs(crs);

        return matrixSet;
    }

    /**
     * Note that you should provide EITHER resolutions or scales. Providing both will cause a
     * precondition violation exception.
     */
    public static TileMatrixSet createTileMatrixSet(
            final String title,
            final CoordinateReferenceSystem crs,
            final BoundingBox extent,
            CornerOfOrigin cornerOfOrigin,
            double[] resolutions,
            double[] scaleDenominators,
            Double metersPerUnit,
            double cellSize,
            String[] scaleNames,
            int tileWidth,
            int tileHeight,
            boolean yCoordinateFirst) {

        if (title == null) {
            throw new IllegalArgumentException("title is null");
        }
        if (crs == null) {
            throw new IllegalArgumentException("crs is null");
        }
        if (extent == null){
            throw new IllegalArgumentException("extent is null");
        }
        if (extent.isNull() || !extent.isSane()) {
            throw new IllegalArgumentException("extent is invalid" + extent);
        }
        if (resolutions == null && scaleDenominators == null) {
            throw new IllegalArgumentException("The TileMatrixSet definition must have either resolutions or scale denominators");
        }
        if (resolutions != null && scaleDenominators != null) {
            throw new IllegalArgumentException("Only one of resolutions or scaleDenominators should be provided, not both");
        }

        for (int i = 1; resolutions != null && i < resolutions.length; i++) {
            if (resolutions[i] >= resolutions[i - 1]) {
                throw new IllegalArgumentException(
                        "Each resolution should be lower than it's prior one. Res["
                                + i
                                + "] == "
                                + resolutions[i]
                                + ", Res["
                                + (i - 1)
                                + "] == "
                                + resolutions[i - 1]
                                + ".");
            }
        }

        for (int i = 1; scaleDenominators != null && i < scaleDenominators.length; i++) {
            if (scaleDenominators[i] >= scaleDenominators[i - 1]) {
                throw new IllegalArgumentException(
                        "Each scale denominator should be lower than it's prior one. Scale["
                                + i
                                + "] == "
                                + scaleDenominators[i]
                                + ", Scale["
                                + (i - 1)
                                + "] == "
                                + scaleDenominators[i - 1]
                                + ".");
            }
        }

        TileMatrixSet tileMatrixSet = basTileMatrixSet(title, crs);

        // true：保留分辨率并计算 scaleDenominators
        // false：分辨率是根据 scaleDenominators 分母计算的
        tileMatrixSet.setResolutionsPreserved(resolutions != null);

//        gridSet.set(pixelSize);

        tileMatrixSet.setExtent(extent);
        tileMatrixSet.setCornerOfOrigin(cornerOfOrigin);

//        tileMatrixSet.setyCoordinateFirst(yCoordinateFirst);

        if (resolutions == null) {
            tileMatrixSet.setMatrixLevels(new TileMatrix[scaleDenominators.length]);
        } else {
            tileMatrixSet.setMatrixLevels(new TileMatrix[resolutions.length]);
        }

        if (metersPerUnit == null) {
            metersPerUnit = getMetersPerUnit(crs);
        }

        for (int i = 0; i < tileMatrixSet.getNumLevels(); i++) {
            TileMatrix curMatrix = new TileMatrix();

            if (scaleDenominators != null) {
                curMatrix.setScaleDenominator(scaleDenominators[i]);
                curMatrix.setResolution(cellSize * (scaleDenominators[i] / metersPerUnit));
            } else {
                // 计算比例尺 (单位像素下表示的实际空间距离 * 每个单位情况表示多少米) / (像素大小，或称像元大小)
                // 约去像元, 即获取到比例尺分母
                curMatrix.setResolution(resolutions[i]);
                curMatrix.setScaleDenominator(
                        (resolutions[i] * metersPerUnit) / DEFAULT_PIXEL_SIZE_METER);
            }

            // 每个瓦片在地图单位中的宽度(表示一个瓦片的宽度, 如256像素多少米或多少度)
            final double mapUnitWidth = tileWidth * curMatrix.getResolution();
            // 每个瓦片在地图单位中的高度(表示一个瓦片的高度, 以像素为单位, 如256像素多少米或多少度)
            final double mapUnitHeight = tileHeight * curMatrix.getResolution();

            // 瓦片矩阵的宽度, 通过减去一小部分（0.01倍瓦片的宽度）来留出一些空间，然后除以每个瓦片的宽度，最后使用Math.ceil()函数向上取整以确保覆盖整个地图范围。结果被转换为整数类型以得到瓦片矩阵的宽度
            final int matrixWidth =
                    (int) Math.ceil((extent.getWidth() - mapUnitWidth * 0.01) / mapUnitWidth);
            // 瓦片矩阵的高度, 通过减去一小部分（0.01倍瓦片的高度）来留出一些空间，然后除以每个瓦片的高度，最后使用Math.ceil()函数向上取整以确保覆盖整个地图范围。结果被转换为整数类型以得到瓦片矩阵的高度
            final int matrixHigh =
                    (int) Math.ceil((extent.getHeight() - mapUnitHeight * 0.01) / mapUnitHeight);

            curMatrix.setMatrixWidth(matrixWidth);
            curMatrix.setMatrixHeight(matrixHigh);

            if (scaleNames == null || scaleNames[i] == null) {
                curMatrix.setTitle(tileMatrixSet.getTitle() + ":" + i);
            } else {
                curMatrix.setTitle(scaleNames[i]);
            }

            curMatrix.setCellSize(cellSize);
            curMatrix.setTileWidth(tileWidth);
            curMatrix.setTileHeight(tileHeight);
            curMatrix.setCornerOfOrigin(cornerOfOrigin);

            tileMatrixSet.setMatrix(i, curMatrix);
        }

        return tileMatrixSet;
    }

    public static TileMatrixSet createTileMatrixSet(
            final String title,
            final CoordinateReferenceSystem crs,
            final BoundingBox extent,
            final CornerOfOrigin cornerOfOrigin,
            final int levels,
            final Double metersPerUnit,
            final double cellSize,
            final int tileWidth,
            final int tileHeight,
            final boolean yCoordinateFirst) {

        // extent 宽度
        final double extentWidth = extent.getWidth();
        // extent 高度
        final double extentHeight = extent.getHeight();

        // 计算分辨率, 因为此处是自顶向下构建的, 顶部即0级(也就是单张瓦片表示全部, 即单位像素内表示的地图单位(米或度))
        double resX = extentWidth / tileWidth;
        double resY = extentHeight / tileHeight;

        final int tilesWide, tilesHigh;
        // 确定0级分辨率, 以较小边为主(因为存在长短边, 也就是非正方形的情况,
        // 所以0级存在一张图无法全部显示的情况, 分辨率自然以小边为主, 并会根据最终的分辨率调整整体的Extent, 确保矩阵的横纵方向的瓦片数是整数)
        if (resX <= resY) {
            // use one tile wide by N tiles high
            tilesWide = 1;
            tilesHigh = (int) Math.round(resY / resX);
            // previous resY was assuming 1 tile high, recompute with the actual number of tiles
            // high
            resY = resY / tilesHigh;
        } else {
            // use one tile high by N tiles wide
            tilesHigh = 1;
            tilesWide = (int) Math.round(resX / resY);
            // previous resX was assuming 1 tile wide, recompute with the actual number of tiles
            // wide
            resX = resX / tilesWide;
        }

        // the maximum of resX and resY is the one that adjusts better
        final double res = Math.max(resX, resY);

        final double adjustedExtentWidth = tilesWide * tileWidth * res;
        final double adjustedExtentHeight = tilesHigh * tileHeight * res;

        BoundingBox adjExtent = new BoundingBox(extent);
        adjExtent.setMaxX(adjExtent.getMinX() + adjustedExtentWidth);
        // Do we keep the top or the bottom fixed?
        // // 这里想要表达的就是原点在于左上角还是左下角（确定起算位置）, 因为这样会影响y方向上的范围计算, 就是进行y方向上范围计算
        if (CornerOfOrigin.TOP_LEFT.equals(cornerOfOrigin)) {
            adjExtent.setMinY(adjExtent.getMaxY() - adjustedExtentHeight);
        } else {
            adjExtent.setMaxY(adjExtent.getMinY() + adjustedExtentHeight);
        }

        // 根据给定的层级参数, 构建各层级下分辨率值
        double[] resolutions = new double[levels];
        // 设置0级的分辨率
        resolutions[0] = res;

        for (int i = 1; i < levels; i++) {
            // 采用常规均匀四分（四叉树）方式进行构建
            resolutions[i] = resolutions[i - 1] / 2;
        }

        return createTileMatrixSet(
                title,
                crs,
                adjExtent,
                cornerOfOrigin,
                resolutions,
                null,
                metersPerUnit,
                cellSize,
                null,
                tileWidth,
                tileHeight,
                yCoordinateFirst);
    }

    /** @throws IllegalArgumentException if the equivalence can't be established */
    public static Double getMetersPerUnit(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }

        final CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis(0);
        final Unit<?> unit = axis.getUnit();

        return metersPerUnit(unit);
    }

    /** @throws IllegalArgumentException if the provided unit can't be converted to meters */
    static Double metersPerUnit(final Unit<?> unit) {
        double meters;
        final Unit<Angle> degree = NonSI.DEGREE_ANGLE;

        // FIXME: 2024/4/20 也就是说，目前仅仅支持4490和4326, 这两个的椭球长半轴一致
        if (degree.equals(unit)) {
            meters = TileMatrixSetFactory.EPSG_4326_TO_METERS;
        } else {
            try {
                meters = unit.getConverterToAny(SI.METRE).convert(1);
            } catch (Exception e) {
                UnitConverter converter;
                try {
                    converter = unit.getConverterToAny(degree);
                    double toDegree = converter.convert(1);
                    meters = toDegree * TileMatrixSetFactory.EPSG_4326_TO_METERS;
                } catch (UnconvertibleException | IncommensurableException e1) {
                    throw new IllegalArgumentException(e1);
                }
            }
        }
        return meters;
    }
}
