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
/**
 * 
 */
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * Encodes default values for OSM map data.
 */
public abstract class AbstractOsmTileSource extends AbstractTileSource 
{
    private static final int DEFAULT_MAX_ZOOM = 18;

	/**
     * Stores the name and base url of the tile source.
     * @param pName The name of the tile source.
     * @param pBaseUrl The base url of the tile source.
     */
    protected AbstractOsmTileSource(String pName, String pBaseUrl) 
    {
        super(pName, pBaseUrl);
    }

    @Override
    public int getMaxZoom() 
    {
        return DEFAULT_MAX_ZOOM;
    }

    @Override
    public boolean requiresAttribution() 
    {
        return true;
    }

    @Override
    public String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight) 
    {
        return "\u00a9 OpenStreetMap contributors, CC-BY-SA ";
    }

    @Override
    public String getAttributionLinkURL()
    {
        return "http://openstreetmap.org/";
    }

    @Override
    public Image getAttributionImage() 
    {
        return null;
    }

    @Override
    public String getAttributionImageURL() 
    {
        return null;
    }

    @Override
    public String getTermsOfUseText() 
    {
        return null;
    }

    @Override
    public String getTermsOfUseURL() 
    {
        return "http://www.openstreetmap.org/copyright";
    }
}
