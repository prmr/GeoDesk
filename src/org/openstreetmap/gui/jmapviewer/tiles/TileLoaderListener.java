package org.openstreetmap.gui.jmapviewer.tiles;


//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Observers of the tile loading progress.
 */
public interface TileLoaderListener 
{
    /**
     * Will be called if a new {@link Tile} has been loaded successfully.
     * Loaded can mean downloaded or loaded from file cache.
     *
     * @param pTile The tile loading.
     * @param pSuccess whether the loading was successful.
     */
    void tileLoadingFinished(Tile pTile, boolean pSuccess);
}
