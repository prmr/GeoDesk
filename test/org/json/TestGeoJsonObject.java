/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.json;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestGeoJsonObject
{
	private final String aCapeTown = "{ \"type\": \"Point\"," + 
									 "  \"coordinates\": [18.427633,-33.916210]," + 
									 "  \"properties\": { \"name\": \"Cape Town, South Africa\"," +
									 "                    \"description\": \"This is not the capital of South Africa\"}}";
	
	private final String aMontreal = "{ \"type\": \"Point\"," + 
			 "  \"coordinates\": [-73.596039,45.489502]," + 
			 "  \"properties\": { \"name\": \"Montréal, QC, Canada\"," +
			 "                    \"description\": \"Home of McGill University\"}}";
	
	private final String aCollection = "{ \"type\": \"GeometryCollection\"," +
			" \"geometries\": [" + 
			aCapeTown + "," + aMontreal + "]," + 
			"  \"properties\": { \"geodesk-version\": \"0.3.0\" }}";
	
	@Test
	public void testParse()
	{
		JSONObject object = new JSONObject(aCapeTown);
		Set<String> keySet = object.keySet();
		assertEquals( 3, keySet.size());
		assertTrue( keySet.contains("type"));
		assertTrue( keySet.contains("coordinates"));
		assertTrue( keySet.contains("properties"));
		assertEquals("Point", object.getString("type"));
		
		Object coordinates = object.get("coordinates");
		assertEquals(JSONArray.class, coordinates.getClass());
		assertEquals(2, ((JSONArray)coordinates).length());
		Object value1 = ((JSONArray)coordinates).get(0);
		assertEquals(java.lang.Double.class, value1.getClass());
		assertEquals(18.427633, (Double)value1, 0.000000001);
		Object value2 = ((JSONArray)coordinates).get(1);
		assertEquals(java.lang.Double.class, value2.getClass());
		assertEquals(-33.916210, (Double)value2, 0.000000001);
		
		Object properties = object.get("properties");
		assertEquals(JSONObject.class, properties.getClass());
		Set<String> keySet2 = ((JSONObject)properties).keySet();
		assertEquals(2, keySet2.size());
		assertTrue(keySet2.contains("name"));
		assertTrue(keySet2.contains("description"));
		
		assertEquals("Cape Town, South Africa", ((JSONObject)properties).getString("name"));
		assertEquals("This is not the capital of South Africa", 
				((JSONObject)properties).getString("description"));
	}
	
	@Test
	public void testAccents()
	{
		JSONObject object = new JSONObject(aMontreal);
		assertEquals("Montréal, QC, Canada", object.getJSONObject("properties").getString("name"));
	}
	
	@Test
	public void testCollection()
	{
		JSONObject object = new JSONObject(aCollection);
		Set<String> keySet = object.keySet();
		assertEquals( 3, keySet.size());
		assertEquals("GeometryCollection", object.getString("type"));
		assertEquals("0.3.0", object.getJSONObject("properties").getString("geodesk-version"));
		JSONArray locations = object.getJSONArray("geometries");
		assertEquals(2, locations.length());
	}
}
