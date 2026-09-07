package com.example.demo.repositories.projections;

public interface DailyWordsReadProjection {
    Integer getUserId();
    String getUsername();
    String getEmail();
    Long getTotalWords();
}