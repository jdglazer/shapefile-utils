package com.jdglazer.shapefile;

public class PointOutOfBoundsException extends Exception {
	
	public PointOutOfBoundsException() {
		super( "Invalid point index provided" );
	}
}
