package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
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

import org.openstreetmap.gui.jmapviewer.JMVCommandEvent.CommandType;
import org.openstreetmap.gui.jmapviewer.tiles.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.tiles.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.tiles.Tile;
import org.openstreetmap.gui.jmapviewer.tiles.TileCache;
import org.openstreetmap.gui.jmapviewer.tiles.TileController;
import org.openstreetmap.gui.jmapviewer.tiles.TileLoader;
import org.openstreetmap.gui.jmapviewer.tiles.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.tilesources.MapnikOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

/**
 * Provides a simple panel that displays pre-rendered map tiles.
 *
 * @author Jan Peter Stotz
 * @author Martin P. Robillard - On-map legend
 *
 */
public class JMapViewer extends JPanel implements TileLoaderListener 
{
	public static final int MAX_ZOOM = 22;
    public static final int MIN_ZOOM = 0;
	
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
        setPreferredSize(new Dimension(400, 400));
        setDisplayPositionByLatLon(50, 9, 3);
        //setToolTipText("");
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        //        Point screenPoint = event.getLocationOnScreen();
        //        Coordinate c = getPosition(screenPoint);
        return super.getToolTipText(event);
    }
    
    public MapMarker[] getMapMarkersAt(Point pPoint)
    {
        List<MapMarker> lMarkers = new ArrayList<MapMarker>();
        for( MapMarker marker : getMapMarkerList())
        {
            Point lMarkerPoint = getMapPosition(marker.getLatitude(),marker.getLongitude());
            if( lMarkerPoint != null && 
                pPoint.x > lMarkerPoint.x-marker.getRadius() && 
                pPoint.x < lMarkerPoint.x+marker.getRadius() &&
                pPoint.y > lMarkerPoint.y-marker.getRadius() &&
                pPoint.y < lMarkerPoint.y+marker.getRadius())
            {
                lMarkers.add(marker);
            }
        }
        
        return lMarkers.toArray(new MapMarker[lMarkers.size()]);
    }

    protected void initializeZoomSlider() {
        aZoomSlider = new JSlider(MIN_ZOOM, aTileController.getTileSource().getMaxZoom());
        aZoomSlider.setOrientation(JSlider.VERTICAL);
        aZoomSlider.setBounds(10, 10, 30, 150);
        aZoomSlider.setOpaque(false);
        aZoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setZoom(aZoomSlider.getValue());
            }
        });
        add(aZoomSlider);
        int size = 18;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/plus.png"));
            aZoomInButton = new JButton(icon);
        } catch (Exception e) {
            aZoomInButton = new JButton("+");
            aZoomInButton.setFont(new Font("sansserif", Font.BOLD, 9));
            aZoomInButton.setMargin(new Insets(0, 0, 0, 0));
        }
        aZoomInButton.setBounds(4, 155, size, size);
        aZoomInButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });
        add(aZoomInButton);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/minus.png"));
            aZoomOutButton = new JButton(icon);
        } catch (Exception e) {
            aZoomOutButton = new JButton("-");
            aZoomOutButton.setFont(new Font("sansserif", Font.BOLD, 9));
            aZoomOutButton.setMargin(new Insets(0, 0, 0, 0));
        }
        aZoomOutButton.setBounds(8 + size, 155, size, size);
        aZoomOutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
        add(aZoomOutButton);
    }

    /**
     * Changes the map pane so that it is centered on the specified coordinate
     * at the given zoom level.
     *
     * @param lat
     *            latitude of the specified coordinate
     * @param lon
     *            longitude of the specified coordinate
     * @param zoom
     *            {@link #MIN_ZOOM} <= zoom level <= {@link #MAX_ZOOM}
     */
    public void setDisplayPositionByLatLon(double lat, double lon, int zoom) {
        setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), lat, lon, zoom);
    }

    /**
     * Changes the map pane so that the specified coordinate at the given zoom
     * level is displayed on the map at the screen coordinate
     * <code>mapPoint</code>.
     *
     * @param mapPoint
     *            point on the map denoted in pixels where the coordinate should
     *            be set
     * @param lat
     *            latitude of the specified coordinate
     * @param lon
     *            longitude of the specified coordinate
     * @param zoom
     *            {@link #MIN_ZOOM} <= zoom level <=
     *            {@link TileSource#getMaxZoom()}
     */
    public void setDisplayPositionByLatLon(Point mapPoint, double lat, double lon, int zoom) {
        int x = OsmMercator.longitudeToX(lon, zoom);
        int y = OsmMercator.latitudeToY(lat, zoom);
        setDisplayPosition(mapPoint, x, y, zoom);
    }

    public void setDisplayPosition(int x, int y, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), x, y, zoom);
    }

    public void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
        if (zoom > aTileController.getTileSource().getMaxZoom() || zoom < MIN_ZOOM)
            return;

        // Get the plain tile number
        Point p = new Point();
        p.x = x - mapPoint.x + getWidth() / 2;
        p.y = y - mapPoint.y + getHeight() / 2;
        aCenter = p;
        setIgnoreRepaint(true);
        try {
            int oldZoom = this.aZoomLevel;
            this.aZoomLevel = zoom;
            if (oldZoom != zoom) {
                zoomChanged(oldZoom);
            }
            if (aZoomSlider.getValue() != zoom) {
                aZoomSlider.setValue(zoom);
            }
        } finally {
            setIgnoreRepaint(false);
            repaint();
        }
    }

    /**
     * Sets the displayed map pane and zoom level so that all chosen map elements are
     * visible.
     */
    public void setDisplayToFitMapElements(boolean markers, boolean rectangles, boolean polygons) 
    {
        int nbElemToCheck = 0;
        if (markers && aMapMarkerList != null)
            nbElemToCheck += aMapMarkerList.size();
        if (nbElemToCheck == 0)
            return;

        int x_min = Integer.MAX_VALUE;
        int y_min = Integer.MAX_VALUE;
        int x_max = Integer.MIN_VALUE;
        int y_max = Integer.MIN_VALUE;
        int mapZoomMax = aTileController.getTileSource().getMaxZoom();

        if (markers) {
            for (MapMarker marker : aMapMarkerList) {
                int x = OsmMercator.longitudeToX(marker.getLongitude(), mapZoomMax);
                int y = OsmMercator.latitudeToY(marker.getLatitude(), mapZoomMax);
                x_max = Math.max(x_max, x);
                y_max = Math.max(y_max, y);
                x_min = Math.min(x_min, x);
                y_min = Math.min(y_min, y);
            }
        }

        int height = Math.max(0, getHeight());
        int width = Math.max(0, getWidth());
        int newZoom = mapZoomMax;
        int x = x_max - x_min;
        int y = y_max - y_min;
        while (x > width || y > height) {
            newZoom--;
            x >>= 1;
            y >>= 1;
        }
        x = x_min + (x_max - x_min) / 2;
        y = y_min + (y_max - y_min) / 2;
        int z = 1 << (mapZoomMax - newZoom);
        x /= z;
        y /= z;
        setDisplayPosition(x, y, newZoom);
    }


    /**
     * Sets the displayed map pane and zoom level so that all map markers are
     * visible.
     */
    public void setDisplayToFitMapMarkers() {
        setDisplayToFitMapElements(true, false, false);
    }

    /**
     * Sets the displayed map pane and zoom level so that all map rectangles are
     * visible.
     */
    public void setDisplayToFitMapRectangles() {
        setDisplayToFitMapElements(false, true, false);
    }

    /**
     * Calculates the latitude/longitude coordinate of the center of the
     * currently displayed map area.
     *
     * @return latitude / longitude
     */
    public Coordinate getPosition() {
        double lon = OsmMercator.xToLongitude(aCenter.x, aZoomLevel);
        double lat = OsmMercator.yToLatitude(aCenter.y, aZoomLevel);
        return new Coordinate(lat, lon);
    }

    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint
     *            relative pixel coordinate regarding the top left corner of the
     *            displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point mapPoint) {
        return getPosition(mapPoint.x, mapPoint.y);
    }

    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPointX
     * @param mapPointY
     * @return latitude / longitude
     */
    public Coordinate getPosition(int mapPointX, int mapPointY) {
        int x = aCenter.x + mapPointX - getWidth() / 2;
        int y = aCenter.y + mapPointY - getHeight() / 2;
        double lon = OsmMercator.xToLongitude(x, aZoomLevel);
        double lat = OsmMercator.yToLatitude(y, aZoomLevel);
        return new Coordinate(lat, lon);
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @param lat
     * @param lon
     * @param checkOutside
     * @return point on the map or <code>null</code> if the point is not visible
     *         and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(double lat, double lon, boolean checkOutside) {
        int x = OsmMercator.longitudeToX(lon, aZoomLevel);
        int y = OsmMercator.latitudeToY(lat, aZoomLevel);
        x -= aCenter.x - getWidth() / 2;
        y -= aCenter.y - getHeight() / 2;
        if (checkOutside) {
            if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
                return null;
        }
        return new Point(x, y);
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @param lat
     * @param lon
     * @return point on the map or <code>null</code> if the point is not visible
     */
    public Point getMapPosition(double lat, double lon) {
        return getMapPosition(lat, lon, true);
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @param coord
     * @return point on the map or <code>null</code> if the point is not visible
     */
    public Point getMapPosition(Coordinate coord) {
        if (coord != null)
            return getMapPosition(coord.getLatitude(), coord.getLongitude());
        else
            return null;
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @param coord
     * @return point on the map or <code>null</code> if the point is not visible
     *         and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(Coordinate coord, boolean checkOutside) {
        if (coord != null)
            return getMapPosition(coord.getLatitude(), coord.getLongitude(), checkOutside);
        else
            return null;
    }

    /**
     * Gets the meter per pixel.
     *
     * @return the meter per pixel
     * @author Jason Huntley
     */
    public double getMeterPerPixel() {
        Point origin=new Point(5,5);
        Point center=new Point(getWidth()/2, getHeight()/2);

        double pDistance=center.distance(origin);

        Coordinate originCoord=getPosition(origin);
        Coordinate centerCoord=getPosition(center);

        double mDistance=OsmMercator.getDistance(originCoord.getLatitude(), originCoord.getLongitude(),
                centerCoord.getLatitude(), centerCoord.getLongitude());

        return mDistance/pDistance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int iMove = 0;

        int tilesize = aTileSource.getTileSize();
        int tilex = aCenter.x / tilesize;
        int tiley = aCenter.y / tilesize;
        int off_x = (aCenter.x % tilesize);
        int off_y = (aCenter.y % tilesize);

        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        int posx = w2 - off_x;
        int posy = h2 - off_y;

        int diff_left = off_x;
        int diff_right = tilesize - off_x;
        int diff_top = off_y;
        int diff_bottom = tilesize - off_y;

        boolean start_left = diff_left < diff_right;
        boolean start_top = diff_top < diff_bottom;

        if (start_top) {
            if (start_left) {
                iMove = 2;
            } else {
                iMove = 3;
            }
        } else {
            if (start_left) {
                iMove = 1;
            } else {
                iMove = 0;
            }
        } // calculate the visibility borders
        int x_min = -tilesize;
        int y_min = -tilesize;
        int x_max = getWidth();
        int y_max = getHeight();

        // paint the tiles in a spiral, starting from center of the map
        boolean painted = true;
        int x = 0;
        while (painted) {
            painted = false;
            for (int i = 0; i < 4; i++) {
                if (i % 2 == 0) {
                    x++;
                }
                for (int j = 0; j < x; j++) {
                    if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) {
                        // tile is visible
                        Tile tile = aTileController.getTile(tilex, tiley, aZoomLevel);
                        if (tile != null) {
                            painted = true;
                            tile.paint(g, posx, posy);
                            if (aTileGridVisible) {
                                g.drawRect(posx, posy, tilesize, tilesize);
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
        g.drawRect(w2 - aCenter.x, h2 - aCenter.y, mapSize, mapSize);

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        if (aMapMarkersVisible && aMapMarkerList != null) {
            for (MapMarker marker : aMapMarkerList) {
                paintMarker(g, marker);
            }
        }
        
        if (aMapLegendVisible )
        {
            paintLegend(g);
        }
        
        // Paint legend
        

        aAttributionSupport.paintAttribution(g, getWidth(), getHeight(), getPosition(0, 0), getPosition(getWidth(), getHeight()), aZoomLevel, this);
    }
    
    
    /**
     * Paint the legend.
     * @param g
     */
    protected void paintLegend(Graphics g)
    {
        Graphics2D lGraphics = (Graphics2D)g;
        lGraphics.setStroke(new BasicStroke(3));
        lGraphics.drawLine(LEGEND_OFFSET, getHeight()-LEGEND_OFFSET, 100 + LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.drawLine(LEGEND_OFFSET, getHeight()-LEGEND_OFFSET+5, LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.drawLine(100+LEGEND_OFFSET, getHeight()-LEGEND_OFFSET+5, 100+LEGEND_OFFSET, getHeight()-LEGEND_OFFSET);
        lGraphics.setFont(lGraphics.getFont().deriveFont(11f)); 
        long lMper100Pix = Math.round(getMeterPerPixel()*100);
        if( lMper100Pix < 1000 )
        {
            lGraphics.drawString(String.format("%dm",lMper100Pix), LEGEND_OFFSET+5, getHeight()-LEGEND_OFFSET+15);
        }
        else
        {
            double lKMper100Pix = getMeterPerPixel()/10;
            lGraphics.drawString(String.format("%dkm",Math.round(lKMper100Pix)), LEGEND_OFFSET+5, getHeight()-LEGEND_OFFSET+15);
        }
    }

    /**
     * Paint a single marker.
     */
    protected void paintMarker(Graphics g, MapMarker marker) {
        Point p = getMapPosition(marker.getLatitude(), marker.getLongitude());
        if (p != null) {
            marker.paint(g, p);
        }
    }

    /**
     * Moves the visible map pane.
     *
     * @param x
     *            horizontal movement in pixel.
     * @param y
     *            vertical movement in pixel
     */
    public void moveMap(int x, int y) {
        aCenter.x += x;
        aCenter.y += y;
        repaint();
        this.fireJMVEvent(new JMVCommandEvent(CommandType.MOVE, this));
    }

    /**
     * @return the current zoom level
     */
    public int getZoom() {
        return aZoomLevel;
    }

    /**
     * Increases the current zoom level by one
     */
    public void zoomIn() {
        setZoom(aZoomLevel + 1);
    }

    /**
     * Increases the current zoom level by one
     */
    public void zoomIn(Point mapPoint) {
        setZoom(aZoomLevel + 1, mapPoint);
    }

    /**
     * Decreases the current zoom level by one
     */
    public void zoomOut() {
        setZoom(aZoomLevel - 1);
    }

    /**
     * Decreases the current zoom level by one
     *
     * @param mapPoint point to choose as center for new zoom level
     */
    public void zoomOut(Point mapPoint) {
        setZoom(aZoomLevel - 1, mapPoint);
    }

    /**
     * Set the zoom level and center point for display
     *
     * @param zoom new zoom level
     * @param mapPoint point to choose as center for new zoom level
     */
    public void setZoom(int zoom, Point mapPoint) {
        if (zoom > aTileController.getTileSource().getMaxZoom() || zoom < aTileController.getTileSource().getMinZoom()
                || zoom == this.aZoomLevel)
            return;
        Coordinate zoomPos = getPosition(mapPoint);
        JobDispatcher.getInstance().cancelOutstandingJobs();
        // requests
        setDisplayPositionByLatLon(mapPoint, zoomPos.getLatitude(), zoomPos.getLongitude(), zoom);

        this.fireJMVEvent(new JMVCommandEvent(CommandType.ZOOM, this));
    }

    /**
     * Set the zoom level
     *
     * @param zoom new zoom level
     */
    public void setZoom(int zoom) {
        setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
    }

    /**
     * Every time the zoom level changes this method is called. Override it in
     * derived implementations for adapting zoom dependent values. The new zoom
     * level can be obtained via {@link #getZoom()}.
     *
     * @param oldZoom
     *            the previous zoom level
     */
    protected void zoomChanged(int oldZoom) {
        aZoomSlider.setToolTipText("Zoom level " + aZoomLevel);
        aZoomInButton.setToolTipText("Zoom to level " + (aZoomLevel + 1));
        aZoomOutButton.setToolTipText("Zoom to level " + (aZoomLevel - 1));
        aZoomOutButton.setEnabled(aZoomLevel > aTileController.getTileSource().getMinZoom());
        aZoomInButton.setEnabled(aZoomLevel < aTileController.getTileSource().getMaxZoom());
    }

    public boolean isTileGridVisible() {
        return aTileGridVisible;
    }

    public void setTileGridVisible(boolean tileGridVisible) {
        this.aTileGridVisible = tileGridVisible;
        repaint();
    }

    public boolean getMapMarkersVisible() {
        return aMapMarkersVisible;
    }

    /**
     * Enables or disables painting of the {@link MapMarker}
     *
     * @param mapMarkersVisible
     * @see #addMapMarker(MapMarker)
     * @see #getMapMarkerList()
     */
    public void setMapMarkerVisible(boolean mapMarkersVisible) {
        this.aMapMarkersVisible = mapMarkersVisible;
        repaint();
    }

    public void setMapMarkerList(List<MapMarker> mapMarkerList) {
        this.aMapMarkerList = mapMarkerList;
        repaint();
    }

    public List<MapMarker> getMapMarkerList() {
        return aMapMarkerList;
    }

    public void addMapMarker(MapMarker marker) {
        aMapMarkerList.add(marker);
        repaint();
    }

    public void removeMapMarker(MapMarker marker) {
        aMapMarkerList.remove(marker);
        repaint();
    }

    public void removeAllMapMarkers() {
        aMapMarkerList.clear();
        repaint();
    }

    public void setZoomContolsVisible(boolean visible) {
        aZoomSlider.setVisible(visible);
        aZoomInButton.setVisible(visible);
        aZoomOutButton.setVisible(visible);
    }

    public boolean getZoomContolsVisible() {
        return aZoomSlider.isVisible();
    }

    public void setTileSource(TileSource tileSource) {
        if (tileSource.getMaxZoom() > MAX_ZOOM)
            throw new RuntimeException("Maximum zoom level too high");
        if (tileSource.getMinZoom() < MIN_ZOOM)
            throw new RuntimeException("Minumim zoom level too low");
        this.aTileSource = tileSource;
        aTileController.setTileSource(tileSource);
        aZoomSlider.setMinimum(tileSource.getMinZoom());
        aZoomSlider.setMaximum(tileSource.getMaxZoom());
        JobDispatcher.getInstance().cancelOutstandingJobs();
        if (aZoomLevel > tileSource.getMaxZoom()) {
            setZoom(tileSource.getMaxZoom());
        }

        aAttributionSupport.initialize(tileSource);
        repaint();
    }

    public void tileLoadingFinished(Tile tile, boolean success) {
        repaint();
    }

    
    

    /**
     * Enables or disables painting of the map legend.
     *
     * @param mapLegendVisible
     */
    public void setMapLegendVisible(boolean mapLegendVisible) {
        this.aMapLegendVisible = mapLegendVisible;
        repaint();
    }

    public void setTileLoader(TileLoader loader) {
        aTileController.setTileLoader(loader);
    }

    protected EventListenerList listenerList = new EventListenerList();

    /**
     * @param listener listener to set
     */
    public void addJMVListener(JMapViewerEventListener listener) {
        listenerList.add(JMapViewerEventListener.class, listener);
    }

    /**
     * @param listener listener to remove
     */
    public void removeJMVListener(JMapViewerEventListener listener) {
        listenerList.remove(JMapViewerEventListener.class, listener);
    }

    /**
     * Send an update to all objects registered with viewer
     *
     * @param event to dispatch
     */
    void fireJMVEvent(JMVCommandEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==JMapViewerEventListener.class) {
                ((JMapViewerEventListener)listeners[i+1]).processCommand(evt);
            }
        }
    }
}
