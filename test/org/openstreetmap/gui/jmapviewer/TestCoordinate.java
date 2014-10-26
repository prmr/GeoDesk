package org.openstreetmap.gui.jmapviewer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCoordinate
{
	private Coordinate aOrigin;
	
	@Before
	public void setUp()
	{
		aOrigin = new Coordinate(0,0);
	}
	
	@Test
	public void testBasicInitialization()
	{
		assertEquals(0, aOrigin.getLatitude(), 0);
		assertEquals(0, aOrigin.getLongitude(), 0);
	}
}
