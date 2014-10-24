package org.openstreetmap.gui.jmapviewer.tiles;

//License: GPL. Copyright 2012 by Dirk Stöcker


/**
 * Interface for implementing a tile loading job. Tiles are usually loaded via HTTP
 * or from a file.
 *
 * @author Dirk Stöcker
 */
public interface TileJob extends Runnable {

    /**
     * Function to return the tile associated with the job
     *
     * @return {@link Tile} to be handled
     */
    public Tile getTile();
}
