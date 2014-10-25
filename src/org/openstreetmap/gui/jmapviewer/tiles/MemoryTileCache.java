package org.openstreetmap.gui.jmapviewer.tiles;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.util.Hashtable;
import java.util.logging.Logger;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * {@link TileCache} implementation that stores all {@link Tile} objects in
 * memory up to a certain limit ({@link #getCacheSize()}). If the limit is
 * exceeded the least recently used {@link Tile} objects will be deleted.
 *
 * @author Jan Peter Stotz
 */
public class MemoryTileCache implements TileCache
{
    private static final int CACHE_SIZE = 200;

	private static final Logger LOGGER = Logger.getLogger(MemoryTileCache.class.getName());
    private Hashtable<String, CacheEntry> aEntries;

    // List of all tiles in their last recently used order.
    private CacheLinkedListElement aLastRecentlyUsedTiles;

    /**
     * Constructs a new in-memory cache.
     */
    public MemoryTileCache() 
    {
        aEntries = new Hashtable<String, CacheEntry>(CACHE_SIZE);
        aLastRecentlyUsedTiles = new CacheLinkedListElement();
    }

    @Override
    public void addTile(Tile pTile) 
    {
        CacheEntry entry = new CacheEntry(pTile);
        aEntries.put(pTile.getKey(), entry);
        aLastRecentlyUsedTiles.addFirst(entry);
        if(aEntries.size() > CACHE_SIZE)
        {
            removeOldEntries();
        }
    }

    @Override
    public Tile getTile(TileSource pTileSource, int pTileX, int pTileY, int pZoom) 
    {
        CacheEntry entry = aEntries.get(Tile.getTileKey(pTileSource, pTileX, pTileY, pZoom));
        if(entry == null)
        {
            return null;
        }
        // We don't care about placeholder tiles and hourglass image tiles, the
        // important tiles are the loaded ones
        if(entry.aTile.isLoaded())
        {
            aLastRecentlyUsedTiles.moveElementToFirstPos(entry);
        }
        return entry.aTile;
    }

    /**
     * Removes the least recently used tiles.
     */
    protected void removeOldEntries() 
    {
        synchronized(aLastRecentlyUsedTiles) 
        {
            try 
            {
                while (aLastRecentlyUsedTiles.getElementCount() > CACHE_SIZE) 
                {
                    removeEntry(aLastRecentlyUsedTiles.getLastElement());
                }
            } 
            catch(Exception e)
            {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    private void removeEntry(CacheEntry pEntry) 
    {
        aEntries.remove(pEntry.aTile.getKey());
        aLastRecentlyUsedTiles.removeEntry(pEntry);
    }

    /**
     * Linked list element holding the {@link Tile} and links to the
     * {@link #aNext} and {@link #aPrev} item in the list.
     */
    private static final class CacheEntry 
    {
        private Tile aTile;
        private CacheEntry aNext;
        private CacheEntry aPrev;

        private CacheEntry(Tile pTile) 
        {
            aTile = pTile;
        }
    }

    /**
     * Special implementation of a double linked list for {@link CacheEntry}
     * elements. It supports element removal in constant time - in difference to
     * the Java implementation which needs O(n).
     *
     * @author Jan Peter Stotz
     */
    private static class CacheLinkedListElement 
    {
    	private CacheEntry aFirstElement = null;
    	private CacheEntry aLastElement;
    	private int aElementCount;

        public CacheLinkedListElement() 
        {
            clear();
        }

        public synchronized void clear() 
        {
            aElementCount = 0;
            aFirstElement = null;
            aLastElement = null;
        }

        /**
         * Add the element to the head of the list.
         *
         * @param pElement new element to be added
         */
        public synchronized void addFirst(CacheEntry pElement) 
        {
            if (aElementCount == 0) 
            {
                aFirstElement = pElement;
                aLastElement = pElement;
                pElement.aPrev = null;
                pElement.aNext = null;
            } 
            else 
            {
                pElement.aNext = aFirstElement;
                aFirstElement.aPrev = pElement;
                pElement.aPrev = null;
                aFirstElement = pElement;
            }
            aElementCount++;
        }

        /**
         * Removes the specified element from the list.
         *
         * @param pElement element to be removed
         */
        public synchronized void removeEntry(CacheEntry pElement) 
        {
            if (pElement.aNext != null) 
            {
                pElement.aNext.aPrev = pElement.aPrev;
            }
            if (pElement.aPrev != null) 
            {
                pElement.aPrev.aNext = pElement.aNext;
            }
            if(pElement == aFirstElement)
            {
                aFirstElement = pElement.aNext;
            }
            if(pElement == aLastElement)
            {
                aLastElement = pElement.aPrev;
            }
            pElement.aNext = null;
            pElement.aPrev = null;
            aElementCount--;
        }

        public synchronized void moveElementToFirstPos(CacheEntry pEntry) 
        {
            if (aFirstElement == pEntry)
            {
                return;
            }
            removeEntry(pEntry);
            addFirst(pEntry);
        }

        public int getElementCount() 
        {
            return aElementCount;
        }

        public CacheEntry getLastElement()
        {
            return aLastElement;
        }
     }
}
