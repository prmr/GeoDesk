package org.openstreetmap.gui.jmapviewer.tiles;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource.TileUpdate;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP and
 * saves all loaded files in a directory located in the temporary directory.
 * If a tile is present in this file cache it will not be loaded from OSM again.
 *
 * @author Jan Peter Stotz
 * @author Stefan Zeller
 */
// CSOFF:
public class OsmFileCacheTileLoader extends OsmTileLoader 
{
    private static final Logger LOGGER = Logger.getLogger(OsmFileCacheTileLoader.class.getName());
    
    private static final String ETAG_FILE_EXT = ".etag";
    private static final String TAGS_FILE_EXT = ".tags";

    private static final Charset TAGS_CHARSET = Charset.forName("UTF-8");

    private static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
    private static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;
    private static final long MAX_CACHE_FILE_AGE = FILE_AGE_ONE_WEEK;

    private String aCacheDirBase;
    
    private final Map<TileSource, File> aSourceCacheDirMap;

    /**
     * Sets the maximum age of the local cached tile in the file system. If a
     * local tile is older than the specified file age
     * {@link OsmFileCacheTileLoader} will connect to the tile server and check
     * if a newer tile is available using the mechanism specified for the
     * selected tile source/server.
     */
    
    private long aRecheckAfter = FILE_AGE_ONE_DAY;

    /**
     * Create a OSMFileCacheTileLoader with given cache directory.
     * If cacheDir is not set or invalid, IOException will be thrown.
     * @param pMap the listener checking for tile load events (usually the map for display)
     * @param pCacheDirectory directory to store cached tiles
     * @throws IOException if there's a problem
     */
    public OsmFileCacheTileLoader(TileLoaderListener pMap, File pCacheDirectory) throws IOException  
    {
        super(pMap);
        if(pCacheDirectory == null || (!pCacheDirectory.exists() && !pCacheDirectory.mkdirs()))
        {
            throw new IOException("Cannot access cache directory");
        }

        LOGGER.finest("Tile cache directory: " + pCacheDirectory);
        aCacheDirBase = pCacheDirectory.getAbsolutePath();
        aSourceCacheDirMap = new HashMap<TileSource, File>();
    }
    
    /**
     * Create a OSMFileCacheTileLoader with system property temp dir.
     * If not set an IOException will be thrown.
     * @param pMap the listener checking for tile load events (usually the map for display)
     * @throws IOException if there's a problem
     */
    public OsmFileCacheTileLoader(TileLoaderListener pMap) throws IOException 
    {
        this(pMap, getDefaultCacheDir());
    }
    
    private static File getDefaultCacheDir()
    {
        String tempDir = null;
        String userName = System.getProperty("user.name");
        try 
        {
            tempDir = System.getProperty("java.io.tmpdir");
        } 
        catch(SecurityException e) 
        {
            LOGGER.log(Level.WARNING, "Failed to access system property ''java.io.tmpdir'' for security reasons. Exception was: " + e.toString());
            throw e; 
        }
        try 
        {
            if(tempDir == null)
            {
                throw new IOException("No temp directory set");
            }
            String subDirName = "JMapViewerTiles";
            // On Linux/Unix systems we do not have a per user tmp directory.
            // Therefore we add the user name for getting a unique dir name.
            if (userName != null && userName.length() > 0) 
            {
                subDirName += "_" + userName;
            }
            File cacheDir = new File(tempDir, subDirName);
            return cacheDir;
        } 
        catch(Exception e) 
        {
        	// TODO
        }
        return null;
    }

    @Override
    public TileJob createTileLoaderJob(Tile pTile) 
    {
        return new FileLoadJob(pTile);
    }

    private File getSourceCacheDir(TileSource pSource)
    {
        File dir = aSourceCacheDirMap.get(pSource);
        if (dir == null) 
        {
            dir = new File(aCacheDirBase, pSource.getName().replaceAll("[\\\\/:*?\"<>|]", "_"));
            if(!dir.exists()) 
            {
                dir.mkdirs();
            }
        }
        return dir;
    }
    
    private class FileLoadJob implements TileJob 
    {
        private InputStream aInput = null;
        private Tile aTile;
        private File aTileCacheDir;
        private File aTileFile = null;
        private long aFileAge = 0;
        private boolean aFileTilePainted = false;

        public FileLoadJob(Tile pTile) 
        {
            aTile = pTile;
        }

        public Tile getTile() 
        {
            return aTile;
        }

        public void run() 
        {
            synchronized (aTile)
            {
                if((aTile.isLoaded() && !aTile.isError()) || aTile.isLoading())
                {
                    return;
                }
                aTile.setLoaded(false);
                aTile.setError(false);
                aTile.setLoading(true);
            }
            aTileCacheDir = getSourceCacheDir(aTile.getSource());
            if(loadTileFromFile()) 
            {
                return;
            }
            if(aFileTilePainted) 
            {
                TileJob job = new TileJob() 
                {
                    public void run() 
                    {
                        loadOrUpdateTile();
                    }
                    public Tile getTile() 
                    {
                        return aTile;
                    }
                };
                JobDispatcher.getInstance().addJob(job);
            } 
            else
            {
                loadOrUpdateTile();
            }
        }

        private void loadOrUpdateTile() 
        {
            try
            {
                URLConnection urlConn = loadTileFromOsm(aTile);
                if(aTileFile != null) 
                {
                    switch(aTile.getSource().getTileUpdate()) 
                    {
                    case IfModifiedSince:
                        urlConn.setIfModifiedSince(aFileAge);
                        break;
                    case LastModified:
                        if (!isOsmTileNewer(aFileAge)) 
                        {
                            LOGGER.finest("LastModified test: local version is up to date: " + aTile);
                            aTile.setLoaded(true);
                            aTileFile.setLastModified(System.currentTimeMillis() - MAX_CACHE_FILE_AGE + aRecheckAfter);
                            return;
                        }
                        break;
                    case None:
                    	break;
                    case IfNoneMatch:
                    	break;
                    case ETag:
                    	break;
                    }
                    
                }
                if (aTile.getSource().getTileUpdate() == TileUpdate.ETag || aTile.getSource().getTileUpdate() == TileUpdate.IfNoneMatch) 
                {
                    String fileETag = aTile.getValue("etag");
                    if (fileETag != null) 
                    {
                        switch (aTile.getSource().getTileUpdate()) 
                        {
                        case IfNoneMatch:
                            urlConn.addRequestProperty("If-None-Match", fileETag);
                            break;
                        case ETag:
                            if (hasOsmTileETag(fileETag)) 
                            {
                                aTile.setLoaded(true);
                                aTileFile.setLastModified(System.currentTimeMillis() - MAX_CACHE_FILE_AGE
                                        + aRecheckAfter);
                                return;
                            }
                        }
                    }
                    aTile.putValue("etag", urlConn.getHeaderField("ETag"));
                }
                if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 304) 
                {
                    // If we are isModifiedSince or If-None-Match has been set
                    // and the server answers with a HTTP 304 = "Not Modified"
                    LOGGER.finest("ETag test: local version is up to date: " + aTile);
                    aTile.setLoaded(true);
                    aTileFile.setLastModified(System.currentTimeMillis() - MAX_CACHE_FILE_AGE + aRecheckAfter);
                    return;
                }

                loadTileMetadata(aTile, urlConn);
                saveTagsToFile();

                if ("no-tile".equals(aTile.getValue("tile-info")))
                {
                    aTile.setError();
                    aListener.tileLoadingFinished(aTile, true);
                } 
                else 
                {
                    for(int i = 0; i < 5; ++i) 
                    {
                        if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 503)
                        {
                            Thread.sleep(5000+(new Random()).nextInt(5000));
                            continue;
                        }
                        byte[] buffer = loadTileInBuffer(urlConn);
                        if (buffer != null) 
                        {
                            aTile.loadImage(new ByteArrayInputStream(buffer));
                            aTile.setLoaded(true);
                            aListener.tileLoadingFinished(aTile, true);
                            saveTileToFile(buffer);
                            break;
                        }
                    }
                }
            } 
            catch (Exception e) 
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

        protected boolean loadTileFromFile()
        {
            FileInputStream fin = null;
            try 
            {
                aTileFile = getTileFile();
                if(!aTileFile.exists())
                {
                    return false;
                }

                loadTagsFromFile();
                if ("no-tile".equals(aTile.getValue("tile-info")))
                {
                    aTile.setError();
                    if (aTileFile.exists()) 
                    {
                        aTileFile.delete();
                    }
                    aTileFile = getTagsFile();
                } 
                else 
                {
                    fin = new FileInputStream(aTileFile);
                    if (fin.available() == 0)
                    {
                    	fin.close();
                        throw new IOException("File empty");
                    }
                    aTile.loadImage(fin);
                    fin.close();
                }

                aFileAge = aTileFile.lastModified();
                boolean oldTile = System.currentTimeMillis() - aFileAge > MAX_CACHE_FILE_AGE;
                if(!oldTile) 
                {
                    aTile.setLoaded(true);
                    aListener.tileLoadingFinished(aTile, true);
                    aFileTilePainted = true;
                    return true;
                }
                aListener.tileLoadingFinished(aTile, true);
                aFileTilePainted = true;
            } 
            catch(Exception e) 
            {
                try 
                {
                    if (fin != null) 
                    {
                        fin.close();
                        aTileFile.delete();
                    }
                } 
                catch (Exception e1) 
                {
                }
                aTileFile = null;
                aFileAge = 0;
            }
            return false;
        }

        protected byte[] loadTileInBuffer(URLConnection pUrlConnection) throws IOException 
        {
            aInput = pUrlConnection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(aInput.available());
            byte[] buffer = new byte[2048];
            boolean finished = false;
            do 
            {
                int read = aInput.read(buffer);
                if (read >= 0) 
                {
                    bout.write(buffer, 0, read);
                } 
                else 
                {
                    finished = true;
                }
            } 
            while (!finished);
            if(bout.size() == 0)
            {
                return null;
            }
            return bout.toByteArray();
        }

        /**
         * Performs a <code>HEAD</code> request for retrieving the
         * <code>LastModified</code> header value.
         *
         * Note: This does only work with servers providing the
         * <code>LastModified</code> header:
         * <ul>
         * <li>{@link tilesources.CycleOsmTileSource} - supported</li>
         * <li>{@link tilesources.MapnikOsmTileSource} - not supported</li>
         * </ul>
         *
         * @param pFileAge time of the 
         * @return <code>true</code> if the tile on the server is newer than the
         *         file
         * @throws IOException
         */
        protected boolean isOsmTileNewer(long pFileAge) throws IOException 
        {
            URL url;
            url = new URL(aTile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            long lastModified = urlConn.getLastModified();
            if(lastModified == 0)
            {
                return true; // no LastModified time returned
            }
            return lastModified > pFileAge;
        }

        protected boolean hasOsmTileETag(String pETag) throws IOException 
        {
            URL url;
            url = new URL(aTile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            String osmETag = urlConn.getHeaderField("ETag");
            if(osmETag == null)
            {
                return true;
            }
            return osmETag.equals(pETag);
        }

        private File getTileFile()
        {
            return new File(aTileCacheDir + "/" + aTile.getZoom() + "_" + aTile.getXtile() + "_" + aTile.getYtile() + "."
                    + aTile.getSource().getTileType());
        }

        private File getTagsFile() 
        {
            return new File(aTileCacheDir + "/" + aTile.getZoom() + "_" + aTile.getXtile() + "_" + aTile.getYtile()
                    + TAGS_FILE_EXT);
        }

        private void saveTileToFile(byte[] pRawData) 
        {
            try
            {
                FileOutputStream f = new FileOutputStream(aTileCacheDir + "/" + aTile.getZoom() + "_" + aTile.getXtile()
                        + "_" + aTile.getYtile() + "." + aTile.getSource().getTileType());
                f.write(pRawData);
                f.close();
            } 
            catch (Exception e) 
            {
                System.err.println("Failed to save tile content: " + e.getLocalizedMessage());
            }
        }

        private void saveTagsToFile()
        {
            File tagsFile = getTagsFile();
            if (aTile.getMetadata() == null) 
            {
                tagsFile.delete();
                return;
            }
            try 
            {
                final PrintWriter f = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tagsFile),
                        TAGS_CHARSET));
                for (Entry<String, String> entry : aTile.getMetadata().entrySet()) 
                {
                    f.println(entry.getKey() + "=" + entry.getValue());
                }
                f.close();
            } 
            catch (Exception e) 
            {
                System.err.println("Failed to save tile tags: " + e.getLocalizedMessage());
            }
        }

        /** Load backward-compatiblity .etag file and if it exists move it to new .tags file. */
        private void loadOldETagfromFile() 
        {
            File etagFile = new File(aTileCacheDir, aTile.getZoom() + "_"
                    + aTile.getXtile() + "_" + aTile.getYtile() + ETAG_FILE_EXT);
            if(!etagFile.exists())
            {
            	return;
            }
            try 
            {
                FileInputStream f = new FileInputStream(etagFile);
                byte[] buf = new byte[f.available()];
                f.read(buf);
                f.close();
                String etag = new String(buf, TAGS_CHARSET.name());
                aTile.putValue("etag", etag);
                if (etagFile.delete()) 
                {
                    saveTagsToFile();
                }
            } 
            catch(IOException e) 
            {
                System.err.println("Failed to load compatiblity etag: " + e.getLocalizedMessage());
            }
        }

        private void loadTagsFromFile() 
        {
            loadOldETagfromFile();
            File tagsFile = getTagsFile();
            try 
            {
                final BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(tagsFile),
                        TAGS_CHARSET));
                for (String line = f.readLine(); line != null; line = f.readLine()) 
                {
                    final int i = line.indexOf('=');
                    if (i == -1 || i == 0) 
                    {
                        System.err.println("Malformed tile tag in file '" + tagsFile.getName() + "':" + line);
                        continue;
                    }
                    aTile.putValue(line.substring(0, i), line.substring(i+1));
                }
                f.close();
            } 
            catch (FileNotFoundException e) 
            {
            } 
            catch (Exception e) 
            {
                System.err.println("Failed to load tile tags: " + e.getLocalizedMessage());
            }
        }

    }
}
