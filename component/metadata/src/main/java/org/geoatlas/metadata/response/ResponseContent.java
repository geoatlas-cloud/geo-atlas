package org.geoatlas.metadata.response;

import java.io.Serializable;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/21 21:25
 * @since: 1.0
 **/
public class ResponseContent<T> implements Serializable {

    private static final long serialVersionUID = 8736110477585852096L;

    private Boolean success;
    private int status;

    private String message;

    private long timestamp;

    private T data;

    public ResponseContent() {
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static <T> ResponseContent<T> ok() {
        return build(Boolean.TRUE, ResponseStatus.SUCCESS, null);
    }

    public static <T> ResponseContent<T> ok(T data) {
       return build(Boolean.TRUE, ResponseStatus.SUCCESS, data);
    }

    public static <T> ResponseContent<T> ok(T data, String message) {
        return build(Boolean.TRUE, ResponseStatus.SUCCESS.getStatus(), message, data);
    }

    public static <T> ResponseContent<T> failed(T data) {
        return build(Boolean.FALSE, ResponseStatus.FAIL, data);
    }

    public static <T> ResponseContent<T> failed(String message) {
        return build(Boolean.FALSE, ResponseStatus.FAIL.getStatus(), message, null);
    }

    public static <T> ResponseContent<T> failed(int status, String message) {
        return build(Boolean.FALSE, status, message, null);
    }

    public static <T> ResponseContent<T> failed(ResponseStatus status) {
        return build(Boolean.FALSE, status, null);
    }

    public static <T> ResponseContent<T> failed(ResponseStatus status, String message) {
        return build(Boolean.FALSE, status.getStatus(), message, null);
    }

    public static <T> ResponseContent<T> failed(ResponseStatus status, String message, T data) {
        return build(Boolean.FALSE, status.getStatus(), message, data);
    }

    public static <T> ResponseContent<T> build(Boolean success, ResponseStatus status, T data) {
        return build(success, status.getStatus(), status.getMessage(), data);
    }

    public static <T> ResponseContent<T> build(Boolean success, int status, String message, T data) {
        ResponseContent<T> wrapper = new ResponseContent<>();
        wrapper.setSuccess(success);
        wrapper.setStatus(status);
        wrapper.setMessage(message);
        wrapper.setData(data);
        wrapper.setTimestamp(System.currentTimeMillis());
        return wrapper;
    }
}
