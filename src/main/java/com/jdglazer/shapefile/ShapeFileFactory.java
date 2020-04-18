package com.jdglazer.shapefile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jdglazer.shapefile.utils.InvalidFileException;
import com.jdglazer.shapefile.utils.InvalidFileTypeException;

public class ShapeFileFactory {

	public static ShapeFile getShapeFile(File shp, File shx, File dbf) throws InvalidFileException, FileNotFoundException, InvalidFileTypeException, IOException {
		
		ShapeFile shapeFile = null;
		try {
		    shapeFile = new ShapeFile(shp.getAbsolutePath(),shx.getAbsolutePath(),dbf.getAbsolutePath());
		} catch(RecordOutOfBoundsException roobe) {
			throw new InvalidFileException("Invalid shape file format");
		}
		
		return getDerivedShapeFile(shapeFile);
	}
	
	public static ShapeFile getShapeFile(File shp) throws InvalidFileException, FileNotFoundException, InvalidFileTypeException, IOException {
		
		ShapeFile shapeFile = null;
		try {
		    shapeFile = new ShapeFile(shp.getAbsolutePath());
		} catch(RecordOutOfBoundsException roobe) {
			throw new InvalidFileException("Invalid shape file format");
		}
        
		return getDerivedShapeFile(shapeFile);
	}
	
	private static ShapeFile getDerivedShapeFile(ShapeFile shapeFile) throws FileNotFoundException, InvalidFileTypeException, IOException {
		switch(shapeFile.getShapeType()) {
		    case POLYGON:
			    shapeFile = new PolygonShapeFile(shapeFile);
			    break;
		    case MULTIPOINT:
		    	shapeFile = new MultipointShapeFile(shapeFile);
		    	break;
		    case POINT:
		    	shapeFile = new PointShapeFile(shapeFile);
		    	break;
		    case POLYLINE:
		    	shapeFile = new PolylineShapeFile(shapeFile);
		    	break;
		    default:
		    	break;
		}
		
		return shapeFile;
    }
	
}
