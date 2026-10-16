package com.cloudforge.application.dto;

import java.util.List;

public class ProjectPageResponse {

    private List<ProjectResponse> content;
    private int page;
    private int size;
    private Long totalElements;
    private int totalPages;

    public List<ProjectResponse> getContent() {
        return content;
    }

    public void setContent(List<ProjectResponse> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
