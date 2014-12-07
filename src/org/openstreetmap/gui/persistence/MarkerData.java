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
package org.openstreetmap.gui.persistence;

/**
 * All the data associated with a marker.
 * 
 * @author Martin P. Robillard
 */
public class MarkerData
{
	String aName;
	double aLatitude;
	double aLongitude;
	String aDescription;
	
    /**
	 * @return the name of the location.
	 */
	public String getName()
	{
		return aName;
	}

	/**
	 * @return the latitude of the location.
	 */
	public double getLatitude()
	{
		return aLatitude;
	}

	/**
	 * @return the longitude of the location.
	 */
	public double getLongitude()
	{
		return aLongitude;
	}

	/**
	 * @return the description of the location.
	 */
	public String getDescription()
	{
		return aDescription;
	}
    
    @Override
    public String toString()
    {
        return aName + " (" + aLatitude + "," + aLongitude + "); " + aDescription;
    }
}