package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

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
