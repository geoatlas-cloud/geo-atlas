package org.geoatlas.pyramid.util;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/3 17:36
 * @since: 1.0
 **/
public class CommonUtils {
    public static long[][] arrayDeepCopy(long[][] array) {
        long[][] ret = new long[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, ret[i], 0, array[i].length);
        }

        return ret;
    }
}
