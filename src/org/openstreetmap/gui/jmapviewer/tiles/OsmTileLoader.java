package org.openstreetmap.gui.jmapviewer.tiles;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM.
 *
 * @author Jan Peter Stotz
 */
public class OsmTileLoader implements TileLoader
{
	private static final int READ_TIMEOUT = 30000;

	protected TileLoaderListener aListener;
    
    // Holds the HTTP headers. Insert e.g. User-Agent here when default should not be used.
	private Map<String, String> aHeaders = new HashMap<>();
    private int aTimeoutConnect = 0;
    private int aTimeoutRead = 0;

    /**
     * New tile loader with a single listener.
     * @param pListener The listener for this loader.
     */
    public OsmTileLoader(TileLoaderListener pListener) 
    {
    	aHeaders.put("Accept", "text/html, image/png, image/jpeg, image/gif, */*");
    	aListener = pListener;
    }

    @Override
    public TileJob createTileLoaderJob(Tile pTile)
    {
    	return new TileLoaderJob(pTile);
    }

    /**
     * Load a tile from OSM.
     * @param pTile The tile to load
     * @return The URL connection 
     * @throws IOException Something happened
     */
    protected URLConnection loadTileFromOsm(Tile pTile) throws IOException 
    {
        URL url;
        url = new URL(pTile.getUrl());
        URLConnection urlConn = url.openConnection();
        if (urlConn instanceof HttpURLConnection) 
        {
            prepareHttpUrlConnection((HttpURLConnection)urlConn);
        }
        urlConn.setReadTimeout(READ_TIMEOUT); 
        return urlConn;
    }

    /**
     * Load the metadata for the tile.
     * @param pTile The tile
     * @param pUrlConnection The URL connection
     */
    protected void loadTileMetadata(Tile pTile, URLConnection pUrlConnection) 
    {
        String str = pUrlConnection.getHeaderField("X-VE-TILEMETA-CaptureDatesRange");
        if (str != null) 
        {
            pTile.putValue("capture-date", str);
        }
        str = pUrlConnection.getHeaderField("X-VE-Tile-Info");
        if (str != null) 
        {
            pTile.putValue("tile-info", str);
        }
    }

    /**
     * Prepare the Url Connection.
     * @param pUrlConnection The url connection to prepare.
     */
    protected void prepareHttpUrlConnection(HttpURLConnection pUrlConnection) 
    {
        for(Entry<String, String> entry : aHeaders.entrySet())
        {
            pUrlConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if(aTimeoutConnect != 0)
        {
            pUrlConnection.setConnectTimeout(aTimeoutConnect);
        }
        if(aTimeoutRead != 0)
        {
            pUrlConnection.setReadTimeout(aTimeoutRead);
        }
    }

    @Override
    public String toString() 
    {
        return getClass().getSimpleName();
    }
    
    private class TileLoaderJob implements TileJob
    {
    	private final Tile aTile;
    	private InputStream aInput = null;
    	
    	public TileLoaderJob(Tile pTile)
    	{
    		aTile = pTile;
    	}

        public void run()
        {
            synchronized (aTile) 
            {
                if ((aTile.isLoaded() && !aTile.isError()) || aTile.isLoading())
                {
                    return;
                }
                aTile.setLoaded(false);
                aTile.setError(false);
                aTile.setLoading(true);
            }
            try
            {
                URLConnection conn = loadTileFromOsm(aTile);
                loadTileMetadata(aTile, conn);
                if ("no-tile".equals(aTile.getValue("tile-info")))
                {
                    aTile.setError();
                }
                else
                {
                    aInput = conn.getInputStream();
                    aTile.loadImage(aInput);
                    aInput.close();
                    aInput = null;
                }
                aTile.setLoaded(true);
                aListener.tileLoadingFinished(aTile, true);
            }
            catch(IOException e)
            {
                aTile.setError();
                aListener.tileLoadingFinished(aTile, false);
                if (aInput == null)
                {
                    try
                    {
                        System.err.println("Failed loading " + aTile.getUrl() +": " + e.getMessage());
                    }
                    catch(IOException i)
                    {
                    }
                }
            }
            finally
            {
                aTile.setLoading(false);
                aTile.setLoaded(true);
            }
        }

        public Tile getTile()
        {
            return aTile;
        }
    }
}
