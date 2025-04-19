package com.example.springlm.common.exception;

public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DomainException notFindRow(Long id) {
        return new DomainException(String.format("%d 해당 ROW가 존재하지 않습니다", id));
    }
    
    public static DomainException notFindRow(String str) {
        return new DomainException(String.format("%s 해당 ROW가 존재하지 않습니다", str));
    }
}
