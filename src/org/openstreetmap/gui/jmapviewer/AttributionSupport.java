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
package org.openstreetmap.gui.jmapviewer;

import static org.openstreetmap.gui.jmapviewer.FeatureAdapter.translate;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.HashMap;

import org.openstreetmap.gui.jmapviewer.tilesources.Attributed;

/**
 * Services for handling attribution of the map data.
 */
public class AttributionSupport 
{
	private static final Font ATTR_FONT = new Font("Arial", Font.PLAIN, 10);
	
    private Attributed aSource;
    private Image aAttributionImage;
    private String aAttributionTermsText;
    private String aAttributionTermsUrl;
    
    // CSOFF:
    private static final Font ATTR_LINK_FONT;
    // CSON:
    
    private Rectangle aAttributionTextBounds = null;
    private Rectangle aAttributionTermsBounds = null;
    private Rectangle aAttributionImageBounds = null;

    static {
        HashMap<TextAttribute, Integer> aUnderline = new HashMap<TextAttribute, Integer>();
        aUnderline.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        ATTR_LINK_FONT = ATTR_FONT.deriveFont(aUnderline);
    }

    /**
     * Initialize the support for attributing the source pTileSource.
     * @param pTileSource The map data source to provide attribution for.
     */
    public void initialize(Attributed pTileSource) 
    {
        aSource = pTileSource;
        if(pTileSource.requiresAttribution()) 
        {
            aAttributionImage = pTileSource.getAttributionImage();
            aAttributionTermsText = pTileSource.getTermsOfUseText();
            aAttributionTermsUrl = pTileSource.getTermsOfUseURL();
            if(aAttributionTermsUrl != null && aAttributionTermsText == null) 
            {
                aAttributionTermsText = translate("Background Terms of Use");
            }
        } 
        else
        {
            aAttributionImage = null;
            aAttributionTermsUrl = null;
        }
    }

    /**
     * Paint the attribution decorations.
     * @param pGraphics The graphics context.
     * @param pWidth The width of the paint area
     * @param pHeight The height of the paint area
     * @param pTopLeft The top left coordinate
     * @param pBottomRight The bottom right coordinate
     * @param pZoomLevel The zoom level
     * @param pObserver The image observer
     */
    public void paintAttribution(Graphics pGraphics, int pWidth, int pHeight, 
    		Coordinate pTopLeft, Coordinate pBottomRight, int pZoomLevel, ImageObserver pObserver) 
    {
        if (aSource == null || !aSource.requiresAttribution())
        {
            return;
        }
        
        Font font = pGraphics.getFont();
        pGraphics.setFont(ATTR_LINK_FONT);

        // Draw terms of use text
        int termsTextHeight = 0;
        int termsTextY = pHeight;

        // CSOFF:
        if(aAttributionTermsText != null) 
        {
            Rectangle2D termsStringBounds = pGraphics.getFontMetrics().getStringBounds(aAttributionTermsText, pGraphics);
            int textRealHeight = (int) termsStringBounds.getHeight();
            termsTextHeight = textRealHeight - 5;
            int termsTextWidth = (int) termsStringBounds.getWidth();
            termsTextY = pHeight - termsTextHeight;
            int x = 2;
            int y = pHeight - termsTextHeight;
            aAttributionTermsBounds = new Rectangle(x, y-termsTextHeight, termsTextWidth, textRealHeight);
            pGraphics.setColor(Color.black);
            pGraphics.drawString(aAttributionTermsText, x + 1, y + 1);
            pGraphics.setColor(Color.white);
            pGraphics.drawString(aAttributionTermsText, x, y);
        }

        // Draw attribution logo
        if (aAttributionImage != null) 
        {
            int x = 2;
            int imgWidth = aAttributionImage.getWidth(pObserver);
            int imgHeight = aAttributionImage.getHeight(pObserver);
            int y = termsTextY - imgHeight - termsTextHeight - 5;
            aAttributionImageBounds = new Rectangle(x, y, imgWidth, imgHeight);
            pGraphics.drawImage(aAttributionImage, x, y, null);
        }

        pGraphics.setFont(ATTR_FONT);
        String attributionText = aSource.getAttributionText(pZoomLevel, pTopLeft, pBottomRight);
        if (attributionText != null) 
        {
            Rectangle2D stringBounds = pGraphics.getFontMetrics().getStringBounds(attributionText, pGraphics);
            int textHeight = (int) stringBounds.getHeight() - 5;
            int x = pWidth - (int) stringBounds.getWidth();
            int y = pHeight - textHeight;
            pGraphics.setColor(Color.black);
            pGraphics.drawString(attributionText, x + 1, y + 1);
            pGraphics.setColor(Color.white);
            pGraphics.drawString(attributionText, x, y);
            aAttributionTextBounds = new Rectangle(x, y-textHeight, (int) stringBounds.getWidth(), (int) stringBounds.getHeight());
        }

        pGraphics.setFont(font);
        // CSON:
    }

    /**
     * Make it possible to click the attribution decorations
     * and view the terms of use.
     * @param pPoint The point clicked?
     * @param pClicked Whether the point was clicked?
     * @return True if the attribution was opened
     */
    public boolean handleAttribution(Point pPoint, boolean pClicked) 
    {
        if (aSource == null || !aSource.requiresAttribution())
        {
            return false;
        }

        /* TODO: Somehow indicate the link is clickable state to user */

        if(aAttributionTextBounds != null && aAttributionTextBounds.contains(pPoint))
        {
            String attributionURL = aSource.getAttributionLinkURL();
            if(attributionURL != null) 
            {
                if (pClicked) 
                {
                    FeatureAdapter.openLink(attributionURL);
                }
                return true;
            }
        } 
        else if( aAttributionImageBounds != null && aAttributionImageBounds.contains(pPoint))
        {
            String attributionImageURL = aSource.getAttributionImageURL();
            if (attributionImageURL != null) 
            {
                if (pClicked) 
                {
                    FeatureAdapter.openLink(aSource.getAttributionImageURL());
                }
                return true;
            }
        } 
        else if(aAttributionTermsBounds != null && aAttributionTermsBounds.contains(pPoint)) 
        {
            String termsOfUseURL = aSource.getTermsOfUseURL();
            if (termsOfUseURL != null)
            {
                if (pClicked) 
                {
                    FeatureAdapter.openLink(termsOfUseURL);
                }
                return true;
            }
        }
        return false;
    }

}

