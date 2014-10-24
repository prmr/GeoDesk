package org.openstreetmap.gui.jmapviewer.tiles;

//License: GPL. Copyright 2008 by Jan Peter Stotz


/**
 * Interface for implementing a tile loader. Tiles are usually loaded via HTTP
 * or from a file.
 *
 * @author Jan Peter Stotz
 */
public interface TileLoader 
{
    /**
     * A typical implementation of this function should create and return a
     * new {@link TileJob} instance that performs the load action.
     *
     * @param pTile the tile to be loaded
     * @return {@link TileJob} implementation that performs the desired load
     *          action.
     */
    TileJob createTileLoaderJob(Tile pTile);
}
