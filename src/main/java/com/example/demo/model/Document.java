package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_documents_users"))
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_admin_id", foreignKey = @ForeignKey(name = "fk_documents_uploaded_by_admin"))
    private User uploadedByAdmin;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PROCESSING";

    @Column(name = "visibility", nullable = false, length = 10)
    private String visibility = "PRIVATE";

    @Column(name = "is_sample", nullable = false)
    private boolean sample = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chunkIndex ASC")
    private List<DocumentChunk> documentChunks = new ArrayList<>();

    @OneToMany(mappedBy = "document")
    private List<ReadingSession> readingSessions = new ArrayList<>();

    @OneToMany(mappedBy = "document")
    private List<ReadingProgress> readingProgressList = new ArrayList<>();

    public Document() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getUploadedByAdmin() {
        return uploadedByAdmin;
    }

    public void setUploadedByAdmin(User uploadedByAdmin) {
        this.uploadedByAdmin = uploadedByAdmin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DocumentChunk> getDocumentChunks() {
        return documentChunks;
    }

    public void setDocumentChunks(List<DocumentChunk> documentChunks) {
        this.documentChunks = documentChunks;
    }

    public List<ReadingSession> getReadingSessions() {
        return readingSessions;
    }

    public void setReadingSessions(List<ReadingSession> readingSessions) {
        this.readingSessions = readingSessions;
    }

    public List<ReadingProgress> getReadingProgressList() {
        return readingProgressList;
    }

    public void setReadingProgressList(List<ReadingProgress> readingProgressList) {
        this.readingProgressList = readingProgressList;
    }
}