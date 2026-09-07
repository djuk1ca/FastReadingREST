package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading_progress")
public class ReadingProgress {

    @EmbeddedId
    private ReadingProgressId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reading_progress_users"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("documentId")
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reading_progress_documents"))
    private Document document;

    @Column(name = "current_word_index", nullable = false)
    private Integer currentWordIndex = 0;

    @Column(name = "last_wpm")
    private Integer lastWpm;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ReadingProgress() {
    }

    public ReadingProgressId getId() {
        return id;
    }

    public void setId(ReadingProgressId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getCurrentWordIndex() {
        return currentWordIndex;
    }

    public void setCurrentWordIndex(Integer currentWordIndex) {
        this.currentWordIndex = currentWordIndex;
    }

    public Integer getLastWpm() {
        return lastWpm;
    }

    public void setLastWpm(Integer lastWpm) {
        this.lastWpm = lastWpm;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}