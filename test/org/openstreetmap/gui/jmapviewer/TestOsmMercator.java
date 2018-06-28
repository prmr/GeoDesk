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
	
	@Test
	public void testXtoLongitude()
	{
		assertEquals(-180, OsmMercator.xToLongitude(0, 0),0);
		assertEquals(-180, OsmMercator.xToLongitude(0, 1),0);
		assertEquals(-180, OsmMercator.xToLongitude(0, 2),0);
		assertEquals(0, OsmMercator.xToLongitude(128, 0),0);
		assertEquals(0, OsmMercator.xToLongitude(256, 1),0);
	}
	
	@Test
	public void testYtoLatitude()
	{
		final int tileSize = 256;
		// Almost north pole: complete top of the map.
		assertEquals(85.05112877980659, OsmMercator.yToLatitude(0, 0),0.000001);
		assertEquals(85.05112877980659, OsmMercator.yToLatitude(0, 1),0.000001);
		assertEquals(85.05112877980659, OsmMercator.yToLatitude(0, 2),0.000001);
		assertEquals(85.05112877980659, OsmMercator.yToLatitude(0, 3),0.000001);
		assertEquals(85.05112877980659, OsmMercator.yToLatitude(0, 4),0.000001);
		
		// Almost south pole: complete bottom of the map
		assertEquals(-84.9283, OsmMercator.yToLatitude(tileSize-1, 0),0.001);
		assertEquals(-84.9901, OsmMercator.yToLatitude((tileSize * (1 << 1)-1), 1),0.001);
		assertEquals(-85.0207, OsmMercator.yToLatitude((tileSize * (1 << 2))-1, 2),0.001);
		assertEquals(-85.0359, OsmMercator.yToLatitude((tileSize * (1 << 3)-1), 3),0.001);
		
		// Equator: middle of the map
		assertEquals(0.0, OsmMercator.yToLatitude(tileSize/2, 0),0.000001);
		assertEquals(0.0, OsmMercator.yToLatitude((tileSize* (1 << 1))/2, 1),0.000001);
		assertEquals(0.0, OsmMercator.yToLatitude((tileSize* (1 << 2))/2, 2),0.000001);
		assertEquals(0.0, OsmMercator.yToLatitude((tileSize* (1 << 3))/2, 3),0.000001);
	}
	
	@Test
	public void testLongitudeToX()
	{
		// Test the left side of the map
		assertEquals(0, OsmMercator.longitudeToX(-180, 0));
		assertEquals(0, OsmMercator.longitudeToX(-180, 1));
		assertEquals(0, OsmMercator.longitudeToX(-180, 2));
		
		// Test the right side of the map
		assertEquals(255, OsmMercator.longitudeToX(180, 0));
		assertEquals(256*2-1, OsmMercator.longitudeToX(180, 1));
		assertEquals(256*4-1, OsmMercator.longitudeToX(180, 2));
		
		// Test the middle of the map
		assertEquals(256/2, OsmMercator.longitudeToX(0, 0));
		assertEquals(256*2/2, OsmMercator.longitudeToX(0, 1));
		assertEquals(256*4/2, OsmMercator.longitudeToX(0, 2));
		
		// Test all points using the converse function. 
		// Assumes there are no errors in xToLongitude
		for( int i = 0; i < 256; i++ )
		{
			assertEquals(i, OsmMercator.longitudeToX(OsmMercator.xToLongitude(i, 0),0));
		}
		for( int i = 0; i < 512; i++ )
		{
			assertEquals(i, OsmMercator.longitudeToX(OsmMercator.xToLongitude(i, 1),1));
		}
		for( int i = 0; i < 1024; i++ )
		{
			assertEquals(i, OsmMercator.longitudeToX(OsmMercator.xToLongitude(i, 2),2));
		}
	}
	
	@Test
	public void testLatitudeToY()
	{
		final int tileSize = 256;
		// Almost north pole: complete top of the map.
		assertEquals(0, OsmMercator.latitudeToY(85.0511, 0));
		assertEquals(0, OsmMercator.latitudeToY(85.0511, 1));
		assertEquals(0, OsmMercator.latitudeToY(85.0511, 2));
		assertEquals(0, OsmMercator.latitudeToY(85.0511, 3));

		// Almost south pole: complete bottom of the map
		// Somehow there's a bit of drift on the south pole
		assertEquals(tileSize-1, OsmMercator.latitudeToY(-85, 0));
		assertEquals((tileSize * (1 << 1)-1), OsmMercator.latitudeToY(-85, 1));
		assertEquals((tileSize * (1 << 2)-2), OsmMercator.latitudeToY(-85, 2));
		assertEquals((tileSize * (1 << 3)-4), OsmMercator.latitudeToY(-85, 3));
		assertEquals((tileSize * (1 << 4)-7), OsmMercator.latitudeToY(-85, 4));
		
		// Equator: middle of the map
		assertEquals(tileSize/2, OsmMercator.latitudeToY(0.0, 0));
		assertEquals((tileSize* (1 << 1))/2, OsmMercator.latitudeToY(0.0, 1));
		assertEquals((tileSize* (1 << 2))/2, OsmMercator.latitudeToY(0.0, 2));
		assertEquals((tileSize* (1 << 3))/2, OsmMercator.latitudeToY(0.0, 3));
		assertEquals((tileSize* (1 << 4))/2, OsmMercator.latitudeToY(0.0, 4));

		
		// The latitudeToY function is not an accurate converse of the yToLatitude, so 
		// we can't write a test as for the longitude.
	}
}
