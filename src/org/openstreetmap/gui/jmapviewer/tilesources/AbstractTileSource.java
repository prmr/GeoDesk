package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;
import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.Coordinate;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Stores the basic tile source meta-data.
 * @author Jan Peter Stotz
 * @author Martin P. Robillard
 */
public abstract class AbstractTileSource implements TileSource 
{
    private static final int DEFAULT_MAX_ZOOM = 21;
	private static final int DEFAULT_TILE_SIZE = 256;
	protected String aAttributionText;
    protected String aAttributionLinkURL;
    protected Image aAttributionImage;
    protected String aAttributionImageURL;
    protected String aTermsOfUseText;
    protected String aTermsOfUseURL;
	protected String aName;
	protected String aBaseUrl;

	/**
	 * Assigns the attribution data, name, and base url.
	 * @param pName The name of the tile source.
	 * @param pBaseUrl The base url.
	 */
	protected AbstractTileSource(String pName, String pBaseUrl) 
    {
        aName = pName;
        aBaseUrl = pBaseUrl;
        if(aBaseUrl.endsWith("/"))
        {
            aBaseUrl = aBaseUrl.substring(0, aBaseUrl.length()-1);
        }
    }
	
    @Override
    public boolean requiresAttribution() 
    {
        return aAttributionText != null || aAttributionImage != null || aTermsOfUseText != null || aTermsOfUseURL != null;
    }

    @Override
    public String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight)
    {
        return aAttributionText;
    }

    @Override
    public String getAttributionLinkURL() 
    {
        return aAttributionLinkURL;
    }

    @Override
    public Image getAttributionImage()
    {
        return aAttributionImage;
    }

    @Override
    public String getAttributionImageURL() 
    {
        return aAttributionImageURL;
    }

    @Override
    public String getTermsOfUseText() 
    {
        return aTermsOfUseText;
    }

    @Override
    public String getTermsOfUseURL()
    {
        return aTermsOfUseURL;
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
	 * @return The base url
	 */
	protected String getBaseUrl()
	{
	    return aBaseUrl;
	}

	@Override
	public String getTileUrl(int pZoom, int pTileX, int pTileY) throws IOException
	{
	    return getBaseUrl() + "/" + pZoom + "/" + pTileX + "/" + pTileY + "." + "png";
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
