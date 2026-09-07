package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
@Embeddable
public class ReadingProgressId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "document_id")
    private Integer documentId;

    public ReadingProgressId() {
    }

    public ReadingProgressId(Integer userId, Integer documentId) {
        this.userId = userId;
        this.documentId = documentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadingProgressId that)) return false;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, documentId);
    }
}