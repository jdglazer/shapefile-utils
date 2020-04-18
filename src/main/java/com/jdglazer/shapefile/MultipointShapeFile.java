package com.jdglazer.shapefile;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jdglazer.shapefile.utils.InvalidFileTypeException;

public class MultipointShapeFile extends ShapeFile {

/**
 * Constructor that uses multipoint type ShapeFile 
 * 
 * @param shapeFile The parent Multipoint type shape file
 * @throws FileNotFoundException
 * @throws IOException
 * @throws InvalidFileTypeException thrown if the shape file passed in is not a multipoint type file
 */
	public MultipointShapeFile( ShapeFile shapeFile ) throws FileNotFoundException, IOException, InvalidFileTypeException {
		
		super( shapeFile );
		
		if( shapeFile.getShapeType() != ShapeFile.Type.MULTIPOINT )
			
			throw new InvalidFileTypeException("Not a multipoint type file.");

	}
	
/**
 * Returns little endian double from record
 * @param recordIndex
 * @param extrema_offset
 * @return
 * @throws RecordOutOfBoundsException
 * @throws IOException
 */
	private double getCoorExtrema(int recordIndex, int extrema_offset) throws RecordOutOfBoundsException, IOException {
		
		return getDoubleFrom( fileIndex( SHP_EXTENSION ), 
							  L_END, 
							  recordOffset( recordIndex ) + 12 + extrema_offset );
	}
	
	private void _validPointOffset( int recordIndex, int pointIndex ) throws PointOutOfBoundsException, IOException, RecordOutOfBoundsException {
		
		if( numPoints( recordIndex ) <= pointIndex || pointIndex < 0 )
			
			throw new PointOutOfBoundsException();
	}
	
/**
 * Gets the number of points in a record
 * 
 * @param recordIndex The record index ( starting with index of 0 )
 * @return The number of lat/lon points in a given record
 * @throws IOException
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 */
	public int numPoints( int recordIndex ) throws IOException, RecordOutOfBoundsException {
		return getIntFrom( fileIndex( SHP_EXTENSION ), 
				  L_END, 
				  recordOffset( recordIndex ) + 48 );
	}
	
/**
 * Gets the bounding box minimum latitude for a record
 * 
 * @param recordIndex
 * @return minimum latitude for record
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 * @throws IOException
 */
	public double minLat( int recordIndex ) throws RecordOutOfBoundsException, IOException {
		return getCoorExtrema( recordIndex, 8 );
	}
	
/**
 * Gets the bounding box maximum latitude for a record
 * 
 * @param recordIndex
 * @return maximum latitude for record
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 * @throws IOException
 */
	public double maxLat( int recordIndex ) throws RecordOutOfBoundsException, IOException {
		return getCoorExtrema( recordIndex, 24 );
	}
	
/**
 * Gets the bounding box minimum longitude for a record
 * 
 * @param recordIndex
 * @return minimum longitude for record
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 * @throws IOException
 */
	public double minLon( int recordIndex ) throws RecordOutOfBoundsException, IOException {
		return getCoorExtrema( recordIndex, 0 );
	}
	
/**
 * Gets the bounding box maximum longitude for a record
 * 
 * @param recordIndex
 * @return maximum longitude for record
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 * @throws IOException
 */
	public double maxLon( int recordIndex ) throws RecordOutOfBoundsException, IOException {
		return getCoorExtrema( recordIndex, 16 );
	}
	
/**
 * Gets the ordered latitude/longitude pair from the recordIndex & pointIndex specified
 * @param recordIndex
 * @param pointIndex
 * @return
 * @throws IOException
 * @throws RecordOutOfBoundsException If the record index is less than 0 or greater than the last record
 * @throws PointOutOfBoundsException 
 */
	public float [] getLatLon( int recordIndex, int pointIndex ) throws IOException, RecordOutOfBoundsException, PointOutOfBoundsException {
		
		_validPointOffset( recordIndex, pointIndex );
		
		double longitude = getDoubleFrom( fileIndex( SHP_EXTENSION ), 
				  L_END, 
				  recordOffset( recordIndex ) + 52 + pointIndex*16 );
		
		double latitude = getDoubleFrom( fileIndex( SHP_EXTENSION ), 
				  L_END, 
				  recordOffset( recordIndex ) + 60 + pointIndex*16 );
		
		return new float[]{ (float) latitude, (float) longitude };
	}

}
