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
}
