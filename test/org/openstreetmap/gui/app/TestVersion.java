package org.openstreetmap.gui.app;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestVersion
{
	@Test
	public void testVersion()
	{
		assertEquals( "0.1", new Version(0,1,0).toString());
		assertEquals( "0.1.1", new Version(0,1,1).toString());
		assertEquals( "1.1.1", new Version(1,1,1).toString());
	}
}
