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

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * An object (typically a tile source) with an attached attribution.
 */
public interface Attributed 
{
    /**
     * @return True if the object requires attribution in text or image form.
     */
    boolean requiresAttribution();

    /**
     * @param pZoom The optional zoom level for the view.
     * @param pBottomRight The bottom right of the bounding box for attribution.
     * @param pTopLeft The top left of the bounding box for attribution.
     * @return Attribution text for the object.
     */
    String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight);

    /**
     * @return The URL to open when the user clicks the attribution text.
     */
    String getAttributionLinkURL();

    /**
     * @return The URL for the attribution image. Null if no image should be displayed.
     */
    Image getAttributionImage();

    /**
     * @return The URL to open when the user clicks the attribution image.
     * When return value is null, the image is still displayed (provided getAttributionImage()
     * returns a value other than null), but the image does not link to a website.
     */
    String getAttributionImageURL();

    /**
     * @return The attribution "Terms of Use" text.
     * In case it returns null, but getTermsOfUseURL() is not null, a default
     * terms of use text is used.
     */
    String getTermsOfUseText();

    /**
     * @return The URL to open when the user clicks the attribution "Terms of Use" text.
     */
    String getTermsOfUseURL();
}
