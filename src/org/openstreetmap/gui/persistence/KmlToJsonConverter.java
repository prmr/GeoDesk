package org.openstreetmap.gui.persistence;

import org.openstreetmap.gui.jmapviewer.MapMarker;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * Converts GPS data in the KML format to the JSON format.
 */
public final class KmlToJsonConverter
{
	private KmlToJsonConverter() {}
	
	/**
	 * @param pArgs The first and only argument is
	 * the input file to convert. Currently this is
	 * just a utility to convert existing files: it
	 * does not have any error checking.
	 * @throws Exception everything
	 */
	public static void main(String[] pArgs) throws Exception
	{
		MarkerData[] data = KMLReader.extractData(pArgs[0]);
		MapMarker[] output = new MapMarker[data.length];
		for( int i = 0; i < data.length; i++ )
		{
			output[i] = new MapMarkerDot(data[i].aLatitude, data[i].aLongitude, data[i].aName, data[i].aDescription);
		}
		JSONPersistence.storeMarkers(output, pArgs[0] + ".json");
	}
}
