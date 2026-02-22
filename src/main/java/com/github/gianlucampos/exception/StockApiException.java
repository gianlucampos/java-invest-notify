package com.github.gianlucampos.exception;

public class StockApiException extends RuntimeException{

    public StockApiException(Exception e) {
        super(e);
    }
}
