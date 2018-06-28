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
