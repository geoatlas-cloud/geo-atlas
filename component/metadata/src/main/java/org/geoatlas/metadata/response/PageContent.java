package org.geoatlas.metadata.response;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/22 15:33
 * @since: 1.0
 **/
public class PageContent<T> implements Serializable {

    private static final long serialVersionUID = -5449261813111071261L;
    private final List<T> content;

    private final int number;

    private final int size;

    private final long totalElements;

    private final int totalPages;

    public PageContent(Page<T> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public PageContent(List<T> content, int number, int size, long totalElements, int totalPages) {
        this.content = Collections.unmodifiableList(content);
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
