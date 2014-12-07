/*******************************************************************************
 * GeoDesk - Desktop application to view and edit geographic markers
 *
 *     Copyright (C) 2014 Martin P. Robillard, Jan Peter Stotz, and others
 *     
 *     See: http://martinrobillard.com/geodesk
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.openstreetmap.gui.jmapviewer;

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
