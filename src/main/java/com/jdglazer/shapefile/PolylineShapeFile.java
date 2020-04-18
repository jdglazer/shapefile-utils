package com.jdglazer.shapefile;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jdglazer.shapefile.utils.InvalidFileTypeException;

public class PolylineShapeFile extends ShapeFile {

	public PolylineShapeFile(ShapeFile shapeFile) throws FileNotFoundException, IOException, InvalidFileTypeException {
		
		super(shapeFile);
		
		if( shapeFile.getShapeType() != ShapeFile.Type.POLYLINE )
			
			throw new InvalidFileTypeException( "Not a polyline type shape file" );
	}
	
/**
 * @throws RecordOutOfBoundsException 
 * @throws IOException 
 * @throws PartOutOfBoundsException 
 * 
 */
	public void validPartOffset( int recordIndex, int partOffset ) throws IOException, RecordOutOfBoundsException, PartOutOfBoundsException {
		
		if( partCount(recordIndex) <= partOffset || partOffset < 0) 
			
			throw new PartOutOfBoundsException();
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
 * The number of parts for the specified record
 * @param recordIndex
 * @return
 * @throws IOException
 * @throws RecordOutOfBoundsException Thrown if the record index is less than zero or larger than the largest index
 */
	public int partCount( int recordIndex ) throws IOException, RecordOutOfBoundsException {
		
		return getIntFrom( (short) 0, L_END, recordOffset(recordIndex) + 44 );
	}
	
	public int pointCount( int recordIndex ) throws IOException, RecordOutOfBoundsException {
		
		return getIntFrom( (short) 0, L_END, recordOffset(recordIndex) + 48 );
	}
	
	public int pointCount( int recordIndex, int partIndex ) throws IOException, RecordOutOfBoundsException, PartOutOfBoundsException {
		
		validPartOffset(recordIndex, partIndex );
		
		int nextOffset = partIndex + 1 == partCount( recordIndex )
			? pointCount( recordIndex )
			: getIntFrom( (short) 0, L_END, recordOffset(recordIndex) + 56 + partIndex*4  );
		
		return nextOffset - getIntFrom( (short) 0, L_END, recordOffset(recordIndex) + 52 + partIndex*4 );
	}
	
	public float [] getLatLon( int recordIndex, int partIndex, int pointIndex ) throws IOException, RecordOutOfBoundsException, PartOutOfBoundsException {
		
		double longitude = getDoubleFrom( (short) 0, L_END, recordOffset( recordIndex ) + partOffset( recordIndex, partIndex ) + pointIndex*16 );
		
		double latitude = getDoubleFrom( (short) 0, L_END, recordOffset(recordIndex) + partOffset( recordIndex, partIndex ) +8 + pointIndex*16);
		
		return new float[] { (float) latitude, (float) longitude };
	}
	
/**
 * The offset of the part ( in bytes ) from the start of the record ( including 8 byte record header )
 * @param recordIndex
 * @param partIndex
 * @return
 * @throws IOException
 * @throws RecordOutOfBoundsException
 * @throws PartOutOfBoundsException
 */
	private int partOffset( int recordIndex, int partIndex ) throws IOException, RecordOutOfBoundsException, PartOutOfBoundsException {
		validPartOffset(recordIndex, partIndex);
		return 52 + partCount( recordIndex )*4 + getIntFrom( (short) 0, L_END, recordOffset(recordIndex) + 52 + partIndex*4 )*16;
	}
	

}
