package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author Jan Peter Stotz
 *
 */
public class MapMarkerDot implements MapMarker {

    double lat;
    double lon;
    Color color;
    private String aDescription = "";
    private String aName = "";

    public MapMarkerDot(double lat, double lon, String pName, String pDescription) {
        this(Color.RED, lat, lon, pName, pDescription);
    }

    public MapMarkerDot(Color color, double lat, double lon, String pName, String pDescription) {
        super();
        this.color = color;
        this.lat = lat;
        this.lon = lon;
        aDescription = pDescription;
        aName = pName;
    }
    
    public String getName()
    {
        return aName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
    
    public void setName(String pName)
    {
        aName = pName;
    }
    
    public void setDescription(String pDescription)
    {
        aDescription = pDescription;
    }
    
    public void paint(Graphics g, Point position) {
        int size_h = 6;
        int size = size_h * 2;
        g.setColor(color);
        g.fillOval(position.x - size_h, position.y - size_h, size, size);
        g.setColor(Color.BLACK);
        g.drawOval(position.x - size_h, position.y - size_h, size, size);
    }

    @Override
    public String toString() {
        return "MapMarker at " + lat + " " + lon;
    }
    
    public int getRadius()
    {
        return 6;
    }
    
    public String getDescription()
    {
        return aDescription;
    }

}
