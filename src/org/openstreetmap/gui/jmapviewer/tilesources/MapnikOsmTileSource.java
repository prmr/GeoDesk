package org.openstreetmap.gui.jmapviewer.tilesources;


/**
 * Mapnik tile source.
 */
public class MapnikOsmTileSource extends AbstractOsmTileSource
{
	/**
	 * Create a new tile source.
	 */
	public MapnikOsmTileSource() 
	{
        super("Mapnik", "http://tile.openstreetmap.org");
    }

	@Override
    public TileUpdate getTileUpdate() 
    {
        return TileUpdate.IfNoneMatch;
    }
}
