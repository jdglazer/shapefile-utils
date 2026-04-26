package com.jdglazer.shapefile;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jdglazer.shapefile.utils.InvalidFileTypeException;

public class PolygonShapeFile extends ShapeFile {

	//private final int RECORD_HEADER_SHAPE_TYPE_OFFSET = 0;
	private final int RECORD_HEADER_MIN_LONGITUDE_OFFSET = 4;
	private final int RECORD_HEADER_MIN_LATITUDE_OFFSET = 12;
	private final int RECORD_HEADER_MAX_LONGITUDE_OFFSET = 20;
	private final int RECORD_HEADER_MAX_LATITUDE_OFFSET = 28;
	private final int RECORD_HEADER_NUM_PARTS_OFFSET = 36;
	//private final int RECORD_HEADER_NUM_POINTS_OFFSET = 40;
	private final int RECORD_HEADER_PART_OFFSET_ARRAY_OFFSET = 44;

	//reusable pool variables (prevents overcrowding of memory)
	private int recOff, recOff1;
	
	public PolygonShapeFile(ShapeFile shapeFile) throws InvalidFileTypeException, FileNotFoundException, IOException {
		super(shapeFile);
		if( shapeFile.getShapeType() != ShapeFile.Type.POLYGON )
			throw new InvalidFileTypeException("Not a polygon shape file");
	}
	
	//A function make sure the point offset is valid for a given part of a record
	private void _vPointOffset(int recordIndex, int partIndex, int pointIndex) throws RecordOutOfBoundsException, IOException{
		if( partLength(recordIndex, partIndex) <= pointIndex || pointIndex < 0)
			
			throw new RecordOutOfBoundsException();
	}
	
/**
 * throws an exception if an invalid record index is passed in
 * 
 * @param recordIndex
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	private void _vRecordIndex(int recordIndex) throws RecordOutOfBoundsException, IOException {
		if( recordIndex >= recordCount() || recordIndex < 0 )
			
			throw new RecordOutOfBoundsException();
	}
	
/**
 * 
 * gets the offset of a specified part in a record
 * 
 * @param recordIndex The index of the shape file record (starts at 0)
 * @param partIndex The index of the record part 
 * @return An integer offset of a part in the file 
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	private int partOffset(int recordIndex, int partIndex) throws RecordOutOfBoundsException, IOException {
		if(partCount(recordIndex) <= partIndex || partIndex < 0)

			throw new RecordOutOfBoundsException();

		return recordOffset(recordIndex) // start of record
				+ GENERIC_RECORD_HEADER_LENGTH // length generic record header
				+ RECORD_HEADER_PART_OFFSET_ARRAY_OFFSET // start of parts offset array in header
				+ ( partCount( recordIndex )*4 ) // after parts offsets array
		        + ( getIntFrom( fileIndex( SHP_EXTENSION ) , L_END, recOff1+(partIndex*4))*16 );
	}
	
/**
 * Determines the number of parts in a polygon record header
 * 
 * @param recordIndex The index of the record in the file (starts at index of 0)
 * @return Integer number of parts in the record
 * @throws IOException
 * @throws RecordOutOfBoundsException if an invalid recordIndex argument is supplied
 * 
 */
	public int partCount( int recordIndex ) throws IOException, RecordOutOfBoundsException {
		return getIntFrom( fileIndex(  SHP_EXTENSION ), L_END, recordOffset( recordIndex )+GENERIC_RECORD_HEADER_LENGTH+RECORD_HEADER_NUM_PARTS_OFFSET );
		
	}

/**
 * Gets the length of a part of a record in points
 * 
 * @param recordIndex The record index (starts at index of 0)
 * @param partIndex The index of the part (starts at index of 0)
 * @return the length of a part in 16 byte latitude and longitude points
 * @throws IOException
 * @throws RecordOutOfBoundsException Invalid partIndex or recordIndex arguments supplied
 * 
 */
	public int partLength(int recordIndex, int partIndex) throws IOException, RecordOutOfBoundsException {
		if( partCount(recordIndex) <= partIndex || 0 > partIndex )
			
			throw new RecordOutOfBoundsException();
		
		recOff = recordOffset(recordIndex);
		
		if( partCount( recordIndex ) == 1 ) 
						
			return getIntFrom( fileIndex(  SHP_EXTENSION ), L_END, recOff+48 );
		
		
		if( partCount( recordIndex ) == partIndex+1 ) 
			
			 return getIntFrom( fileIndex( SHP_EXTENSION ), L_END, recOff+48 ) - 
					 getIntFrom( fileIndex(  SHP_EXTENSION ), L_END, recOff+52+partIndex*4 );
		
		else 
			
			return getIntFrom( fileIndex( SHP_EXTENSION ), L_END, recOff+56+partIndex*4 ) - 
			  			getIntFrom( fileIndex( SHP_EXTENSION ), L_END, recOff+52+partIndex*4 );
	
	}
/**
 * Gets a specified pair of coordinates from a part of a record. 
 * 
 * @param recordIndex The index of the record (starts at 0)
 * @param partIndex The index of the part (starts at 0)
 * @param pointIndex The index of the point (starts at 0)
 * @return Two part double array contain the latitude of the point in the first part and the longitude in the second 
 * 			(ie. getLatLon(...)[0] for latitude, getLatLon(...)[1] for longitude)
 * @throws RecordOutOfBoundsException Invalid recordIndex, partIndex, and/or pointIndex argument(s) supplied
 * @throws IOException
 * 
 */
	public double [] getLatLon(int recordIndex, int partIndex, int pointIndex) throws RecordOutOfBoundsException, IOException {
		_vPointOffset(recordIndex, partIndex, pointIndex);

		recOff = partOffset(recordIndex, partIndex);

		recOff += pointIndex * 16;

		return new double[]{

				getDoubleFrom(fileIndex(SHP_EXTENSION), L_END, recOff + 8),

				getDoubleFrom(fileIndex(SHP_EXTENSION), L_END, recOff)

		};

	}
/**
 * Gets the minimum latitude extreme for a given record
 * 
 * @param recordIndex
 * @return the minimum latitude of a record
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	public double minLat(int recordIndex) throws RecordOutOfBoundsException, IOException {
		return getDoubleFrom( fileIndex( SHP_EXTENSION ),
				L_END,
				recordOffset( recordIndex ) + GENERIC_RECORD_HEADER_LENGTH + RECORD_HEADER_MIN_LATITUDE_OFFSET );
	}
	
/**
 * Gets the maximum latitude extreme for a given record
 * 
 * @param recordIndex
 * @return the maximum latitude of a record
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	public double maxLat(int recordIndex) throws RecordOutOfBoundsException, IOException {
		return getDoubleFrom( fileIndex( SHP_EXTENSION ),
				L_END,
				recordOffset( recordIndex ) + GENERIC_RECORD_HEADER_LENGTH + RECORD_HEADER_MAX_LATITUDE_OFFSET );
	}
	
/**
 * Gets the minimum longitude extreme for a given record
 * 
 * @param recordIndex
 * @return returns the minimum longitude of a given record 
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	public double minLon(int recordIndex) throws RecordOutOfBoundsException, IOException {
		return getDoubleFrom( fileIndex( SHP_EXTENSION ),
				L_END,
				recordOffset( recordIndex ) + GENERIC_RECORD_HEADER_LENGTH + RECORD_HEADER_MIN_LONGITUDE_OFFSET );
	}
	
/**
 * Gets the maximum longitude extreme for a given record
 * 
 * @param recordIndex
 * @return returns the maximum longitude of a record
 * @throws RecordOutOfBoundsException
 * @throws IOException
 * 
 */
	public double maxLon(int recordIndex) throws RecordOutOfBoundsException, IOException {
		return getDoubleFrom( fileIndex( SHP_EXTENSION ),
				L_END,
				recordOffset( recordIndex ) +  GENERIC_RECORD_HEADER_LENGTH + RECORD_HEADER_MAX_LONGITUDE_OFFSET );
	}

}