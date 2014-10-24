package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * The MapQuest tile source.
 */
public class MapQuestOsmTileSource extends AbstractMapQuestTileSource 
{
    private static final String PATTERN = "http://otile%d.mqcdn.com/tiles/1.0.0/osm";

    /**
     * Constructs a new MapQuest tile source.
     */
    public MapQuestOsmTileSource()
    {
        super("MapQuest-OSM", PATTERN);
    }
    
    @Override
    public String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight)
    {
        return super.getAttributionText(pZoom, pTopLeft, pBottomRight)+"- "+MAPQUEST_ATTRIBUTION;
    }
}
