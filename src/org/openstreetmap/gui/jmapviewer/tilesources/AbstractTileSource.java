package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;

import org.openstreetmap.gui.jmapviewer.Coordinate;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Stores the basic tile source meta-data.
 * @author Jan Peter Stotz
 * @author Martin P. Robillard
 */
public abstract class AbstractTileSource implements TileSource 
{
    protected String aAttributionText;
    protected String aAttributionLinkURL;
    protected Image aAttributionImage;
    protected String aAttributionImageURL;
    protected String aTermsOfUseText;
    protected String aTermsOfUseURL;

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
}
