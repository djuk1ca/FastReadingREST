package com.example.demo.api.dto;

public record DailyWordsReadRow(
	    int userId,
	    String username,
	    String email,
	    long totalWords
	) {
	    public int getUserId() { return userId; }
	    public String getUsername() { return username; }
	    public String getEmail() { return email; }
	    public long getTotalWords() { return totalWords; }
	}