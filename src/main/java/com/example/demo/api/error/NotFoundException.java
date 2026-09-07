package com.example.demo.api.error;

@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) { super(msg); }
}