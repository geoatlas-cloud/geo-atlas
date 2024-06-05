package org.geoatlas.ogc.tile.dispatcher;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.mime.MimeException;
import org.geoatlas.cache.core.mime.MimeType;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.metadata.helper.FeatureBBoxHelper;
import org.geoatlas.metadata.helper.FeatureSourceHelper;
import org.geoatlas.metadata.model.FeatureBBoxInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.ogc.tile.context.FeatureTileMatrixSubsetContext;
import org.geoatlas.ogc.tile.generator.DefaultTileGenerator;
import org.geoatlas.ogc.tile.generator.GeoAtlasTileGenerator;
import org.geoatlas.pyramid.index.*;
import org.geoatlas.tile.TileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/10 21:59
 * @since: 1.0
 **/
@Component
public class GeoAtlasTileDispatcher {

    private final GeoAtlasTileGenerator tileGenerator;

    private final StorageBroker storageBroker;

    private final FeatureSourceHelper featureSourceHelper;

    private final FeatureTileMatrixSubsetContext subsetContext;

    private final static Logger log = LoggerFactory.getLogger(GeoAtlasTileDispatcher.class);

    public GeoAtlasTileDispatcher(GeoAtlasTileGenerator tileGenerator,
                                  @Autowired(required = false) StorageBroker storageBroker,
                                  FeatureSourceHelper featureSourceHelper,
                                  FeatureTileMatrixSubsetContext subsetContext) {
        this.tileGenerator = tileGenerator;
        this.storageBroker = storageBroker;
        this.featureSourceHelper = featureSourceHelper;
        this.subsetContext = subsetContext;
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
        NamespaceInfo namespaceInfo = featureSourceHelper.getNamespaceInfo(request.getNamespace());
        if (namespaceInfo == null) {
            throw new GeoAtlasCacheException("namespace not found");
        }
        FeatureLayerInfo featureLayerInfo = featureSourceHelper.getFeatureLayerInfo(request.getNamespace(), layerName, namespaceInfo);
        if (featureLayerInfo == null){
            throw new GeoAtlasCacheException("layer not found");
        }
        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(request.getSchema());
        if (tileMatrixSet == null) {
            throw new GeoAtlasCacheException("TileMatrixSet not found by identifier: " + request.getSchema());
        }
        ConveyorTile tileReq = prepareRequest(request, featureLayerInfo, tileMatrixSet, servletRequest, servletResponse);
        if (null == tileReq) {
            return null;
        }
        ConveyorTile tileResp = null;
        try {
            tileResp = tileGenerator.generator(tileReq);
        } catch (GeoAtlasCacheException exception) {
            log.error("Error dispatching tile request to Geo Atlas", exception);
            throw exception;
        } catch (Exception e) {
            log.error("Error dispatching tile request to Geo Atlas", e);
            throw new RuntimeException(e);
        }
        return tileResp;
    }

    protected ConveyorTile prepareRequest(TileRequest request, FeatureLayerInfo featureLayerInfo,
                                          TileMatrixSet tileMatrixSet,
                                          HttpServletRequest servletRequest,
                                          HttpServletResponse servletResponse) throws GeoAtlasCacheException {
        final MimeType mimeType;
        try {
            mimeType = MimeType.createFromFormat(request.getFormat());
        } catch (MimeException me) {
            // Not a Geo Atlas supported format
            throw new GeoAtlasCacheException("Not a geo atlas supported format: " + me.getMessage());
        }
        TileMatrixSubset subset = subsetContext.getTileMatrixSubset(featureLayerInfo, tileMatrixSet);
        ConveyorTile tileReq = getConveyorTile(request, mimeType, subset, servletRequest, servletResponse);
        return tileReq;
    }

    private ConveyorTile getConveyorTile(TileRequest request, MimeType mimeType,
                                         TileMatrixSubset subset,
                                         HttpServletRequest servletRequest,
                                         HttpServletResponse servletResponse) {
        final String tileMatrixSetId = request.getSchema();
        String layerName = request.getLayer();
        ConveyorTile tileReq =
                new ConveyorTile(
                        storageBroker,
                        layerName,
                        request.getNamespace(),
                        tileMatrixSetId,
                        mimeType,
                        request,
                        Collections.emptyMap(), // TODO: 10/11/23 fix this
                        subset,
                        servletRequest,
                        servletResponse);
        return tileReq;
    }

}
