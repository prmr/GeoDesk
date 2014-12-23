package org.openstreetmap.gui.jmapviewer;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestOsmMercator
{
	@Test
	public void testGetDistance()
	{
		Coordinate p1 = new Coordinate(0,0);
		Coordinate p2 = new Coordinate(0,0);
		assertEquals(0,OsmMercator.getDistance(p1, p2),0);
		
		p2 = new Coordinate(10,10);
		assertEquals(1568520,OsmMercator.getDistance(p1, p2),10);
		
		p2 = new Coordinate(89,0);
		assertEquals(9896348,OsmMercator.getDistance(p1, p2),10);
		
		p2 = new Coordinate(90,0);
		assertEquals(10007543,OsmMercator.getDistance(p1, p2),10);
		
		p2 = new Coordinate(-90,0);
		assertEquals(10007543,OsmMercator.getDistance(p1, p2),10);
		
		p2 = new Coordinate(-45,0);
		assertEquals(5003771,OsmMercator.getDistance(p1, p2),10);
		
		p1 = new Coordinate(10, 15);
		p2 = new Coordinate(10, 105);
		assertEquals(9815405,OsmMercator.getDistance(p1, p2),10);
		
		p1 = new Coordinate(10, 15);
		p2 = new Coordinate(0, -105);
		assertEquals(13287649,OsmMercator.getDistance(p1, p2),10);
		
		p1 = new Coordinate(10, 180);
		p2 = new Coordinate(10, -180);
		assertEquals(0,OsmMercator.getDistance(p1, p2),0.00000001);
	}
	
	/** This test checks the computation of getDistance step by step
	 *  for an angle of 90 degrees */
	@Test
	public void testNinetyDegrees()
	{
		Coordinate p1 = new Coordinate(0,0);
		Coordinate p2 = new Coordinate(90,0);
		double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());
        assertEquals(0,lat1,0);
        assertEquals(0,lon1,0);
        assertEquals(Math.PI/2,lat2,0);
        assertEquals(0,lon2,0);
        
        double deltaLongitude = lon2 - lon1;
        double deltaLatitude = lat2 - lat1;
        
        assertEquals(0, deltaLongitude,0);
        assertEquals(Math.PI/2, deltaLatitude,0);
        
        double sinDLat = Math.sin(deltaLatitude/2);
        assertEquals(0.5, sinDLat*sinDLat,0.00000001);
        
        double coslat1 = Math.cos(lat1);
        assertEquals(1,coslat1,0);
        double coslat2 = Math.cos(lat2);
        assertEquals(0,coslat2,0.00000001);
        double sinDLon = Math.sin(deltaLongitude/2);
        assertEquals(0,sinDLon*sinDLon,0);
        double component = sinDLat*sinDLat + coslat1*coslat2*sinDLon*sinDLon;
        assertEquals(0.5, component, 0.00000001);
        
        double sqrt = Math.sqrt(component);
        assertEquals(Math.sqrt(0.5),sqrt,0.00000001);
        double min = Math.min(1, sqrt);
        assertEquals(sqrt,min,0);
        
        double twoasin = 2*Math.asin(min);
        assertEquals(Math.PI/2, twoasin, 0.00000001);
        
        double distance = twoasin*6371000;
        assertEquals(10007543, distance, 1 );
	}
}
