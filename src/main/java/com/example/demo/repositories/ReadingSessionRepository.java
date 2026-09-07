package com.example.demo.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ReadingSession;
import com.example.demo.repositories.projections.DailyWordsReadProjection;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    @Query("""
        select s from ReadingSession s
        where s.id = :sessionId and s.user.id = :userId
    """)
    Optional<ReadingSession> findByIdForUser(@Param("sessionId") Long sessionId,
                                             @Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("""
        update ReadingSession s
        set s.endedAt = CURRENT_TIMESTAMP,
            s.avgWpm = :avgWpm,
            s.wordsRead = :wordsRead
        where s.id = :sessionId and s.user.id = :userId
    """)
    int endSession(@Param("userId") Integer userId,
                   @Param("sessionId") Long sessionId,
                   @Param("avgWpm") Integer avgWpm,
                   @Param("wordsRead") Integer wordsRead);

    @Query(value = """
        SELECT
            u.id AS userId,
            u.username AS username,
            u.email AS email,
            COALESCE(SUM(rs.words_read), 0) AS totalWords
        FROM reading_sessions rs
        JOIN users u ON u.id = rs.user_id
        WHERE rs.ended_at IS NOT NULL
          AND rs.ended_at >= :fromTs
          AND rs.ended_at < :toTs
        GROUP BY u.id, u.username, u.email
        ORDER BY totalWords DESC
    """, nativeQuery = true)
    List<DailyWordsReadProjection> sumWordsReadByUserForDay(@Param("fromTs") LocalDateTime fromTs,
                                                            @Param("toTs") LocalDateTime toTs);
}