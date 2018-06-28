/*******************************************************************************
 * GeoDesk - Desktop application to view and edit geographic markers
 * 
 * Copyright (C) 2014, 2015 Martin P. Robillard, Jan Peter Stotz, and others
 * 
 * See: http://martinrobillard.com/geodesk
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.openstreetmap.gui.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.app.Version;
import org.openstreetmap.gui.jmapviewer.MapMarker;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * Single point of access for reading and writing marker data stored in a JSON file. The file follows the GeoJSON
 * specification and stores a single GeometryCollection.
 * 
 * @see http://geojson.org
 * 
 * @author Martin P. Robillard
 */
public final class JSONPersistence
{
	private JSONPersistence()
	{
	}

	/**
	 * Loads the marker data from file.
	 * 
	 * @param pFineName
	 *            The name of the source file.
	 * @return An array of MarkerData loaded from file.
	 * @throws PersistenceException
	 *             If there's any problem with the load.
	 */
	public static MarkerData[] loadMarkers(String pFineName)
	{
		try
		{
			List<String> lines = Files.readAllLines(Paths.get(pFineName), StandardCharsets.ISO_8859_1);
			StringBuffer buffer = new StringBuffer();
			for (String string : lines)
			{
				buffer.append(string.trim());
			}

			List<MarkerData> markers = new Vector<>();
			JSONObject geometryCollection = new JSONObject(buffer.toString());
			if (geometryCollection.has("geometries"))
			{
				JSONArray points = geometryCollection.getJSONArray("geometries");
				for (int i = 0; i < points.length(); i++)
				{
					markers.add(extractMarker(points.getJSONObject(i)));
				}
			}

			if (geometryCollection.has("features"))
			{
				JSONArray features = geometryCollection.getJSONArray("features");
				for (int i = 0; i < features.length(); i++)
				{
					// currently disabled
					markers.add(extractFeature(features.getJSONObject(i)));
				}
			}

			return markers.toArray(new MarkerData[0]);
		}
		catch (IOException exception)
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

	private static MarkerData extractFeature(JSONObject pFeature)
	{
		MarkerData lReturn = new MarkerData();

		// if the feature is a waypoint
		if (pFeature.has("properties") && pFeature.getJSONObject("properties").has("gpxtype") && 
				pFeature.getJSONObject("properties").getString("gpxtype").equals("wptType") && 
				pFeature.has("geometry"))
		{

			try
			{
				JSONArray coordinates = pFeature.getJSONObject("geometry").getJSONArray("coordinates");
				lReturn.aLongitude = coordinates.getDouble(0);
				lReturn.aLatitude = coordinates.getDouble(1);
			}
			catch(Exception e)
			{
				System.out.println("JSONPersistence::extractFeature( " + pFeature + " ) - ERROR: " + e.getMessage());
			}
		}
		if (pFeature.has("properties"))
		{
			lReturn.aName = pFeature.getJSONObject("properties").getString("name");
		}
		return lReturn;
	}

	/**
	 * Stores the markers to disk.
	 * 
	 * @param pMarkers
	 *            The marker data to persist.
	 * @param pFileName
	 *            The destination file.
	 * @throws PersistenceException
	 *             is there's any issue saving the markers.
	 */
	public static void storeMarkers(MapMarker[] pMarkers, String pFileName)
	{
		JSONObject geographyCollection = new JSONObject();
		geographyCollection.put("type", "GeometryCollection");
		JSONObject versionInfo = new JSONObject();
		versionInfo.put("geodesk-version", Version.instance().toString());
		geographyCollection.put("properties", versionInfo);
		JSONArray geometries = new JSONArray();
		for (MapMarker marker : pMarkers)
		{
			geometries.put(createPointObject(marker));
		}
		geographyCollection.put("geometries", geometries);
		List<String> out = new ArrayList<String>();
		out.add(geographyCollection.toString(3));
		try
		{
			Files.write(Paths.get(pFileName), out, StandardCharsets.ISO_8859_1);
		}
		catch (IOException exception)
		{
			throw new PersistenceException(exception);
		}
	}

	private static JSONObject createPointObject(MapMarker pMarker)
	{
		JSONObject lReturn = new JSONObject();
		lReturn.put("type", "Point");
		JSONArray coordinates = new JSONArray();
		coordinates.put(pMarker.getLongitude());
		coordinates.put(pMarker.getLatitude());
		lReturn.put("coordinates", coordinates);
		JSONObject properties = new JSONObject();
		properties.put("name", pMarker.getName());
		properties.put("description", pMarker.getDescription());
		lReturn.put("properties", properties);
		return lReturn;
	}

	/**
	 * Make a backup of pFileName by copying it to the same directory, overriding any file of the same name. The backup
	 * has the same name as the original file, with the extension .backup.
	 * 
	 * @param pFileName
	 *            The name of the file to back up.
	 * @throws PersistenceException
	 *             If the backup is not successful.
	 */
	public static void backup(String pFileName)
	{
		try
		{
			Files.copy(Paths.get(pFileName), Paths.get(pFileName + ".backup"), StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException exception)
		{
			throw new PersistenceException(exception);
		}
	}

	public static void main(String[] args)
	{
		MarkerData[] data = JSONPersistence.loadMarkers("data\\TestMarkers.json");
		MapMarker[] converted = new MapMarker[data.length];
		for (int i = 0; i < data.length; i++)
		{
			converted[i] = new MapMarkerDot(data[i].getLatitude(), data[i].getLongitude(), data[i].getName(),
					data[i].getDescription());
		}
		JSONPersistence.storeMarkers(converted, "data\\TestMarkers-out.json");

	}
}
