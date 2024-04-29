package org.geoatlas.tile.util;

import org.geoatlas.io.Resource;
import org.geoatlas.tile.TileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/29 18:07
 * @since: 1.0
 **/
public class ResponseUtils {

    private final static Logger log = LoggerFactory.getLogger(ResponseUtils.class);

    public static void writeData(TileObject tile, HttpServletResponse response) {
        int httpCode = 200;
        Resource blob = tile.getBlob();
        int contentLength = (int)(blob == null ? -1L : blob.getSize());
        writeFixedResponse(response, httpCode, tile.getFormat(), blob, contentLength);
    }

    public static void writeFixedResponse(HttpServletResponse response, int httpCode, String contentType, Resource resource, int contentLength) {
        response.setStatus(httpCode);
        response.setContentType(contentType);
        response.setContentLength(contentLength);
        if (resource != null) {
            try {
                OutputStream os = response.getOutputStream();
                Throwable var8 = null;

                try {
                    WritableByteChannel channel = Channels.newChannel(os);
                    Throwable var10 = null;

                    try {
                        resource.transferTo(channel);
                    } catch (Throwable var35) {
                        var10 = var35;
                        throw var35;
                    } finally {
                        if (channel != null) {
                            if (var10 != null) {
                                try {
                                    channel.close();
                                } catch (Throwable var34) {
                                    var10.addSuppressed(var34);
                                }
                            } else {
                                channel.close();
                            }
                        }

                    }
                } catch (Throwable var37) {
                    var8 = var37;
                    throw var37;
                } finally {
                    if (os != null) {
                        if (var8 != null) {
                            try {
                                os.close();
                            } catch (Throwable var33) {
                                var8.addSuppressed(var33);
                            }
                        } else {
                            os.close();
                        }
                    }

                }
            } catch (IOException var39) {
                log.debug("Caught IOException: " + var39.getMessage() + "\n\n" + var39.toString());
            }
        }

    }


}
