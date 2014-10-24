package org.openstreetmap.gui.jmapviewer.tiles;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

//License: GPL. Copyright 2008 by Jan Peter Stotz

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
