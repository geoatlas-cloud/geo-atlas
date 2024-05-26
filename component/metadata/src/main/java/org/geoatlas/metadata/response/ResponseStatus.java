package org.geoatlas.metadata.response;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/21 21:27
 * @since: 1.0
 **/
public enum ResponseStatus {
    SUCCESS(200, "success"),
    NOT_FOUND(404, "not found"),
    FAIL(500, "fail");

    private final int status;
    private final String message;

    ResponseStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
