package org.openstreetmap.gui.jmapviewer.tilesources;

//License: GPL.

import java.io.IOException;

/**
 * Adds the name and base URL to a tile source.
 */
public abstract class AbstractTMSTileSource extends AbstractTileSource 
{
    private static final int DEFAULT_TILE_SIZE = 256;
	private static final int DEFAULT_MAX_ZOOM = 21;
	protected String aName;
    protected String aBaseUrl;

    /**
     * @param pName The name of the tile source.
     * @param pBaseUrl The base URL
     */
    protected AbstractTMSTileSource(String pName, String pBaseUrl) 
    {
        aName = pName;
        aBaseUrl = pBaseUrl;
        if(aBaseUrl.endsWith("/"))
        {
            aBaseUrl = aBaseUrl.substring(0, aBaseUrl.length()-1);
        }
    }

    @Override
    public String getName() 
    {
        return aName;
    }

    @Override
    public int getMaxZoom()
    {
        return DEFAULT_MAX_ZOOM;
    }

    @Override
    public int getMinZoom()
    {
        return 0;
    }

    /**
     * @param pZoom The zoom factor.
     * @param pTileX The tile's x coordinate
     * @param pTileY The tile's y coordinate
     * @return The title path.
     * @throws IOException IOException when subclass cannot return the tile URL
     */
    protected String getTilePath(int pZoom, int pTileX, int pTileY) throws IOException 
    {
        return "/" + pZoom + "/" + pTileX + "/" + pTileY + "." + "png";
    }

    /**
     * @return The base url
     */
    protected String getBaseUrl() 
    {
        return aBaseUrl;
    }

    @Override
    public String getTileUrl(int pZoom, int pTileX, int pTileY) throws IOException 
    {
        return getBaseUrl() + getTilePath(pZoom, pTileX, pTileY);
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public String getTileType() 
    {
        return "png";
    }

    @Override
    public int getTileSize()
    {
        return DEFAULT_TILE_SIZE;
    }
}
