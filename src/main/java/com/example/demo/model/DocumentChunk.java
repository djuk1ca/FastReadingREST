package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "document_chunks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_document_chunks_doc_chunk", columnNames = {"document_id", "chunk_index"})
    }
)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_document_chunks_documents"))
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "start_word_index", nullable = false)
    private Integer startWordIndex;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    @Lob
    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    public DocumentChunk() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getStartWordIndex() {
        return startWordIndex;
    }

    public void setStartWordIndex(Integer startWordIndex) {
        this.startWordIndex = startWordIndex;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }
}