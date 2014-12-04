package org.openstreetmap.gui.persistence;

/**
 * All the data associated with a marker.
 */
public class MarkerData
{
	String aName;
	double aLatitude;
	double aLongitude;
	String aDescription;
	
    /**
	 * @return the name of the location.
	 */
	public String getName()
	{
		return aName;
	}

	/**
	 * @return the latitude of the location.
	 */
	public double getLatitude()
	{
		return aLatitude;
	}

	/**
	 * @return the longitude of the location.
	 */
	public double getLongitude()
	{
		return aLongitude;
	}

	/**
	 * @return the description of the location.
	 */
	public String getDescription()
	{
		return aDescription;
	}
    
    @Override
    public String toString()
    {
        return aName + " (" + aLatitude + "," + aLongitude + "); " + aDescription;
    }
}