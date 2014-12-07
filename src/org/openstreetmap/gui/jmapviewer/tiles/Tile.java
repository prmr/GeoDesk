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
package org.openstreetmap.gui.jmapviewer.tiles;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * Holds one map tile. Additionally the code for loading the tile image and
 * painting it is also included in this class.
 *
 * @author Jan Peter Stotz
 * @author Martin P. Robillard
 */
public class Tile 
{
	private static final int MAX_ZOOM_DIFF = 5;
	private static final BufferedImage ERROR_IMAGE = loadImage("images/error.png");
	private static final BufferedImage LOADING_IMAGE = loadImage("images/hourglass.png");

    private TileSource aSource;
    private int aXTile;
    private int aYTile;
    private int aZoom;
    private BufferedImage aImage;
    private String aKey;
    private boolean aLoaded = false;
    private boolean aLoading = false;
    private boolean aError = false;
    
    // TileLoader-specific tile metadata
    private Map<String, String> aMetaData;

    /**
     * Creates a tile with empty image.
     *
     * @param pSource The tile source
     * @param pXTile The x coordinate
     * @param pYTile The y coordinate
     * @param pZoom The zoom level
     */
    public Tile(TileSource pSource, int pXTile, int pYTile, int pZoom) 
    {
        aSource = pSource;
        aXTile = pXTile;
        aYTile = pYTile;
        aZoom = pZoom;
        aImage = LOADING_IMAGE;
        aKey = getTileKey(pSource, pXTile, pYTile, pZoom);
    }

    /**
     * Creates a tile with an image.
     * @param pSource The tile source
     * @param pXTile The x coordinate
     * @param pYTile The y coordinate
     * @param pZoom The zoom level
     * @param pImage The actual image
     */
    public Tile(TileSource pSource, int pXTile, int pYTile, int pZoom, BufferedImage pImage) 
    {
        this(pSource, pXTile, pYTile, pZoom);
        aImage = pImage;
    }
    
    private static BufferedImage loadImage(String pRelativePath)
    {
    	try
    	{
    		return ImageIO.read(JMapViewer.class.getResourceAsStream(pRelativePath));
    	}
    	catch(IOException e)
    	{
    		return null;
    	}
    }

    /**
     * Tries to get tiles of a lower or higher zoom level (one or two level
     * difference) from cache and use it as a placeholder until the tile has
     * been loaded.
     * @param pCache The tile cache.
     */
    public void loadPlaceholderFromCache(TileCache pCache) 
    {
        BufferedImage tmpImage = new BufferedImage(aSource.getTileSize(), aSource.getTileSize(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) tmpImage.getGraphics();
        for(int zoomDiff = 1; zoomDiff < MAX_ZOOM_DIFF; zoomDiff++) 
        {
            // first we check if there are already the 2^x tiles
            // of a higher detail level
            int zoomHigh = aZoom + zoomDiff;
            if (zoomDiff < 3 && zoomHigh <= JMapViewer.MAX_ZOOM) 
            {
                int factor = 1 << zoomDiff;
                int xTileHigh = aXTile << zoomDiff;
                int yTileHigh = aYTile << zoomDiff;
                double scale = 1.0 / factor;
                g.setTransform(AffineTransform.getScaleInstance(scale, scale));
                int paintedTileCount = 0;
                for(int x = 0; x < factor; x++) 
                {
                    for (int y = 0; y < factor; y++) 
                    {
                        Tile tile = pCache.getTile(aSource, xTileHigh + x, yTileHigh + y, zoomHigh);
                        if (tile != null && tile.isLoaded()) 
                        {
                            paintedTileCount++;
                            tile.paint(g, x * aSource.getTileSize(), y * aSource.getTileSize());
                        }
                    }
                }
                if (paintedTileCount == factor * factor) 
                {
                    aImage = tmpImage;
                    return;
                }
            }

            int zoomLow = aZoom - zoomDiff;
            if(zoomLow >= JMapViewer.MIN_ZOOM) 
            {
                int xTileLow = aXTile >> zoomDiff;
                int yTileLow = aYTile >> zoomDiff;
                int factor = 1 << zoomDiff;
                double scale = factor;
                AffineTransform at = new AffineTransform();
                int translateX = (aXTile % factor) * aSource.getTileSize();
                int translateY = (aYTile % factor) * aSource.getTileSize();
                at.setTransform(scale, 0, 0, scale, -translateX, -translateY);
                g.setTransform(at);
                Tile tile = pCache.getTile(aSource, xTileLow, yTileLow, zoomLow);
                if (tile != null && tile.isLoaded()) 
                {
                    tile.paint(g, 0, 0);
                    aImage = tmpImage;
                    return;
                }
            }
        }
    }

    /**
     * @return The source for this tile.
     */
    public TileSource getSource() 
    {
        return aSource;
    }

    /**
     * @return tile number on the x axis of this tile
     */
    public int getXtile() 
    {
        return aXTile;
    }

    /**
     * @return tile number on the y axis of this tile
     */
    public int getYtile() 
    {
        return aYTile;
    }

    /**
     * @return zoom level of this tile
     */
    public int getZoom() 
    {
        return aZoom;
    }

    /**
     * Load the image for this tile from pInput.
     * @param pInput The stream to load the image from.
     * @throws IOException If we can't load the image.
     */
    public void loadImage(InputStream pInput) throws IOException
    {
        aImage = ImageIO.read(pInput);
    }

    /**
     * @return key that identifies a tile
     */
    public String getKey() 
    {
        return aKey;
    }

    /**
     * @return True if the image is loaded.
     */
    public boolean isLoaded()
    {
        return aLoaded;
    }

    /**
     * @return True if the image is currently loading.
     */
    public boolean isLoading() 
    {
        return aLoading;
    }

    /**
     * Set the loaded flag.
     * @param pLoaded True if the image is loaded.
     */
    public void setLoaded(boolean pLoaded) 
    {
        aLoaded = pLoaded;
    }

    /**
     * @return The url for this tile.
     * @throws IOException If we can't obtain it.
     */
    public String getUrl() throws IOException 
    {
        return aSource.getTileUrl(aZoom, aXTile, aYTile);
    }

    /**
     * Paints the tile-image on the {@link Graphics} <code>g</code> at the
     * position <code>x</code>/<code>y</code>.
     *
     * @param pGraphics The graphics element.
     * @param pX x-coordinate in <code>g</code>
     * @param pY y-coordinate in <code>g</code>
     */
    public void paint(Graphics pGraphics, int pX, int pY) 
    {
        if(aImage != null)
        {
        	pGraphics.drawImage(aImage, pX, pY, null);
        }
    }

    @Override
    public String toString()
    {
        return "Tile " + aKey;
    }

    /**
     * Note that the hash code does not include the {@link #aSource}.
     * Therefore a hash based collection can only contain tiles
     * of one {@link #aSource}.
     * @return the hashcode for this tile.
     */
    @Override
    public int hashCode() 
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + aXTile;
        result = prime * result + aYTile;
        result = prime * result + aZoom;
        return result;
    }

    /**
     * Compares this object with <code>obj</code> based on
     * the fields {@link #aXTile}, {@link #aYTile} and
     * {@link #aZoom}.
     * The {@link #aSource} field is ignored.
     * @param pObject the object to compare against.
     * @return true if this tile is equals to pObject
     */
    @Override
    public boolean equals(Object pObject)
{
        if (this == pObject)
        {
            return true;
        }
        if(pObject == null)
        {
            return false;
        }
        if(getClass() != pObject.getClass())
        {
            return false;
        }
        Tile other = (Tile) pObject;
        if(aXTile != other.aXTile)
        {
            return false;
        }
        if(aYTile != other.aYTile)
        {
            return false;
        }
        if(aZoom != other.aZoom)
        {
            return false;
        }
        return true;
    }

    /**
     * @param pSource The tile source
     * @param pXTile The x coordinate
     * @param pYTile The y coordinate
     * @param pZoom The zoom level
     * @return The key for this tile.
     */
    public static String getTileKey(TileSource pSource, int pXTile, int pYTile, int pZoom) 
    {
        return pZoom + "/" + pXTile + "/" + pYTile + "@" + pSource.getName();
    }

    /**
     * Indicate that there is a problem with this tile.
     */
    public void setError()
    {
        aError = true;
        aImage = ERROR_IMAGE;
    }

    /**
     * Puts the given key/value pair to the metadata of the tile.
     * If value is null, the (possibly existing) key/value pair is removed from 
     * the meta data.
     * 
     * @param pKey The key
     * @param pValue The value
     */
    public void putValue(String pKey, String pValue) 
    {
        if (pValue == null || pValue.isEmpty()) 
        {
            if (aMetaData != null) 
            {
                aMetaData.remove(pKey);
            }
            return;
        }
        if (aMetaData == null) 
        {
            aMetaData = new HashMap<String, String>();
        }
        aMetaData.put(pKey, pValue);
    }

    /**
     * Get the meta-data value for pKey.
     * @param pKey The key to look up.
     * @return The value for pKey
     */
    public String getValue(String pKey) 
    {
        if (aMetaData == null)
        {
        	return null;
        }
        return aMetaData.get(pKey);
    }

    /**
     * @return The meta-data structure.
     */
    public Map<String, String> getMetadata() 
    {
        return aMetaData;
    }

	/**
	 * Sets a new value for the loading flag.
	 * @param pLoading The new value.
	 */
	public void setLoading(boolean pLoading)
	{
		aLoading = pLoading;
	}

	/**
	 * @return True if the error flag is set.
	 */
	public boolean isError()
	{
		return aError;
	}

	/**
	 * Sets the error flag.
	 * @param pError The new value for the error flag.
	 */
	public void setError(boolean pError)
	{
		aError = pError;
	}
}
