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
package org.openstreetmap.gui.jmapviewer.tiles;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * Implement this interface for creating your custom tile cache for
 * {@link JMapViewer}.
 *
 * @author Jan Peter Stotz
 */
public interface TileCache
{
    /**
     * Retrieves a tile from the cache if present, otherwise <code>null</code>
     * will be returned.
     *
     * @param pTileSource
     *            the tile source
     * @param pTileX
     *            tile number on the x axis of the tile to be retrieved
     * @param pTileY
     *            tile number on the y axis of the tile to be retrieved
     * @param pZoomLevel
     *            zoom level of the tile to be retrieved
     * @return the requested tile or <code>null</code> if the tile is not
     *         present in the cache
     */
    Tile getTile(TileSource pTileSource, int pTileX, int pTileY, int pZoomLevel);

    /**
     * Adds a tile to the cache. How long after adding a tile can be retrieved
     * via {@link #getTile(TileSource, int, int, int)} is unspecified and depends on the
     * implementation.
     *
     * @param pTile the tile to be added
     */
    void addTile(Tile pTile);
}
