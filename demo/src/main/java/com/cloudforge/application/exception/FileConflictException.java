package com.cloudforge.application.exception;



public class FileConflictException extends RuntimeException{

    public FileConflictException(String message){
        super(message);
    }
}
