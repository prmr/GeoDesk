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

/**
 * Cycle map tile source.
 */
public class CycleOsmTileSource extends AbstractOsmTileSource
{
	private static final int MAX_ZOOM = 17;

	private static final String PATTERN = "http://%s.tile.opencyclemap.org/cycle";

    private static final String[] SERVER = { "a", "b", "c" };

    private int aServerNumber = 0;

    /**
     * Create a cycle map tile source.
     */
    public CycleOsmTileSource()
    {
        super("OSM Cycle Map", PATTERN);
    }

    @Override
    public String getBaseUrl()
    {
        String url = String.format(this.aBaseUrl, new Object[] { SERVER[aServerNumber] });
        aServerNumber = (aServerNumber + 1) % SERVER.length;
        return url;
    }

    @Override
    public int getMaxZoom()
    {
        return MAX_ZOOM;
    }

    @Override
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.LastModified;
    }
}
