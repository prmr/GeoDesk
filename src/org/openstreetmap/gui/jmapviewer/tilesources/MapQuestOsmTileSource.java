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
package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * The MapQuest tile source.
 */
public class MapQuestOsmTileSource extends AbstractMapQuestTileSource 
{
    private static final String PATTERN = "http://otile%d.mqcdn.com/tiles/1.0.0/osm";

    /**
     * Constructs a new MapQuest tile source.
     */
    public MapQuestOsmTileSource()
    {
        super("MapQuest-OSM", PATTERN);
    }
    
    @Override
    public String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight)
    {
        return super.getAttributionText(pZoom, pTopLeft, pBottomRight)+"- "+MAPQUEST_ATTRIBUTION;
    }
}
