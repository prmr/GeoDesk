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

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

/**
 * Default values for MapQuest tile sources.
 */
public class AbstractMapQuestTileSource extends AbstractOsmTileSource 
{
	protected static final String MAPQUEST_ATTRIBUTION = "Tiles Courtesy of MapQuest ";
    protected static final String MAPQUEST_WEBSITE = "http://www.mapquest.com";
	
    // MapQuest logo in base64: http://developer.mapquest.com/content/osm/mq_logo.png
    private static final String LOGO_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJl"+
            "YWR5ccllPAAAAZtJREFUeNpi/P//P0PPcYf/DGSAEssDjIzdx+zJ0gwDLMQqVBWyZVAStGRgBMK33x8wnH62"+
            "kngD+DmkGBwUshn42SXA/P8M/xg+/3rDcOPNPuIMMJeKAmv+8OMpw7ffHxikeLUZXBTzgez3DEzEGMDGzAmm"+
            "jz5ewLDqWiHE6UwcDHxsYhAXsLPwMFhKxzIIccozPP18ieHhx3MMGsKOYP7td4fBzgUBN+ViBkeFLDD7zbf7"+
            "DK++3WFgAMXC448X/uMDV17t+H/r7UEM8VNPl/8Hu0CGTx9s6tXXOxhEuJQYxLnVgK44w/Dzz1cGNWF7BlGg"+
            "2KJLqQzCQBcxMbEw/P77g0FTxBkYJs8gXgCFKiMwOLbf6WDQF/djcFUqAvv33fdHYAM4WPjAFrz9/hAeLsef"+
            "LALT4EBkhIYlMxMrAxerIJjNCdTExy4OZv/59xNnAKPEAh+bBNAQSMwKcsgAQ5odzBbilGNghcYE1pS4+14f"+
            "MKq4GP79/w1OHCC/v/x6Exzv+x9MhbiOEeh3LAZQnBeYGCgEjJRmZ4AAAwCE6rplT3Ba/gAAAABJRU5ErkJg"+
            "gg==";

    private static final int NUMBER_OF_SERVERS = 4;
    
    private int aServerNumber = 1;

    /**
     * Constructs the abstract class.
     * @param pName The name of the tile source.
     * @param pBaseUrl The base URL for the tile source.
     */
    protected AbstractMapQuestTileSource(String pName, String pBaseUrl)
    {
        super(pName, pBaseUrl);
    }

    @Override
    public String getBaseUrl()
    {
        String url = String.format(this.aBaseUrl, aServerNumber);
        aServerNumber = (aServerNumber % NUMBER_OF_SERVERS) + 1;
        return url;
    }

    @Override
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.IfModifiedSince;
    }

    @Override
    public Image getAttributionImage() 
    {
        try 
        {
            return ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(LOGO_BASE64)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAttributionImageURL() 
    {
        return MAPQUEST_WEBSITE;
    }


    @Override
    public String getTermsOfUseURL() 
    {
        return "http://developer.mapquest.com/web/products/open/map#terms";
    }
}
