package org.openstreetmap.gui.jmapviewer.tilesources;

/**
 * Cycle map tile source.
 */
public class CycleOsmTileSource extends AbstractOsmTileSource
{
	private static final int MAX_ZOOM = 17;

	private static final String PATTERN = "http://%s.tile.opencyclemap.org/cycle";

    private static final String[] SERVER = { "a", "b", "c" };

    private int aServerNumber = 0;

    /**
     * Create a cycle map tile source.
     */
    public CycleOsmTileSource()
    {
        super("OSM Cycle Map", PATTERN);
    }

    @Override
    public String getBaseUrl()
    {
        String url = String.format(this.aBaseUrl, new Object[] { SERVER[aServerNumber] });
        aServerNumber = (aServerNumber + 1) % SERVER.length;
        return url;
    }

    @Override
    public int getMaxZoom()
    {
        return MAX_ZOOM;
    }

    @Override
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.LastModified;
    }
}
