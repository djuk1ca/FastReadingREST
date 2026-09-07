package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Integer> {

    @Query("""
        select c from DocumentChunk c
        where c.document.id = :docId
          and c.startWordIndex <= :wordIndex
        order by c.startWordIndex desc
    """)
    List<DocumentChunk> findChunkAtOrBefore(@Param("docId") Integer docId,
                                            @Param("wordIndex") Integer wordIndex,
                                            Pageable pageable);

    Optional<DocumentChunk> findByDocumentIdAndChunkIndex(Integer docId, Integer chunkIndex);

    @Query("""
        select c from DocumentChunk c
        where c.document.id = :docId and c.chunkIndex >= :fromChunk
        order by c.chunkIndex asc
    """)
    List<DocumentChunk> findChunksFrom(@Param("docId") Integer docId,
                                       @Param("fromChunk") Integer fromChunk,
                                       Pageable pageable);

    @Query("""
        select coalesce(max(c.chunkIndex), -1) from DocumentChunk c
        where c.document.id = :docId
    """)
    Integer findMaxChunkIndex(@Param("docId") Integer docId);
}