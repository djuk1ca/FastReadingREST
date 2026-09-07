package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    @Query("""
        select d from Document d
        where d.visibility = 'PUBLIC' and d.status = 'READY'
        order by d.updatedAt desc
    """)
    List<Document> findPublicReady();

    @Query("""
        select d from Document d
        where d.owner.id = :userId and d.status = 'READY'
        order by d.updatedAt desc
    """)
    List<Document> findByUserId(@Param("userId") Integer userId);

    @Query("""
        select d from Document d
        where d.id = :docId and d.visibility = 'PUBLIC' and d.status = 'READY'
    """)
    Optional<Document> findPublicReadyById(@Param("docId") Integer docId);

    @Query("""
        select d from Document d
        where d.id = :docId
          and d.visibility = 'PRIVATE'
          and d.status = 'READY'
          and d.owner.id = :userId
    """)
    Optional<Document> findPrivateReadyByIdForUser(@Param("docId") Integer docId,
                                                   @Param("userId") Integer userId);
}