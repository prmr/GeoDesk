/*******************************************************************************
 * GeoDesk - Desktop application to view and edit geographic markers
 *
 *     Copyright (C) 2014 Martin P. Robillard, Jan Peter Stotz, and others
 *     
 *     See: http://martinrobillard.com/geodesk
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.openstreetmap.gui.persistence;

import org.openstreetmap.gui.jmapviewer.MapMarker;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * Converts GPS data in the KML format to the JSON format.
 * 
 * @author Martin P. Robillard
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
