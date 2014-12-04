package org.openstreetmap.gui.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Single point of access for reading and writing marker
 * data stored in a JSON file. The file follows the 
 * GeoJSON specification and stores a single GeometryCollection.
 * 
 * @see http://geojson.org
 */
public final class JSONPersistence
{
	private JSONPersistence() {}
	
	/**
     * Loads the marker data from file.
     * @param pInput The name of the source file.
     * @return An array of MarkerData loaded from file.
     * @throws PersistenceException If there's any problem with the load. 
     */
    public static MarkerData[] loadMarkers(String pInput)
    {
    	try
    	{
    		List<String> lines = Files.readAllLines(Paths.get(pInput), StandardCharsets.ISO_8859_1);
    		StringBuffer buffer = new StringBuffer();
    		for(String string : lines)
    		{
    			buffer.append(string.trim());
    		}
    		JSONObject geometryCollection = new JSONObject(buffer.toString());
    		JSONArray points = geometryCollection.getJSONArray("geometries");
    		MarkerData[] markers = new MarkerData[points.length()];
    		for( int i = 0; i < markers.length; i++ )
    		{
    			markers[i] = extractMarker(points.getJSONObject(i));
    		}
    		return markers;
    	}
    	catch( IOException exception )
    	{
    		throw new PersistenceException(exception);
    	}
    }
    
    private static MarkerData extractMarker(JSONObject pPoint)
    {
    	MarkerData lReturn = new MarkerData();
    	JSONArray coordinates = pPoint.getJSONArray("coordinates");
    	lReturn.aLongitude = coordinates.getDouble(0);
    	lReturn.aLatitude = coordinates.getDouble(1);
    	lReturn.aName = pPoint.getJSONObject("properties").getString("name");
    	lReturn.aDescription = pPoint.getJSONObject("properties").getString("description");
    	return lReturn;
    }
    
    /**
     * Make a backup of pFileName by copying it to the same directory,
     * overriding any file of the same name. The backup has the same 
     * name as the original file, with the extension .backup.
     * @param pFileName The name of the file to back up.
     * @throws PersistenceException If the backup is not successful.
     */
    public static void backup(String pFileName)
    {
    	try
		{
			Files.copy(Paths.get(pFileName), Paths.get(pFileName + ".backup"), 
					StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
		}
		catch( IOException exception )
		{
			throw new PersistenceException(exception);
		}
    }
    
    public static void main(String[] args)
	{
		JSONPersistence.loadMarkers("data\\TestMarkers.json");
    	JSONPersistence.backup("data\\TestMarkers.json");
	}
}
