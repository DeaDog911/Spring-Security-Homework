package org.deadog.springsecurityhomework.exceptions;

public class AuthException extends RuntimeException{

    public AuthException(String message) {
        super(message);
    }
}
