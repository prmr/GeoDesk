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
    public TileJob createTileLoaderJob(final Tile pTile)
    {
        return new TileJob() {

            private InputStream aInput = null;

            public void run()
            {
                synchronized (pTile) 
                {
                    if ((pTile.isLoaded() && !pTile.isError()) || pTile.isLoading())
                    {
                        return;
                    }
                    pTile.setLoaded(false);
                    pTile.setError(false);
                    pTile.setLoading(true);
                }
                try
                {
                    URLConnection conn = loadTileFromOsm(pTile);
                    loadTileMetadata(pTile, conn);
                    if ("no-tile".equals(pTile.getValue("tile-info")))
                    {
                        pTile.setError();
                    }
                    else
                    {
                        aInput = conn.getInputStream();
                        pTile.loadImage(aInput);
                        aInput.close();
                        aInput = null;
                    }
                    pTile.setLoaded(true);
                    aListener.tileLoadingFinished(pTile, true);
                }
                catch(Exception e)
                {
                    pTile.setError();
                    aListener.tileLoadingFinished(pTile, false);
                    if (aInput == null)
                    {
                        try
                        {
                            System.err.println("Failed loading " + pTile.getUrl() +": " + e.getMessage());
                        }
                        catch(IOException i)
                        {
                        }
                    }
                }
                finally
                {
                    pTile.setLoading(false);
                    pTile.setLoaded(true);
                }
            }

            public Tile getTile()
            {
                return pTile;
            }
        };
    }

    protected URLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(tile.getUrl());
        URLConnection urlConn = url.openConnection();
        if (urlConn instanceof HttpURLConnection) {
            prepareHttpUrlConnection((HttpURLConnection)urlConn);
        }
        urlConn.setReadTimeout(30000); // 30 seconds read timeout
        return urlConn;
    }

    protected void loadTileMetadata(Tile tile, URLConnection urlConn) {
        String str = urlConn.getHeaderField("X-VE-TILEMETA-CaptureDatesRange");
        if (str != null) {
            tile.putValue("capture-date", str);
        }
        str = urlConn.getHeaderField("X-VE-Tile-Info");
        if (str != null) {
            tile.putValue("tile-info", str);
        }
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        for(Entry<String, String> e : aHeaders.entrySet()) {
            urlConn.setRequestProperty(e.getKey(), e.getValue());
        }
        if(aTimeoutConnect != 0)
            urlConn.setConnectTimeout(aTimeoutConnect);
        if(aTimeoutRead != 0)
            urlConn.setReadTimeout(aTimeoutRead);
    }

    @Override
    public String toString() 
    {
        return getClass().getSimpleName();
    }
}
