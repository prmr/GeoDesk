package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author Jan Peter Stotz
 *
 */
public class MapMarkerDot implements MapMarker 
{
	private static final Color COLOR = Color.RED;
	private static final int RADIUS = 6;
	
    private double aLatitude;
    private double aLongitude;
    private String aDescription = "";
    private String aName = "";

  
    /**
     * Creates a new dot marker.
     * @param pLatitude The latitude of the marker.
     * @param pLongitude The longitude of the marker.
     * @param pName The name of the marker.
     * @param pDescription The description of the marker.
     */
    public MapMarkerDot(double pLatitude, double pLongitude, String pName, String pDescription) 
    {
        aLatitude = pLatitude;
        aLongitude = pLongitude;
        aDescription = pDescription;
        aName = pName;
    }
    
    @Override
    public String getName()
    {
        return aName;
    }

    @Override
    public double getLatitude() 
    {
        return aLatitude;
    }

    @Override
    public double getLongitude() 
    {
        return aLongitude;
    }
    
    @Override
    public void setName(String pName)
    {
        aName = pName;
    }
    
    @Override
    public void setDescription(String pDescription)
    {
        aDescription = pDescription;
    }
    
    @Override
    public void paint(Graphics pGraphic, Point pPosition)
    {
        int height = RADIUS;
        int size = height * 2;
        pGraphic.setColor(COLOR);
        pGraphic.fillOval(pPosition.x - height, pPosition.y - height, size, size);
        pGraphic.setColor(Color.BLACK);
        pGraphic.drawOval(pPosition.x - height, pPosition.y - height, size, size);
    }

    @Override
    public String toString() 
    {
        return "MapMarker at " + aLatitude + " " + aLongitude;
    }
    
    @Override
    public int getRadius()
    {
        return RADIUS;
    }
    
    @Override
    public String getDescription()
    {
        return aDescription;
    }

}
