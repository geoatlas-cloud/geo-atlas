package org.geoatlas.ogc.tile.util;

import org.apache.http.client.utils.DateUtils;
import org.geoatlas.cache.core.conveyor.Conveyor;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.io.ByteArrayResource;
import org.geoatlas.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Date;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/29 18:07
 * @since: 1.0
 **/
public class ResponseUtils {

    private final static Logger log = LoggerFactory.getLogger(ResponseUtils.class);

    public static void writeTile(ConveyorTile convTile) {
        try {
            writeData(convTile);
        } catch (IOException e) {
            log.error("Error writing tile", e);
            writeEmpty(convTile, e.getMessage(), convTile.getMimeType().getMimeType(), new ByteArrayResource());
        }
    }

    private static void writeData(ConveyorTile tile) throws IOException {
        HttpServletResponse servletResp = tile.servletResp;
        final HttpServletRequest servletReq = tile.servletReq;

        final Conveyor.CacheResult cacheResult = tile.getCacheResult();
        int httpCode = HttpServletResponse.SC_OK;
        Resource blob = tile.getBlob();
        String mimeType = tile.getMimeType().getMimeType(blob);
        servletResp.setHeader("geoatlas-cache-result", String.valueOf(cacheResult));
        servletResp.setHeader("geoatlas-tile-index", Arrays.toString(tile.getTileIndex()));
        final long tileTimeStamp = tile.getTSCreated();
        final String ifModSinceHeader = servletReq.getHeader("If-Modified-Since");
        // commons-httpclient's DateUtil can encode and decode timestamps formatted as per RFC-1123,
        // which is one of the three formats allowed for Last-Modified and If-Modified-Since headers
        // (e.g. 'Sun, 06 Nov 1994 08:49:37 GMT'). See
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1

        final String lastModified = DateUtils.formatDate(new Date(tileTimeStamp));
        servletResp.setHeader("Last-Modified", lastModified);

        final Date ifModifiedSince;
        if (ifModSinceHeader != null && ifModSinceHeader.length() > 0) {

            ifModifiedSince = DateUtils.parseDate(ifModSinceHeader);
            // the HTTP header has second precision
            long ifModSinceSeconds = 1000 * (ifModifiedSince.getTime() / 1000);
            long tileTimeStampSeconds = 1000 * (tileTimeStamp / 1000);
            if (ifModSinceSeconds >= tileTimeStampSeconds) {
                httpCode = HttpServletResponse.SC_NOT_MODIFIED;
                blob = null;
            }
        }

        if (httpCode == HttpServletResponse.SC_OK) {
            String ifNoneMatch = servletReq.getHeader("If-None-Match");
            String hexTag = Long.toHexString(tileTimeStamp);

            if (ifNoneMatch != null) {
                if (ifNoneMatch.equals(hexTag)) {
                    httpCode = HttpServletResponse.SC_NOT_MODIFIED;
                    blob = null;
                }
            }

            // If we get here, we want ETags but the client did not have the tile.
            servletResp.setHeader("ETag", hexTag);
        }

        int contentLength = (int) (blob == null ? -1 : blob.getSize());
        writeFixedResponse(servletResp, httpCode, mimeType, blob, cacheResult, contentLength);
    }

    private static void writeEmpty(
            ConveyorTile tile,
            String message,
            String mimeType,
            ByteArrayResource emptyTileContents) {
        tile.servletResp.setHeader("geoatlas-message", message);

        String ifNoneMatch = tile.servletReq.getHeader("If-None-Match");
        if (ifNoneMatch != null && ifNoneMatch.equals("ga-blank-tile")) {
            tile.servletResp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        } else {
            tile.servletResp.setHeader("ETag", "ga-blank-tile");
        }

        // handle no-content in case we have to return no result at all (e.g., expected for pbf)
        int status = emptyTileContents == null ? 204 : 200;

        writeFixedResponse(
                tile.servletResp,
                status,
                mimeType,
                emptyTileContents,
                Conveyor.CacheResult.OTHER);
    }

    /**
     * Helper method that writes an HTTP response setting the provided HTTP code.
     *
     * @param response HTTP response
     * @param httpCode HTTP status code
     * @param contentType HTTP response content type
     * @param resource HTTP response resource
     * @param cacheRes provides information about the tile retrieving
     */
    public static void writeFixedResponse(
            HttpServletResponse response,
            int httpCode,
            String contentType,
            Resource resource,
            Conveyor.CacheResult cacheRes) {

        int contentLength = (int) (resource == null ? -1 : resource.getSize());
        writeFixedResponse(
                response, httpCode, contentType, resource, cacheRes, contentLength);
    }

    public static void writeFixedResponse(HttpServletResponse response, int httpCode, String contentType, Resource resource, Conveyor.CacheResult cacheRes, int contentLength) {
        response.setStatus(httpCode);
        response.setContentType(contentType);
        response.setContentLength(contentLength);
        if (resource != null) {
            try (OutputStream os = response.getOutputStream();
                 WritableByteChannel channel = Channels.newChannel(os)) {
                resource.transferTo(channel);
                if (log.isDebugEnabled()) {
                    log.debug("contentLength: {}, cacheResult: {}", contentLength, cacheRes);
                }
            } catch (IOException ioe) {
                log.error("Caught IOException: {} \n\n {}", ioe.getMessage(), ioe.toString());
            }
        }
    }
}
