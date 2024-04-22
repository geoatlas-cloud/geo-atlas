package org.geoatlas.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/** repackage from org.geowebcache.io.Resource
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 10:44
 * @since: 1.0
 **/
public interface Resource {

    /** The size of the resource in bytes. */
    public long getSize();

    /**
     * Writes the resource to a channel
     *
     * @param channel the channel to write too
     * @return The number of bytes written
     */
    public long transferTo(WritableByteChannel channel) throws IOException;

    /**
     * Overwrites the resource with bytes read from a channel.
     *
     * @param channel the channel to read from
     * @return The number of bytes read
     */
    public long transferFrom(ReadableByteChannel channel) throws IOException;

    /** An InputStream backed by the resource. */
    public InputStream getInputStream() throws IOException;

    /** An OutputStream backed by the resource. Writes are appended to the resource. */
    public OutputStream getOutputStream() throws IOException;

    /**
     * The time the resource was last modified.
     *
     * @see java.lang.System#currentTimeMillis
     */
    public long getLastModified();
}
