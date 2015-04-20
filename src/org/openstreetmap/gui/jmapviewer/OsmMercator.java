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
	private static final long FULL_CIRCLE_IN_DEGREES = 360L;
	private static final long HALF_CIRCLE_IN_DEGREES = 180L;
	private static final double FULL_CIRCLE_IN_DEGREES_D = 360d;
	private static final double HALF_CIRCLE_IN_DEGREES_D = 180d;
	private static final int TILE_SIZE = 256;
	private static final double MAX_LAT = 85.05112877980659;
	private static final double MIN_LAT = -85.05112877980659;
	private static final double EARTH_RADIUS = 6371000; // An approximation

    private OsmMercator()
    {}
    
    private static double radius(int pZoomLevel)
    {
        return (TILE_SIZE * (1 << pZoomLevel)) / (2.0 * Math.PI);
    }

    /**
     * Returns the absolute number of map pixels in either y or x.
     * This number is computed as: 2^Zoomlevel * TILE_WIDTH 
     * where TILE_SIZE is the width and/or height of a tile in pixels.
     * // 0 -> 256, 1 -> 512, etc.
     *
     * @param pZoomlevel zoom level to request pixel data
     * @return number of pixels
     * @pre pZoomlevel >= 0
     */
    private static int getMaxPixels(int pZoomlevel) 
    {
    	assert pZoomlevel >= 0;
        return TILE_SIZE * (1 << pZoomlevel);
    }

    /**
     * @param pZoomLevel The zoom level
     * @return A negative value of half the tile width or height.
     */
    public static int falseNorthing(int pZoomLevel) 
    {
        return -1 * getMaxPixels(pZoomLevel) / 2;
    }

    /**
     * Gets the distance using the Haversine Formula.
     *
     * @param pPoint1 The first coordinate
	 * @param pPoint2 The second coordinate
     * @return The distance in meters
     * @pre pPoint1 != null
     * @pre pPoint2 != null
     */
    public static double getDistance(Coordinate pPoint1, Coordinate pPoint2 ) 
    {
    	assert pPoint1 != null;
    	assert pPoint2 != null;
    	double startlat = Math.toRadians(pPoint1.getLatitude());
        double startlong = Math.toRadians(pPoint1.getLongitude());
        double endlat = Math.toRadians(pPoint2.getLatitude());
        double endlon = Math.toRadians(pPoint2.getLongitude());
    	
        double deltaLongitude = endlon - startlong;
        double deltaLatitude = endlat - startlat;
        double component = Math.sin(deltaLatitude/2)*Math.sin(deltaLatitude/2) + 
        		Math.cos(startlat) * Math.cos(endlat) * Math.sin(deltaLongitude/2)*Math.sin(deltaLongitude/2);
        double component2 = 2*Math.asin(Math.min(1, Math.sqrt(component)));
        return component2 * EARTH_RADIUS;
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
    	assert pLongitude >= -HALF_CIRCLE_IN_DEGREES && pLongitude <= HALF_CIRCLE_IN_DEGREES;
    	assert pZoomLevel >= 0;
        int mp = getMaxPixels(pZoomLevel);
        int x = (int) ((mp * (pLongitude + HALF_CIRCLE_IN_DEGREES)) / FULL_CIRCLE_IN_DEGREES);
        x = Math.min(x, mp - 1);
        return x;
    }

    /**
     * Transforms latitude to pixel space.
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
     * @param pLatitude [MIN_LAT...MAX_LAT]
     * @param pZoomLevel The zoom level.
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    public static int latitudeToY(double pLatitude, int pZoomLevel) 
    {
    	assert pLatitude >= MIN_LAT && pLatitude <= MAX_LAT : String.format("lat=%f zoomLevel=%d", pLatitude, pZoomLevel);
    	double sinLat = Math.sin(Math.toRadians(pLatitude));
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
     * Mathematical derivation
     *
     * lon = toDegree((aX - falseEasting(aZoomlevel)) / radius(aZoomlevel))
     * lon = 180 / PI * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel) / (2 * PI)
     * lon = 180 * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel))
     * lon = 360 / getMaxPixels(aZoomlevel) * (aX - getMaxPixels(aZoomlevel) / 2)
     * lon = 360 * aX / getMaxPixels(aZoomlevel) - 180
	 *
     * @param pX [0..2^Zoomlevel*TILE_SIZE[
     * @param pZoomLevel The zoom level.
     * @return ]-180..180[
     */
    public static double xToLongitude(int pX, int pZoomLevel) 
    {
    	assert pX >= 0 && pX < powerOfTwo(pZoomLevel)*TILE_SIZE: String.format("x=%d zoomLevel=%d", pX, pZoomLevel);
    	return ((FULL_CIRCLE_IN_DEGREES_D * pX) / getMaxPixels(pZoomLevel)) - HALF_CIRCLE_IN_DEGREES_D; 
    }
    
    /**
     * @param pPixel A pixel coordinate to check for validity.
     * @param pZoomLevel the zoom level.
     * @return If pX is a pixel in the map.
     */
    public static boolean inMap( int pPixel, int pZoomLevel )
    {
    	assert pZoomLevel >= 0;
    	return pPixel >= 0 && pPixel < powerOfTwo(pZoomLevel)*TILE_SIZE;
    }
    
    /**
     * @param pPower The power of two. >= 0
     * @return 2 to the power of pPower.
     */
    private static long powerOfTwo(int pPower)
    {
    	assert pPower >= 0;
    	return 1 << pPower;
    }

    /**
     * Transforms pixel coordinate Y to latitude.
     *
     * @param pY [0..2^Zoomlevel*TILE_SIZE[
     * @param pZoomLevel the zoom level
     * @return [MIN_LAT..MAX_LAT] is about [-85..85]
     */
    public static double yToLatitude(int pY, int pZoomLevel)
    {
    	assert pY >= 0 && pY < powerOfTwo(pZoomLevel)*TILE_SIZE : String.format("y=%d zoomLevel=%d", pY, pZoomLevel);
    	int ycoordinate = pY;
    	ycoordinate += falseNorthing(pZoomLevel);
        double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * ycoordinate / radius(pZoomLevel))));
        return -1 * Math.toDegrees(latitude);
    }

}
