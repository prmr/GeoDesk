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

import org.openstreetmap.gui.jmapviewer.tilesources.MapnikOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * Controls the loading of tiles.
 */
public class TileController
{
    private TileLoader aTileLoader;
    private TileCache aTileCache;
    private TileSource aTileSource;

    /**
     * Creates a controller that loads Mapnik tiles by default.
     * @param pSource The tile source.
     * @param pTileCache The tile cache.
     * @param pListener The tile listener.
     */
    public TileController(TileSource pSource, TileCache pTileCache, TileLoaderListener pListener) 
    {
        aTileSource = new MapnikOsmTileSource();
        aTileLoader = new OsmTileLoader(pListener);
        this.aTileCache = pTileCache;
    }

    /**
     * Retrieves a tile from the cache. If the tile is not present in the cache
     * a load job is added to the working queue of {@link JobThread}.
     *
     * @param pTileX the X position of the tile
     * @param pTileY the Y position of the tile
     * @param pZoomLevel the zoom level of the tile
     * @return specified tile from the cache or <code>null</code> if the tile
     *         was not found in the cache.
     */
    public Tile getTile(int pTileX, int pTileY, int pZoomLevel) 
    {
        int max = 1 << pZoomLevel;
        if( pTileX < 0 || pTileX >= max || pTileY < 0 || pTileY >= max )
        {
            return null;
        }
        Tile tile = aTileCache.getTile(aTileSource, pTileX, pTileY, pZoomLevel);
        if (tile == null)
        {
            tile = new Tile(aTileSource, pTileX, pTileY, pZoomLevel);
            aTileCache.addTile(tile);
            tile.loadPlaceholderFromCache(aTileCache);
        }
        if(!tile.isLoaded()) 
        {
        	JobDispatcher.getInstance().addJob(aTileLoader.createTileLoaderJob(tile));
        }
        return tile;
    }

    /**
     * Sets the tile loader to a non-default value.
     * @param pTileLoader The new value.
     */
    public void setTileLoader(TileLoader pTileLoader)
    {
        aTileLoader = pTileLoader;
    }

    /**
     * @return The tile source managed by the controller.
     */
    public TileSource getTileSource()
    {
        return aTileSource;
    }

    /**
     * Sets the tile source to a non-default value.
     * @param pTileSource The new value.
     */
    public void setTileSource(TileSource pTileSource)
    {
        aTileSource = pTileSource;
    }
}
