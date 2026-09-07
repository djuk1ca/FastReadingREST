package com.example.demo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.ReadingProgress;
import com.example.demo.model.ReadingProgressId;

import jakarta.transaction.Transactional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, ReadingProgressId> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO reading_progress (user_id, document_id, current_word_index, last_wpm, updated_at)
        VALUES (:userId, :docId, :wordIndex, :lastWpm, NOW())
        ON CONFLICT (user_id, document_id)
        DO UPDATE SET
            current_word_index = EXCLUDED.current_word_index,
            last_wpm = EXCLUDED.last_wpm,
            updated_at = NOW()
        """, nativeQuery = true)
    void upsert(@Param("userId") Integer userId,
                @Param("docId") Integer docId,
                @Param("wordIndex") Integer wordIndex,
                @Param("lastWpm") Integer lastWpm);

    @Query("""
        select rp from ReadingProgress rp
        where rp.id.userId = :userId and rp.id.documentId = :docId
    """)
    Optional<ReadingProgress> findByUserAndDocument(@Param("userId") Integer userId,
                                                    @Param("docId") Integer docId);
}