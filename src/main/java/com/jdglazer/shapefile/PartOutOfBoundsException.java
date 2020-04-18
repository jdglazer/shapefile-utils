package com.jdglazer.shapefile;

public class PartOutOfBoundsException extends Exception {
	
	public PartOutOfBoundsException() {
		
		super( "The part index provided is invalid" );
	}
}
