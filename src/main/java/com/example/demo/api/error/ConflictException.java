package com.example.demo.api.error;

@SuppressWarnings("serial")
public class ConflictException extends RuntimeException {
	public ConflictException(String message) { super(message); }
}
