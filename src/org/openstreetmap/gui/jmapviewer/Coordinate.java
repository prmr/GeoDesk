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

import java.awt.geom.Point2D;

/**
 * This class represents a latitude-longitude pair.
 *
 * @author Jan Peter Stotz
 * @author Stefan Zeller
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
