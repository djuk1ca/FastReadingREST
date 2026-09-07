package com.example.demo.api.error;

@SuppressWarnings("serial")
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String msg) { super(msg); }
}