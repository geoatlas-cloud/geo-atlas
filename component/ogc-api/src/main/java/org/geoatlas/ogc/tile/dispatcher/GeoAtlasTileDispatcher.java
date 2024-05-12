package org.geoatlas.ogc.tile.dispatcher;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.mime.MimeException;
import org.geoatlas.cache.core.mime.MimeType;
import org.geoatlas.ogc.tile.adapter.GeoAtlasCachePyramidAdapter;
import org.geoatlas.tile.TileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/10 21:59
 * @since: 1.0
 **/
@Component
public class GeoAtlasTileDispatcher {

    private final GeoAtlasCachePyramidAdapter cachePyramidAdapter;

    private final static Logger log = LoggerFactory.getLogger(GeoAtlasTileDispatcher.class);

    public GeoAtlasTileDispatcher(GeoAtlasCachePyramidAdapter cachePyramidAdapter) {
        this.cachePyramidAdapter = cachePyramidAdapter;
    }

    public final ConveyorTile dispatch(final TileRequest request, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) throws GeoAtlasCacheException {

        final String layerName = request.getLayer();
        /*
         * This is a quick way of checking if the request was for a single layer. We can't really
         * use request.getLayers() because in the event that a layerGroup was requested, the request
         * parser turned it into a list of actual Layers
         */
        if (layerName.indexOf(',') != -1) {
            throw new GeoAtlasCacheException("more than one layer requested");
        }

        ConveyorTile tileReq = prepareRequest(request, servletRequest, servletResponse);
        if (null == tileReq) {
            return null;
        }
        ConveyorTile tileResp = null;
        try {
            tileResp = cachePyramidAdapter.getTile(tileReq);
        } catch (GeoAtlasCacheException exception) {
            log.error("Error dispatching tile request to Geo Atlas", exception);
            throw exception;
        } catch (Exception e) {
            log.error("Error dispatching tile request to Geo Atlas", e);
            throw new RuntimeException(e);
        }
        return tileResp;
    }

    protected ConveyorTile prepareRequest(TileRequest request, HttpServletRequest servletRequest,
                                          HttpServletResponse servletResponse) throws GeoAtlasCacheException {
        final MimeType mimeType;
        try {
            mimeType = MimeType.createFromFormat(request.getFormat());
        } catch (MimeException me) {
            // Not a Geo Atlas supported format
            throw new GeoAtlasCacheException("Not a geo atlas supported format: " + me.getMessage());
        }

        ConveyorTile tileReq = getConveyorTile(request, mimeType, servletRequest, servletResponse);
        return tileReq;
    }

    private ConveyorTile getConveyorTile(TileRequest request, MimeType mimeType,
                                         HttpServletRequest servletRequest,
                                         HttpServletResponse servletResponse) {
        final String tileMatrixSetId = request.getSchema();
        String layerName = request.getLayer();
        ConveyorTile tileReq =
                new ConveyorTile(
                        cachePyramidAdapter.getStorageBroker(),
                        layerName,
                        tileMatrixSetId,
                        mimeType,
                        request,
                        Collections.emptyMap(), // TODO: 10/11/23 fix this
                        servletRequest,
                        servletResponse);
        return tileReq;
    }
}
