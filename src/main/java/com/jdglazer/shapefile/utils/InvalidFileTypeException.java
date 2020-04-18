package com.jdglazer.shapefile.utils;
public class InvalidFileTypeException extends Exception {
	
	InvalidFileTypeException() {}
	
	public InvalidFileTypeException(String msg) {
		super(msg);
	}
}