package com.cloudforge.application.exception;

public class ResourceAccessDeniedException extends RuntimeException{
    public  ResourceAccessDeniedException(String message){
        super(message);
    }
}
