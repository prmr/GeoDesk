package org.openstreetmap.gui.jmapviewer.tilesources;

/**
 * Cycle map tile source.
 */
public class CycleOsmTileSource extends AbstractOsmTileSource
{
	private static final String PATTERN = "http://%s.tile.opencyclemap.org/cycle";

    private static final String[] SERVER = { "a", "b", "c" };

    private int SERVER_NUM = 0;

    public CycleOsmTileSource()
    {
        super("OSM Cycle Map", PATTERN);
    }

    @Override
    public String getBaseUrl()
    {
        String url = String.format(this.aBaseUrl, new Object[] { SERVER[SERVER_NUM] });
        SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
        return url;
    }

    @Override
    public int getMaxZoom()
    {
        return 17;
    }

    @Override
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.LastModified;
    }
}
