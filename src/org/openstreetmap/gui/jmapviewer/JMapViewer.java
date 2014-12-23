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
package org.openstreetmap.gui.jmapviewer;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import static org.openstreetmap.gui.app.GeoDesk.MESSAGES;

import org.openstreetmap.gui.jmapviewer.JMVCommandEvent.CommandType;
import org.openstreetmap.gui.jmapviewer.tiles.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.tiles.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.tiles.Tile;
import org.openstreetmap.gui.jmapviewer.tiles.TileCache;
import org.openstreetmap.gui.jmapviewer.tiles.TileController;
import org.openstreetmap.gui.jmapviewer.tiles.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.tilesources.MapnikOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * Provides a simple panel that displays pre-rendered map tiles.
 *
 * @author Jan Peter Stotz
 * @author Jason Huntley
 * @author Martin P. Robillard - On-map legend
 *
 */
public class JMapViewer extends JPanel implements TileLoaderListener 
{
	public static final int MAX_ZOOM = 22;
	public static final int MIN_ZOOM = 0;
	
	private static final int PREFERRED_HEIGHT = 400;
	private static final int PREFERRED_WIDTH = 400;
	private static final int DEFAULT_LATITUDE = 50;
	private static final int DEFAULT_LONGITUDE = 9;
	private static final int DEFAULT_ZOOM_LEVEL = 3;
	
    private static final long serialVersionUID = 1L;

    /**
     * Vectors for clock-wise tile painting.
     */
    private static final Point[] MOVE = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
    private static final int LEGEND_OFFSET = 40; // How many pixels the legend is off the bottom left corner

    private List<MapMarker> aMapMarkerList;

    private boolean aMapMarkersVisible;
    private boolean aMapLegendVisible;
    private boolean aTileGridVisible;
    private TileController aTileController;
    private EventListenerList aListeners = new EventListenerList();

    /**
     * x- and y-position of the center of this map-panel on the world map
     * denoted in screen pixel regarding the current zoom level.
     */
    private Point aCenter;

    /**
     * Current zoom level.
     */
    private int aZoomLevel;

    private JSlider aZoomSlider;
    private JButton aZoomInButton;
    private JButton aZoomOutButton;
    private TileSource aTileSource;
    private AttributionSupport aAttributionSupport = new AttributionSupport();

    /**
     * Creates a standard {@link JMapViewer} instance that can be controlled via
     * mouse: hold right mouse button for moving, double click left mouse button
     * or use mouse wheel for zooming. Loaded tiles are stored the
     * {@link MemoryTileCache} and the tile loader uses 4 parallel threads for
     * retrieving the tiles.
     */
    public JMapViewer() 
    {
        this(new MemoryTileCache(), 4);
        new JMapController(this);
    }

    /**
     * Create a new JMapViewer with a specified tile cache and thread count.
     * @param pTileCache The tile cache object.
     * @param pDownloadThreadCount The number of threads to use for download.
     */
    public JMapViewer(TileCache pTileCache, int pDownloadThreadCount) 
    {
        super();
        aTileSource = new MapnikOsmTileSource();
        aTileController = new TileController(aTileSource, pTileCache, this);
        aMapMarkerList = new LinkedList<MapMarker>();
        aMapMarkersVisible = true;
        aMapLegendVisible = true;
        aTileGridVisible = false;
        setLayout(null);
        initializeZoomSlider();
        setMinimumSize(new Dimension(aTileSource.getTileSize(), aTileSource.getTileSize()));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setDisplayPositionByLatLon(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ZOOM_LEVEL);
    }

    /**
     * Returns all the markers visible at point pPoint on the map. The
     * function returns multiple markers in the case where they are overlaid 
     * on top of each other.
     * @param pPoint The point selected.
     * @return The map markers at pPoint.
     */
    public MapMarker[] getMapMarkersAt(Point pPoint)
    {
        List<MapMarker> lMarkers = new ArrayList<MapMarker>();
        for( MapMarker marker : getMapMarkerList())
        {
            Point lMarkerPoint = getMapPosition(marker.getLatitude(), marker.getLongitude());
            // CSOFF:
            if( lMarkerPoint != null && 
                pPoint.x > lMarkerPoint.x-marker.getRadius() && 
                pPoint.x < lMarkerPoint.x+marker.getRadius() &&
                pPoint.y > lMarkerPoint.y-marker.getRadius() &&
                pPoint.y < lMarkerPoint.y+marker.getRadius())
            {
                lMarkers.add(marker);
            }
            // CSON:
        }
        
        return lMarkers.toArray(new MapMarker[lMarkers.size()]);
    }

    // CHECKSTYLE DISABLE MagicNumber FOR 50 LINES
    private void initializeZoomSlider() 
    {
        aZoomSlider = new JSlider(MIN_ZOOM, aTileController.getTileSource().getMaxZoom());
        aZoomSlider.setOrientation(JSlider.VERTICAL);
        aZoomSlider.setBounds(10, 10, 30, 150);
        aZoomSlider.setOpaque(false);
        aZoomSlider.addChangeListener(new ChangeListener() 
        {
            public void stateChanged(ChangeEvent pEvent) 
            {
                setZoom(aZoomSlider.getValue());
            }
        });
        add(aZoomSlider);
        int size = 18;
        try 
        {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/plus.png"));
            aZoomInButton = new JButton(icon);
        } 
        catch (Exception e) 
        {
            aZoomInButton = new JButton("+");
            aZoomInButton.setFont(new Font("sansserif", Font.BOLD, 9));
            aZoomInButton.setMargin(new Insets(0, 0, 0, 0));
        }
        aZoomInButton.setBounds(4, 155, size, size);
        aZoomInButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent)
            {
                zoomIn();
            }
        });
        add(aZoomInButton);
        try 
        {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/minus.png"));
            aZoomOutButton = new JButton(icon);
        } 
        catch(Exception e) 
        {
            aZoomOutButton = new JButton("-");
            aZoomOutButton.setFont(new Font("sansserif", Font.BOLD, 9));
            aZoomOutButton.setMargin(new Insets(0, 0, 0, 0));
        }
        aZoomOutButton.setBounds(8 + size, 155, size, size);
        aZoomOutButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                zoomOut();
            }
        });
        add(aZoomOutButton);
    }

    /**
     * Changes the map pane so that it is centered on the specified coordinate
     * at the given zoom level.
     *
     * @param pLatitude Latitude of the specified coordinate
     * @param pLongitude Longitude of the specified coordinate
     * @param pZoomLevel {@link #MIN_ZOOM} <= zoom level <= {@link #MAX_ZOOM}
     */
    public void setDisplayPositionByLatLon(double pLatitude, double pLongitude, int pZoomLevel)
    {
        setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), pLatitude, pLongitude, pZoomLevel);
    }

    /**
     * Changes the map pane so that the specified coordinate at the given zoom
     * level is displayed on the map at the screen coordinate
     * <code>mapPoint</code>.
     *
     * @param pMapPoint point on the map denoted in pixels where the coordinate should
     *                 be set
     * @param pLatitude latitude of the specified coordinate
     * @param pLongitude longitude of the specified coordinate
     * @param pZoom {@link #MIN_ZOOM} <= zoom level <= {@link TileSource#getMaxZoom()}
     */
    public void setDisplayPositionByLatLon(Point pMapPoint, double pLatitude, double pLongitude, int pZoom) 
    {
        int x = OsmMercator.longitudeToX(pLongitude, pZoom);
        int y = OsmMercator.latitudeToY(pLatitude, pZoom);
        setDisplayPosition(pMapPoint, x, y, pZoom);
    }

    /**
     * Same as setDisplayPositionbyLatLon but using the center of the display
     * as the point.
     * @param pX The x coordinate
     * @param pY The y coordinate
     * @param pZoomLevel The zoome level
     */
    public void setDisplayPosition(int pX, int pY, int pZoomLevel)
    {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), pX, pY, pZoomLevel);
    }

    /**
     * Sets the position of the display. 
     * @param pMapPoint A point on the map
     * @param pX The x coordinate
     * @param pY The y coordinate
     * @param pZoomLevel The zoom level
     */
    public void setDisplayPosition(Point pMapPoint, int pX, int pY, int pZoomLevel) 
    {
        if (pZoomLevel > aTileController.getTileSource().getMaxZoom() || pZoomLevel < MIN_ZOOM)
        {
            return;
        }

        // Get the plain tile number
        Point p = new Point();
        p.x = pX - pMapPoint.x + getWidth() / 2;
        p.y = pY - pMapPoint.y + getHeight() / 2;
        aCenter = p;
        setIgnoreRepaint(true);
        try 
        {
            int oldZoom = this.aZoomLevel;
            this.aZoomLevel = pZoomLevel;
            if (oldZoom != pZoomLevel) 
            {
                zoomChanged(oldZoom);
            }
            if (aZoomSlider.getValue() != pZoomLevel) 
            {
                aZoomSlider.setValue(pZoomLevel);
            }
        } 
        finally 
        {
            setIgnoreRepaint(false);
            repaint();
        }
    }

    /**
     * Sets the displayed map pane and zoom level so that all map markers are
     * visible.
     */
    public void setDisplayToFitMapMarkers() 
    {
    	int nbElemToCheck = 0;
        if(aMapMarkerList != null)
        {
            nbElemToCheck += aMapMarkerList.size();
        }
        if (nbElemToCheck == 0)
        {
            return;
        }

        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMax = Integer.MIN_VALUE;
        int mapZoomMax = aTileController.getTileSource().getMaxZoom();

        for (MapMarker marker : aMapMarkerList) 
        {
        	int x = OsmMercator.longitudeToX(marker.getLongitude(), mapZoomMax);
            int y = OsmMercator.latitudeToY(marker.getLatitude(), mapZoomMax);
            xMax = Math.max(xMax, x);
            yMax = Math.max(yMax, y);
            xMin = Math.min(xMin, x);
            yMin = Math.min(yMin, y);
        }
        
        int height = Math.max(0, getHeight());
        int width = Math.max(0, getWidth());
        int newZoom = mapZoomMax;
        int x = xMax - xMin;
        int y = yMax - yMin;
        while (x > width || y > height) 
        {
            newZoom--;
            x >>= 1;
            y >>= 1;
        }
        x = xMin + (xMax - xMin) / 2;
        y = yMin + (yMax - yMin) / 2;
        int z = 1 << (mapZoomMax - newZoom);
        x /= z;
        y /= z;
        setDisplayPosition(x, y, newZoom);
    }

    /**
     * Calculates the latitude/longitude coordinate of the center of the
     * currently displayed map area.
     *
     * @return latitude / longitude
     */
    public Coordinate getPosition() 
    {
        double lon = OsmMercator.xToLongitude(aCenter.x, aZoomLevel);
        double lat = OsmMercator.yToLatitude(aCenter.y, aZoomLevel);
        return new Coordinate(lat, lon);
    }

    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate.
     *
     * @param pMapPoint relative pixel coordinate regarding the top left corner of the
     *            	   displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point pMapPoint) 
    {
        return getPosition(pMapPoint.x, pMapPoint.y);
    }

    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate.
     *
     * @param pMapPointX X coordinate on the map.
     * @param pMapPointY Y coordinate on the map.
     * @return latitude / longitude
     */
    public Coordinate getPosition(int pMapPointX, int pMapPointY)
    {
        int x = aCenter.x + pMapPointX - getWidth() / 2;
        int y = aCenter.y + pMapPointY - getHeight() / 2;
        double lon = OsmMercator.xToLongitude(x, aZoomLevel);
        double lat = OsmMercator.yToLatitude(y, aZoomLevel);
        System.out.println(lat + "," + lon); // TODO
        return new Coordinate(lat, lon);
    }

    /**
     * Calculates the position on the map of a given coordinate.
     *
     * @param pLatitude The latitude 
     * @param pLongitude The longitude
     * @param pCheckOutside Check outside the map.
     * @return point on the map or <code>null</code> if the point is not visible
     *         and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(double pLatitude, double pLongitude, boolean pCheckOutside) 
    {
        int x = OsmMercator.longitudeToX(pLongitude, aZoomLevel);
        int y = OsmMercator.latitudeToY(pLatitude, aZoomLevel);
        x -= aCenter.x - getWidth() / 2;
        y -= aCenter.y - getHeight() / 2;
        if (pCheckOutside) 
        {
            if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
            {
                return null;
            }
        }
        return new Point(x, y);
    }

    /**
     * Calculates the position on the map of a given coordinate.
     *
     * @param pLatitude Latitude
     * @param pLongitude Longitude
     * @return point on the map or <code>null</code> if the point is not visible
     */
    public Point getMapPosition(double pLatitude, double pLongitude) 
    {
        return getMapPosition(pLatitude, pLongitude, true);
    }

    /**
     * Calculates the position on the map of a given coordinate.
     *
     * @param pCoordinate Coordinate
     * @return point on the map or <code>null</code> if the point is not visible
     */
    public Point getMapPosition(Coordinate pCoordinate) 
    {
        if (pCoordinate != null)
        {
            return getMapPosition(pCoordinate.getLatitude(), pCoordinate.getLongitude());
        }
        else
        {
            return null;
        }
    }

    /**
     * Calculates the position on the map of a given coordinate.
     *
     * @param pCoordinate Coordinate
     * @param pCheckOutside Check outside?
     * @return point on the map or <code>null</code> if the point is not visible
     *         and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(Coordinate pCoordinate, boolean pCheckOutside) 
    {
        if (pCoordinate != null)
        {
            return getMapPosition(pCoordinate.getLatitude(), pCoordinate.getLongitude(), pCheckOutside);
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the meter per pixel.
     *
     * @return the meter per pixel
     * CHECKSTYLE DISABLE MagicNumber FOR 5 LINES
     */
    public double getMeterPerPixel() 
    {
        Point origin = new Point(5, 5);
        Point center = new Point(getWidth()/2, getHeight()/2);

        double pDistance = center.distance(origin);

        Coordinate originCoord = getPosition(origin);
        Coordinate centerCoord = getPosition(center);

        double mDistance = OsmMercator.getDistance(originCoord, centerCoord );

        return mDistance/pDistance;
    }

    // CSOFF:
    @Override
    protected void paintComponent(Graphics pGraphics) 
    {
        super.paintComponent(pGraphics);

        int iMove = 0;

        int tilesize = aTileSource.getTileSize();
        int tilex = aCenter.x / tilesize;
        int tiley = aCenter.y / tilesize;
        int offX = aCenter.x % tilesize;
        int offY = aCenter.y % tilesize;

        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        int posx = w2 - offX;
        int posy = h2 - offY;

        int diffLeft = offX;
        int diffRight = tilesize - offX;
        int diffTop = offY;
        int diffBottom = tilesize - offY;

        boolean startLeft = diffLeft < diffRight;
        boolean startTop = diffTop < diffBottom;

        if(startTop) 
        {
            if (startLeft) 
            {
                iMove = 2;
            } 
            else 
            {
                iMove = 3;
            }
        } 
        else
        {
            if(startLeft) 
            {
                iMove = 1;
            } 
            else 
            {
                iMove = 0;
            }
        } // calculate the visibility borders
        int xMin = -tilesize;
        int yMin = -tilesize;
        int xMax = getWidth();
        int yMax = getHeight();

        // paint the tiles in a spiral, starting from center of the map
        boolean painted = true;
        int x = 0;
        while (painted) 
        {
            painted = false;
            for (int i = 0; i < 4; i++) 
            {
                if (i % 2 == 0) 
                {
                    x++;
                }
                for (int j = 0; j < x; j++)
                {
                    if(xMin <= posx && posx <= xMax && yMin <= posy && posy <= yMax) 
                    {
                        // tile is visible
                        Tile tile = aTileController.getTile(tilex, tiley, aZoomLevel);
                        if (tile != null)
                        {
                            painted = true;
                            tile.paint(pGraphics, posx, posy);
                            if (aTileGridVisible) 
                            {
                                pGraphics.drawRect(posx, posy, tilesize, tilesize);
                            }
                        }
                    }
                    Point p = MOVE[iMove];
                    posx += p.x * tilesize;
                    posy += p.y * tilesize;
                    tilex += p.x;
                    tiley += p.y;
                }
                iMove = (iMove + 1) % MOVE.length;
            }
        }
        // outer border of the map
        int mapSize = tilesize << aZoomLevel;
        pGraphics.drawRect(w2 - aCenter.x, h2 - aCenter.y, mapSize, mapSize);

        if (aMapMarkersVisible && aMapMarkerList != null)
        {
            for (MapMarker marker : aMapMarkerList) 
            {
                paintMarker(pGraphics, marker);
            }
        }
        
        if (aMapLegendVisible )
        {
            paintLegend(pGraphics);
        }
        aAttributionSupport.paintAttribution(pGraphics, getWidth(), getHeight(), getPosition(0, 0), 
        		getPosition(getWidth(), getHeight()), aZoomLevel, this);
    } //CSON: NPAthComplexity
    
    // CHECKSTYLE DISABLE MagicNumber FOR 18 LINES
    private void paintLegend(Graphics pGraphics)
    {
        Graphics2D lGraphics = (Graphics2D)pGraphics;
        lGraphics.setStroke(new BasicStroke(3));
        lGraphics.drawLine(LEGEND_OFFSET, getHeight()-LEGEND_OFFSET, 100 + LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.drawLine(LEGEND_OFFSET, getHeight()-LEGEND_OFFSET+5, LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.drawLine(100+LEGEND_OFFSET, getHeight()-LEGEND_OFFSET+5, 100+LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.setFont(lGraphics.getFont().deriveFont(11f)); 
        long lMper100Pix = Math.round(getMeterPerPixel()*100);
        if( lMper100Pix < 1000 )
        {
            lGraphics.drawString(String.format("%dm", lMper100Pix), LEGEND_OFFSET+5, getHeight()-LEGEND_OFFSET+15);
        }
        else
        {
            double lKMper100Pix = getMeterPerPixel()/10;
            lGraphics.drawString(String.format("%dkm", Math.round(lKMper100Pix)), LEGEND_OFFSET+5, getHeight()-LEGEND_OFFSET+15);
        }
    }

    /**
     * Paint a single marker.
     */
    private void paintMarker(Graphics pGraphics, MapMarker pMarker) 
    {
        Point p = getMapPosition(pMarker.getLatitude(), pMarker.getLongitude());
        if (p != null) 
        {
            pMarker.paint(pGraphics, p);
        }
    }

    /**
     * Moves the visible map pane.
     *
     * @param pMoveX horizontal movement in pixel.
     * @param pMoveY vertical movement in pixel
     */
    public void moveMap(int pMoveX, int pMoveY)
    {
        aCenter.x += pMoveX;
        aCenter.y += pMoveY;
        repaint();
        fireJMVEvent(new JMVCommandEvent(CommandType.MOVE, this));
    }

    /**
     * @return the current zoom level
     */
    public int getZoom() 
    {
        return aZoomLevel;
    }

    /**
     * Increases the current zoom level by one.
     */
    public void zoomIn() 
    {
        setZoom(aZoomLevel + 1);
    }

    /**
     * Increases the current zoom level by one.
     * @param pMapPoint point to choose as center for new zoom level
     */
    public void zoomIn(Point pMapPoint) 
    {
        setZoom(aZoomLevel + 1, pMapPoint);
    }

    /**
     * Decreases the current zoom level by one.
     */
    public void zoomOut() 
    {
        setZoom(aZoomLevel - 1);
    }

    /**
     * Decreases the current zoom level by one.
     *
     * @param pMapPoint point to choose as center for new zoom level
     */
    public void zoomOut(Point pMapPoint) 
    {
        setZoom(aZoomLevel - 1, pMapPoint);
    }

    /**
     * Set the zoom level and center point for display.
     *
     * @param pNewZoomLevel new zoom level
     * @param pNewCenter point to choose as center for new zoom level
     */
    public void setZoom(int pNewZoomLevel, Point pNewCenter) 
    {
        if (pNewZoomLevel > aTileController.getTileSource().getMaxZoom() || pNewZoomLevel < aTileController.getTileSource().getMinZoom() ||
                pNewZoomLevel == this.aZoomLevel)
        {
            return;
        }
        Coordinate zoomPos = getPosition(pNewCenter);
        JobDispatcher.getInstance().cancelOutstandingJobs();
        // requests
        setDisplayPositionByLatLon(pNewCenter, zoomPos.getLatitude(), zoomPos.getLongitude(), pNewZoomLevel);
        fireJMVEvent(new JMVCommandEvent(CommandType.ZOOM, this));
    }

    /**
     * Set the zoom level.
     *
     * @param pDesiredZoomLevel new zoom level
     */
    public void setZoom(int pDesiredZoomLevel) 
    {
        setZoom(pDesiredZoomLevel, new Point(getWidth() / 2, getHeight() / 2));
    }

    /**
     * Every time the zoom level changes this method is called. Override it in
     * derived implementations for adapting zoom dependent values. The new zoom
     * level can be obtained via {@link #getZoom()}.
     *
     * @param pOldZoomLevel the previous zoom level
     */
    private void zoomChanged(int pOldZoomLevel) 
    {
        aZoomSlider.setToolTipText(MESSAGES.getString("jmapviewer.tooltip.zlevel") + " " + aZoomLevel);
        aZoomInButton.setToolTipText(MESSAGES.getString("jmapviewer.tooltip.ztolevel") + " " + (aZoomLevel + 1));
        aZoomOutButton.setToolTipText(MESSAGES.getString("jmapviewer.tooltip.ztolevel") + " " + (aZoomLevel - 1));
        aZoomOutButton.setEnabled(aZoomLevel > aTileController.getTileSource().getMinZoom());
        aZoomInButton.setEnabled(aZoomLevel < aTileController.getTileSource().getMaxZoom());
    }

    /**
     * Enables or disables painting of the {@link MapMarker}.
     *
     * @param pMapMarkersVisible True if the map markers should be visible.
     * @see #addMapMarker(MapMarker)
     * @see #getMapMarkerList()
     */
    public void setMapMarkerVisible(boolean pMapMarkersVisible)
    {
        aMapMarkersVisible = pMapMarkersVisible;
        repaint();
    }

    /**
     * @return The list of map markers. TODO return a copy?
     */
    public List<MapMarker> getMapMarkerList()
    {
        return aMapMarkerList;
    }

    /**
     * Add a marker to the map.
     * @param pMarker The marker to add.
     */
    public void addMapMarker(MapMarker pMarker) 
    {
        aMapMarkerList.add(pMarker);
        repaint();
    }

    /**
     * Remomve a marker from the map.
     * @param pMarker The marker to remove.
     */
    public void removeMapMarker(MapMarker pMarker) 
    {
        aMapMarkerList.remove(pMarker);
        repaint();
    }

    /**
     * Remove all markers from the map.
     */
    public void removeAllMapMarkers() 
    {
        aMapMarkerList.clear();
        repaint();
    }

    /**
     * Sets whether the zoom controls are visible.
     * @param pVisible True if yes.
     */
    public void setZoomControlsVisible(boolean pVisible) 
    {
        aZoomSlider.setVisible(pVisible);
        aZoomInButton.setVisible(pVisible);
        aZoomOutButton.setVisible(pVisible);
    }

    /**
     * @return True if the zoom controls are visible.
     */
    public boolean getZoomControlsVisible()
    {
        return aZoomSlider.isVisible();
    }

    /**
     * Sets the tile source for this map viewer.
     * @param pTileSource The tile source.
     */
    public void setTileSource(TileSource pTileSource) 
    {
        if (pTileSource.getMaxZoom() > MAX_ZOOM)
        {
            throw new RuntimeException("Maximum zoom level too high");
        }
        if (pTileSource.getMinZoom() < MIN_ZOOM)
        {
            throw new RuntimeException("Minimum zoom level too low");
        }
        aTileSource = pTileSource;
        aTileController.setTileSource(pTileSource);
        aZoomSlider.setMinimum(pTileSource.getMinZoom());
        aZoomSlider.setMaximum(pTileSource.getMaxZoom());
        JobDispatcher.getInstance().cancelOutstandingJobs();
        if (aZoomLevel > pTileSource.getMaxZoom()) 
        {
            setZoom(pTileSource.getMaxZoom());
        }
        aAttributionSupport.initialize(pTileSource);
        repaint();
    }

    @Override
    public void tileLoadingFinished(Tile pTile, boolean pSuccess) 
    {
        repaint();
    }

    /**
     * Enables or disables painting of the map legend.
     *
     * @param pMapLegendVisible True if the map legend should be visible.
     */
    public void setMapLegendVisible(boolean pMapLegendVisible) 
    {
        aMapLegendVisible = pMapLegendVisible;
        repaint();
    }


    /**
     * @param pListener listener to set
     */
    public void addJMVListener(JMapViewerEventListener pListener) 
    {
        aListeners.add(JMapViewerEventListener.class, pListener);
    }

    /**
     * Send an update to all objects registered with viewer.
     *
     * @param event to dispatch
     */
    private void fireJMVEvent(JMVCommandEvent pEvent) 
    {
        Object[] listeners = aListeners.getListenerList();
        for (int i = 0; i < listeners.length; i+=2) 
        {
            if (listeners[i]==JMapViewerEventListener.class) 
            {
                ((JMapViewerEventListener)listeners[i+1]).processCommand(pEvent);
            }
        }
    }
}
