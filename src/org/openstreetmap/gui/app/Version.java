package org.openstreetmap.gui.app;

/**
 * Support for semantic versioning. A Version must have a 
 * major an minor number, but the patch is optional. The version
 * is a singleton.
 * 
 * @author Martin P. Robillard
 */
public final class Version
{
	private static final Version VERSION = new Version(0, 2, 0);
	
	private int aMajor = -1;
	private int aMinor = -1;
	private int aPatch = -1; 
	
	/**
	 * Create a new version with a patch number.
	 * @param pMajor The major version.
	 * @param pMinor The minor version.
	 * @param pPatch The patch version.
	 */
	private Version(int pMajor, int pMinor, int pPatch)
	{
		assert pMajor >= 0;
		assert pMinor >= 0;
		assert pPatch >= 0;
		assert pMajor + pMinor + pPatch > 0;
		aMajor = pMajor;
		aMinor = pMinor;
		aPatch = pPatch;
	}
	
	/**
	 * @return The global version instance.
	 */
	public static Version instance()
	{
		return VERSION;
	}
	
	@Override
	public String toString()
	{
		StringBuffer lReturn = new StringBuffer();
		lReturn.append(aMajor + "." + aMinor);
		if( aPatch > 0 )
		{
			lReturn.append("." + aPatch);
		}
		return lReturn.toString();
	}
}
