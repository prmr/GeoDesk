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

/**
 * This class implements the Mercator Projection as it is used by Openstreetmap
 * (and google). It provides methods to translate coordinates from 'map space'
 * into latitude and longitude (on the WGS84 ellipsoid) and vice versa. Map
 * space is measured in pixels. The origin of the map space is the top left
 * corner. The map space origin (0,0) has latitude ~85 and longitude -180
 *
 * @author Tim Haussmann
 * @author Jason Huntley
 * @author Jan Peter Stotz
 */
public final class OsmMercator 
{
	private static final long FULL_CIRCCLE_IN_DEGREES = 360L;
	private static final long HALF_CIRCLE_IN_DEGREES = 180L;
	private static final int TILE_SIZE = 256;
	private static final double MAX_LAT = 85.05112877980659;
	private static final double MIN_LAT = -85.05112877980659;
    private static final double EARTH_RADIUS = 6378137; // equatorial earth radius for EPSG:3857 (Mercator) 

    private OsmMercator()
    {}
    
    private static double radius(int pZoomLevel)
    {
        return (TILE_SIZE * (1 << pZoomLevel)) / (2.0 * Math.PI);
    }

    /**
     * Returns the absolute number of pixels in y or x, defined as: 2^Zoomlevel *
     * TILE_WIDTH where TILE_WIDTH is the width of a tile in pixels.
     *
     * @param pZoomlevel zoom level to request pixel data
     * @return number of pixels
     */
    public static int getMaxPixels(int pZoomlevel) 
    {
        return TILE_SIZE * (1 << pZoomlevel);
    }

    /**
     * I'm not sure what the point of this function is.
     * @param pZoomLevel The zoom level
     * @return Something useful, possibly.
     */
    public static int falseEasting(int pZoomLevel) 
    {
        return getMaxPixels(pZoomLevel) / 2;
    }

    /**
     * I'm not sure what the point of this function is.
     * @param pZoomLevel The zoom level
     * @return Something useful, possibly.
     */
    public static int falseNorthing(int pZoomLevel) 
    {
        return -1 * getMaxPixels(pZoomLevel) / 2;
    }

    /**
     * Transform pixel space to coordinates and get the distance.
     *
     * @param pX1 the first x coordinate
     * @param pY1 the first y coordinate
     * @param pX2 the second x coordinate
     * @param pY2 the second y coordinate
     * 
     * @param pZoomLevel the zoom level
     * @return the distance
     */
    public static double getDistance(int pX1, int pY1, int pX2, int pY2, int pZoomLevel) 
    {
        double la1 = yToLatitude(pY1, pZoomLevel);
        double lo1 = xToLongitude(pX1, pZoomLevel);
        double la2 = yToLatitude(pY2, pZoomLevel);
        double lo2 = xToLongitude(pX2, pZoomLevel);

        return getDistance(la1, lo1, la2, lo2);
    }

    /**
     * Gets the distance using Spherical law of cosines.
     *
     * @param pLatitude1 the Latitude in degrees
     * @param pLongitude1 the Longitude in degrees
     * @param pLatitude2 the Latitude from 2nd coordinate in degrees
     * @param pLongitude2 the Longitude from 2nd coordinate in degrees
     * @return the distance
     */
    public static double getDistance(double pLatitude1, double pLongitude1, double pLatitude2, double pLongitude2) 
    {
        double aStartLat = Math.toRadians(pLatitude1);
        double aStartLong = Math.toRadians(pLongitude1);
        double aEndLat = Math.toRadians(pLatitude2);
        double aEndLong = Math.toRadians(pLongitude2);
        double distance = Math.acos(Math.sin(aStartLat) * Math.sin(aEndLat)
                + Math.cos(aStartLat) * Math.cos(aEndLat) *
                Math.cos(aEndLong - aStartLong));

        return EARTH_RADIUS * distance;		
    }

    /**
     * Transform longitude to pixel space.
     *
     * <p>
     * Mathematical optimization<br>
     * <code>
     * x = radius(aZoomlevel) * toRadians(aLongitude) + falseEasting(aZoomLevel)<br>
     * x = getMaxPixels(aZoomlevel) / (2 * PI) * (aLongitude * PI) / 180 + getMaxPixels(aZoomlevel) / 2<br>
     * x = getMaxPixels(aZoomlevel) * aLongitude / 360 + 180 * getMaxPixels(aZoomlevel) / 360<br>
     * x = getMaxPixels(aZoomlevel) * (aLongitude + 180) / 360<br>
     * </code>
     * </p>
     *
     * @param pLongitude [-180..180]
     * @param pZoomLevel The zoom level
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    public static int longitudeToX(double pLongitude, int pZoomLevel)
    {
        int mp = getMaxPixels(pZoomLevel);
        int x = (int) ((mp * (pLongitude + HALF_CIRCLE_IN_DEGREES)) / FULL_CIRCCLE_IN_DEGREES);
        x = Math.min(x, mp - 1);
        return x;
    }

    /**
     * Transforms latitude to pixelspace.
     * <p>
     * Mathematical optimization<br>
     * <code>
     * log(u) := log((1.0 + sin(toRadians(aLat))) / (1.0 - sin(toRadians(aLat))<br>
     *
     * y = -1 * (radius(aZoomlevel) / 2 * log(u)))) - falseNorthing(aZoomlevel))<br>
     * y = -1 * (getMaxPixel(aZoomlevel) / 2 * PI / 2 * log(u)) - -1 * getMaxPixel(aZoomLevel) / 2<br>
     * y = getMaxPixel(aZoomlevel) / (-4 * PI) * log(u)) + getMaxPixel(aZoomLevel) / 2<br>
     * y = getMaxPixel(aZoomlevel) * ((log(u) / (-4 * PI)) + 1/2)<br>
     * </code>
     * </p>
     * @param pLatitude [-90...90]
     * @param pZoomLevel The zoom level.
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    public static int latitudeToY(double pLatitude, int pZoomLevel) 
    {
    	double latitude = pLatitude;
        if(pLatitude < MIN_LAT)
        {
        	latitude = MIN_LAT;
        }
        else if(pLatitude > MAX_LAT)
        {
        	latitude = MAX_LAT;
        }
        double sinLat = Math.sin(Math.toRadians(latitude));
        double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
        int mp = getMaxPixels(pZoomLevel);
        // CSOFF:
        int y = (int) (mp * (0.5 - (log / (4.0 * Math.PI))));
        y = Math.min(y, mp - 1);
        // CSON:
        return y;
    }

    /**
     * Transforms pixel coordinate X to longitude.
     *
     * <p>
     * Mathematical optimization<br>
     * <code>
     * lon = toDegree((aX - falseEasting(aZoomlevel)) / radius(aZoomlevel))<br>
     * lon = 180 / PI * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel) / (2 * PI)<br>
     * lon = 180 * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel))<br>
     * lon = 360 / getMaxPixels(aZoomlevel) * (aX - getMaxPixels(aZoomlevel) / 2)<br>
     * lon = 360 * aX / getMaxPixels(aZoomlevel) - 180<br>
     * </code>
     * </p>
     * @param pX [0..2^Zoomlevel*TILE_WIDTH[
     * @param pZoomLevel The zoom level.
     * @return ]-180..180[
     * CSOFF:
     */
    public static double xToLongitude(int pX, int pZoomLevel) 
    {
        return ((360d * pX) / getMaxPixels(pZoomLevel)) - 180.0; //CSON:
    }

    /**
     * Transforms pixel coordinate Y to latitude.
     *
     * @param pY [0..2^Zoomlevel*TILE_WIDTH[
     * @param pZoomLevel the zoom level
     * @return [MIN_LAT..MAX_LAT] is about [-85..85]
     */
    public static double yToLatitude(int pY, int pZoomLevel)
    {
    	int ycoordinate = pY;
    	ycoordinate += falseNorthing(pZoomLevel);
        double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * ycoordinate / radius(pZoomLevel))));
        return -1 * Math.toDegrees(latitude);
    }

}
