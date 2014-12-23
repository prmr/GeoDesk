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


/**
 * This class represents a latitude-longitude pair, in decimal degrees.
 * Latitudes are in the range [-90,90]; Longitudes are in the range [-180,180]
 *
 * @author Jan Peter Stotz
 * @author Stefan Zeller
 * @author Martin P. Robillard
 */
public class Coordinate
{
	private double aLatitude;
	private double aLongitude;

    /**
     * Creates a new geographic coordinate.
     * @param pLatitude The latitude coordinate
     * @param pLongitude The longitude coordinate
     * @pre pLatitude >= -90 && pLatitude <= 90
     * @pre pLongitude >= -180 && pLongitude <= 180
     */
    public Coordinate(double pLatitude, double pLongitude) 
    {   // CSOFF:
    	assert pLatitude >= -90 && pLatitude <= 90;
    	assert pLongitude >= -180 && pLongitude <= 180; // CSON:
        aLatitude = pLatitude;
        aLongitude = pLongitude;
    }

    /**
     * @return The latitude component of this coordinate
     */
    public double getLatitude() 
    {
        return aLatitude;
    }

    /**
     * @return The longitude component of this coordinate
     */
    public double getLongitude() 
    {
        return aLongitude;
    }

    @Override
    public String toString() 
    {
        return "Coordinate (lat,lon) [" + aLatitude + ", " + aLongitude + "]";
    }
}
