package com.jdglazer.shapefile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.jamel.dbf.DbfReader;

import com.jdglazer.shapefile.utils.InvalidFileTypeException;
import com.jdglazer.shapefile.utils.FileModel;


public class ShapeFile extends FileModel {
	
/**
 * A set of integers representing different shapefile types ( as per esri specs )
 */
	
	public enum Type {
		POINT(1),
		POLYLINE(3),
		POLYGON(5),
		MULTIPOINT(8);
		
		int id;
		private static Map<Integer, Type> map = new HashMap<Integer, Type>();
				
		Type(int id) {
			this.id = id;
		}
		
		static {
			for (Type type: Type.values()) {
				map.put(type.id, type);
			}
		}
		
		public static Type valueOf(int id) {
			return map.get(id);
		}
		
		public int getValue() {
			return id;
		}
	}

	
/**
 * stores the valid main shapefile extension
 * 
 */
	
	public static final String SHP_EXTENSION = ".shp";
	
/**
 * Header file offsets for particular data points
 */
	public static final int ESRI_CODE_OFFSET          = 0;
	public static final int SHP_HEADER_VERSION_OFFSET = 28;
	public static final int SHP_HEADER_TYPE_OFFSET    = 32;
	public static final int SHP_HEADER_LON_MIN_OFFSET = 36;
	public static final int SHP_HEADER_LAT_MIN_OFFSET = 44;
	public static final int SHP_HEADER_LON_MAX_OFFSET = 52;
	public static final int SHP_HEADER_LAT_MAX_OFFSET = 60;
/**
 * stores the valid shape index file extension
 * 
 */
	
	public static final String SHX_EXTENSION = ".shx";
	
/**
 * stores shx header length
 */
	
	public static final int SHX_HEADER_LENGTH = 100;
	
/**
 * stores shx record length
 */
	public static final int SHX_RECORD_LENGTH = 8;
	
/**
 * stores offset of shp record offset value indexed from start of shx record
 */
	
	public static final int SHX_RECORD_LENGTH_OFFSET = 4;
	
/**
 * stores the valid shape database file extension
 * 
 */
	
	public static final String DBF_EXTENSION = ".dbf";
	
/**
 * stores the ESRI code that should be found at the top of the shapefile, and it's index file
 * 
 */
	
	public static final int ESRI_CODE = 9994;
	
	//File Meta variables
	
/**
 * Full path to shapefile and associated index and dbf files
 */
	
	private String shpPath = null, 
				   shxPath = null, 
				   dbfPath = null;
	
/**
 * The dbf database file reader
 */
	
	private DbfReader dbfReader;
	
/**
 * The ESRI Shapefile version declared in the file
 * 
 */
	
	private int version;
	
/**
 * The index associated with the shape type declared in the file
 *  
 */
	
	private Type shapeType;
	
/**
 * The minimum latitude found in the shapefile
 * 
 */
	
	private double latMin;
	
/**
 * The minimum longitude found in the shapefile
 * 
 */
	
	private double lonMin;
				   
/**
 * 
 * The maximum latitude found in the shapefile
 * 
 */
	
	private double latMax;
				   
/**
 * The maximum longitude found in the shapefile
 * 
 */

	private double lonMax;
	
/**
 * Takes the names of the associated .shp, .shx, and .dbf files separately. It is important to
 * pass in the appropriately associated files to ensure the proper functioning of this object. This
 * constructor allows for differences between the names of the main, index and database files.
 * 
 * @param shp The file address of the main shape file
 * @param shx The file address of the shape index file 
 * @param dbf The file address of the shape database file
 * @throws InvalidFileTypeException
 * @throws FileNotFoundException
 * @throws IOException
 * @throws RecordOutOfBoundsException
 * 
 */

	public ShapeFile(String shp, String shx, String dbf) throws InvalidFileTypeException, 
																FileNotFoundException, 
																IOException, 
																RecordOutOfBoundsException {
				
		super(new FileInputStream[]{new FileInputStream(shp), new FileInputStream(shx)});
		
		verifyFileExt(shp.trim(), SHP_EXTENSION);
		
		verifyFileExt(shx.trim(), SHX_EXTENSION);
		
		verifyFileExt(dbf.trim(), DBF_EXTENSION);
		
		if(!hasESRICode())
			
			throw new InvalidFileTypeException("no ESRI Code found ("+ESRI_CODE+")");
		
		this.shpPath = shp;
		
		this.shxPath = shx;
		
		this.dbfPath = dbf;
		
		this.dbfReader = new DbfReader(new File(dbf));
		
		writeFileMeta();
		
	}
	
/**
 * Takes the address and name of the shapefile  and assumes associated 
 * database and index files have the same name in keeping with convention set forth in ESRI shapefile specifications
 * 
 * @param name
 * @throws InvalidFileTypeException
 * @throws FileNotFoundException
 * @throws IOException
 * @throws RecordOutOfBoundsException
 * 
 */
	
	public ShapeFile(String name) throws InvalidFileTypeException, FileNotFoundException, IOException, RecordOutOfBoundsException {
		this(name.trim(), name.trim().substring( 0, name.trim().length() - SHX_EXTENSION.length())+SHX_EXTENSION, name.trim().substring( 0, name.trim().length() - DBF_EXTENSION.length())+DBF_EXTENSION);
	}

/**
 * The basic copy constructor for this object. Please note that if there is no check to verify all necessary fields are set
 * 
 * @param shapeFile The shapefile to copy in
 * @throws IOException 
 * @throws FileNotFoundException 
 * @throws InvalidFileTypeException 
 */
	public ShapeFile( ShapeFile shapeFile ) throws FileNotFoundException, IOException, InvalidFileTypeException {
		
		super( new FileInputStream[]{ new FileInputStream( shapeFile.shpPath ),
									  new FileInputStream( shapeFile.shxPath ), 
									  new FileInputStream( shapeFile.dbfPath )
									 } );
		
		if(!hasESRICode())
			throw new InvalidFileTypeException("no ESRI Code found (9994)");			
		
		this.version = shapeFile.version;
		
		this.shapeType = shapeFile.shapeType;
		
		this.latMin = shapeFile.latMin;
		
		this.lonMin = shapeFile.lonMin;
		
		this.latMax = shapeFile.latMax;
		
		this.lonMax = shapeFile.lonMax;
		
		this.dbfReader = shapeFile.dbfReader;
		
		shapeFile.close();

	}
	
	//A function to get the index of the relevant shape file input stream component in the parent's fis list array
	
	protected short fileIndex(String extension) {
	    return (short) (extension.equals( SHX_EXTENSION ) ? 1 : 0);
	}
	
/**
 * A function to verify esri 9994 code
 * @return true if file conatins 9994 esri code, false otherwise
 * @throws IOException
 */
	private boolean hasESRICode() throws IOException {
		
		return getIntFrom(  fileIndex( SHP_EXTENSION ), B_END,  ESRI_CODE_OFFSET ) == ESRI_CODE;
	}
	
/**
 * Pulls the shapefile meta data from the main file header and stores in the object
 * @throws IOException
 */
	private void writeFileMeta() throws IOException {
		
		version = getIntFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_VERSION_OFFSET );
		
		shapeType = Type.valueOf(getIntFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_TYPE_OFFSET ));
		
		lonMin = getDoubleFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_LON_MIN_OFFSET );
		
		latMin = getDoubleFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_LAT_MIN_OFFSET );
		
		lonMax = getDoubleFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_LON_MAX_OFFSET);
		
		latMax = getDoubleFrom( fileIndex( SHP_EXTENSION ), L_END, SHP_HEADER_LAT_MAX_OFFSET );		
		
	}
	
/**
 * 
 */
	public String printShapeFileMeta() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Version: ");
		sb.append(this.version);
		sb.append("\n");
		sb.append("Shape Type: ");
		sb.append(this.shapeType);
		sb.append("\n");
		sb.append("Longitude:\n");
		sb.append("  Minimum: " );
		sb.append(this.lonMin);
		sb.append("\n");
		sb.append("  Maximum: ");
		sb.append(this.lonMax);
		sb.append("\n");
		sb.append("Latitude:\n");
		sb.append("  Minimum: ");
		sb.append(this.latMin);
		sb.append("\n");
		sb.append("  Maximum: ");
		sb.append(this.latMax);
		sb.append("\n");
		
		try {
		    int recordCount = this.recordCount();
		    sb.append("Record Count: ");
		    sb.append(recordCount);
		    sb.append("\n");
		} catch(IOException ioe) {}
		
		return sb.toString();
	}
/**
 * a function to the offset of a given record in the file
 * @param indexOfRec
 * @return The offset in the file (in bytes) of the given record
 * @throws IOException
 * @throws RecordOutOfBoundsException
 */
	public int recordOffset(int indexOfRec) throws IOException, RecordOutOfBoundsException {
		
		if(indexOfRec >= recordCount() || 0 > indexOfRec)
			
			throw new RecordOutOfBoundsException();
		
		return getIntFrom( fileIndex(SHX_EXTENSION), B_END, SHX_HEADER_LENGTH + indexOfRec*SHX_RECORD_LENGTH ) * 2;
		
	}
	
/**
 * Gets the total number of records in the .shp file
 * 
 * @return integer number of records in the .shp file
 * @throws IOException
 * 
 */
	public int recordCount() throws IOException {
		
		return (int) (mapped_list[ fileIndex( SHX_EXTENSION ) ].capacity() - SHX_HEADER_LENGTH) / SHX_RECORD_LENGTH;
	}
	
/**
 * Determines the length of a given record 
 * 
 * @param recordIndex The index of a record
 * @return integer Length of record in bytes (not including 8 byte record header)
 * @throws IOException
 * 
 */
	
	public int recordLength(int recordIndex) throws IOException {
		
		return getIntFrom( fileIndex( SHX_EXTENSION ), B_END, SHX_HEADER_LENGTH + SHX_RECORD_LENGTH*recordIndex +  SHX_RECORD_LENGTH_OFFSET);
	}

	
/**
 * A function that interfaces with the underlying file models to return the .shp file length. 
 * Please note that there is no support from within this class for returing the length of the
 * associated dbf and shx files. One additional note is that this method dynamically determines the 
 * length of the file from the parent the file channels/mapped buffers. This ensures that for
 * write operations, this remains up to date
 * 
 * @return length of the .shp file in bytes
 * @throws IOException 
 * 
 */
	public long getFileLength( ) throws IOException {
		
		return super.getFileLength(0);
	}
	
/**
 * 
 * @return The path of the shapefile
 */
	public String getShpPath() {
		return this.shpPath;
	}
	
/**
 * 
 * @return The path of the shape index file
 */
	public String getShxPath() {
		return this.shxPath;
	}
	
/**
 * 
 * @return The path to the shape dbf file
 */
	public String getDbfPath() {
		return this.dbfPath;
	}
	
/**
 * 
 * @return the version code for the shape file
 */
	public int getVersion() {
		return this.version;
	}
	
/**
 * 
 * @return The esri dpecified index of the shape type
 */
	public Type getShapeType() {
		return this.shapeType;
	}
	
/**
 * 
 * @return The minimum bounding latitude
 */
	public double getLatMin() {
		return this.latMin;
	}
	
/**
 * 
 * @return The minimum bounding longitude
 */
	public double getLonMin() {
		return this.lonMin;
	}
	
/**
 * 
 * @return The maximum bounding latitude
 */
	public double getLatMax() {
		return this.latMax;
	}
	
/**
 * 
 * @return The maximum bounding longitude
 */
	public double getLonMax() {
		return this.lonMax;
	}
	
}
