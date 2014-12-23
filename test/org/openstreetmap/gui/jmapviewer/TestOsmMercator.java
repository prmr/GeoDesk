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
		assertEquals(0,OsmMercator.getDistance2(p1, p2),0);
		
		p2 = new Coordinate(10,10);
		assertEquals(1568520,OsmMercator.getDistance2(p1, p2),10);
		
		p2 = new Coordinate(89,0);
		assertEquals(9896348,OsmMercator.getDistance2(p1, p2),10);
		
		// TODO
//		p2 = new Coordinate(90,0);
//		assertEquals(9896348,OsmMercator.getDistance2(p1, p2),10);
	}
}
