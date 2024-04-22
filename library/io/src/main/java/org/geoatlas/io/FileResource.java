package org.geoatlas.io;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/** repackage from org.geowebcache.io.FileResource
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 10:45
 * @since: 1.0
 **/
public class FileResource  implements Resource {

    private final File file;

    public FileResource(File file) {
        this.file = file;
    }

    /** @see org.geoatlas.io.Resource#getLastModified() */
    @Override
    public long getLastModified() {
        return file.lastModified();
    }

    /** @see org.geoatlas.io.Resource#getSize() */
    @Override
    public long getSize() {
        // avoid a (relatively expensive) call to File.exists(), file.length() returns 0 if the file
        // doesn't exist anyway
        long size = file.length();
        return size == 0 ? -1 : size;
    }

    @Override
    public long transferTo(WritableByteChannel target) throws IOException {
        // FileLock lock = in.lock();

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel in = fis.getChannel(); ) {
            final long size = in.size();
            long written = 0;
            while ((written += in.transferTo(written, size, target)) < size) {;
            }
            return size;
        }
    }

    @Override
    public long transferFrom(ReadableByteChannel channel) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel out = fos.getChannel();
             FileLock lock = out.lock(); ) {
            final int buffsize = 4096;
            long position = 0;
            long read;
            while ((read = out.transferFrom(channel, position, buffsize)) > 0) {
                position += read;
            }
            return position;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    public File getFile() {
        return file;
    }
}
