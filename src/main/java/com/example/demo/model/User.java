package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uq_users_username", columnNames = "username")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    private List<Document> privateDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "uploadedByAdmin")
    private List<Document> publicUploadedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ReadingSession> readingSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ReadingProgress> readingProgressList = new ArrayList<>();

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Document> getPrivateDocuments() {
        return privateDocuments;
    }

    public void setPrivateDocuments(List<Document> privateDocuments) {
        this.privateDocuments = privateDocuments;
    }

    public List<Document> getPublicUploadedDocuments() {
        return publicUploadedDocuments;
    }

    public void setPublicUploadedDocuments(List<Document> publicUploadedDocuments) {
        this.publicUploadedDocuments = publicUploadedDocuments;
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