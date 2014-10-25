package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2009 by Stefan Zeller

import java.awt.geom.Point2D;

/**
 * This class represents a latitude-longitude pair.
 *
 * @author Jan Peter Stotz
 * @author Martin P. Robillard
 */
public class Coordinate
{
	private Point2D.Double aData;

    /**
     * Creates a new geographic coordinate.
     * @param pLatitude The latitude coordinate
     * @param pLongitude The longitude coordinate
     */
    public Coordinate(double pLatitude, double pLongitude) 
    {
        aData = new Point2D.Double(pLongitude, pLatitude);
    }

    /**
     * @return The latitude component of this coordinate
     */
    public double getLatitude() 
    {
        return aData.y;
    }

    /**
     * @return The longitude component of this coordinate
     */
    public double getLongitude() 
    {
        return aData.x;
    }

    @Override
    public String toString() 
    {
        return "Coordinate[" + aData.y + ", " + aData.x + "]";
    }
}
