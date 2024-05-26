package org.geoatlas.metadata.exception;

import org.geoatlas.metadata.response.ResponseStatus;
import org.geoatlas.metadata.response.ResponseContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/21 22:04
 * @since: 1.0
 **/
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseContent<String> handleException(Exception e) {
        log.error("Exception: ", e);
        return ResponseContent.failed(ResponseStatus.FAIL, e.getMessage());
    }
}
