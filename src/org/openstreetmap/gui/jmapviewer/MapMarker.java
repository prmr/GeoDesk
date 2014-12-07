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

import java.awt.Graphics;
import java.awt.Point;

/**
 * Interface to be implemented by all one dimensional elements that can be displayed on the map.
 *
 * @author Jan Peter Stotz
 * @author Martin P. Robillard Radius and Descriptions
 * 
 * @see JMapViewer#addMapMarker(MapMarker)
 * @see JMapViewer#getMapMarkerList()
 */
public interface MapMarker 
{
    /**
     * @return Latitude of the map marker position
     */
    double getLatitude();

    /**
     * @return Longitude of the map marker position
     */
    double getLongitude();
    
    /**
     * @return The number of pixels away from the point the marker
     * occupies.
     */
    int getRadius();
    
    /**
     * @return The description of the marker.
     */
    String getDescription();
    
    /**
     * @return The name of the marker.
     */
    String getName();
    
    /**
     * Sets the name of the marker.
     * @param pName The name to set.
     */
    void setName(String pName);
    
    /**
     * Sets a description for this marker.
     * @param pDescription The description of the marker.
     */
    void setDescription(String pDescription);

    /**
     * Paints the map marker on the map. The <code>position</code> specifies the
     * coordinates within <code>g</code>
     *
     * @param pGraphic The graphic context 
     * @param pPosition The position to paint
     */
    void paint(Graphics pGraphic, Point pPosition);
}
