package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * The MapQuest aerial tile source.
 */
public class MapQuestOpenAerialTileSource extends AbstractMapQuestTileSource
{
    private static final String PATTERN = "http://oatile%d.mqcdn.com/tiles/1.0.0/sat";

    /**
     * Constructs a MapQuest aerial tile source.
     */
    public MapQuestOpenAerialTileSource()
    {
        super("MapQuest Open Aerial", PATTERN);
    }

    @Override
    public String getAttributionText(int pZoom, Coordinate pTopLeft, Coordinate pBottomRight)
    {
        return "Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency - "+ MAPQUEST_ATTRIBUTION;
    }

    @Override
    public String getAttributionLinkURL()
    {
        return MAPQUEST_WEBSITE;
    }
}
