package org.openstreetmap.gui.app;

/**
 * Support for semantic versioning. A Version must have a 
 * major an minor number, but the patch is optional.
 * 
 * @author Martin P. Robillard
 */
public class Version
{
	private int aMajor = -1;
	private int aMinor = -1;
	private int aPatch = -1; 
	
	/**
	 * Create a new version with a patch number.
	 * @param pMajor The major version.
	 * @param pMinor The minor version.
	 * @param pPatch The patch version.
	 */
	public Version(int pMajor, int pMinor, int pPatch)
	{
		assert pMajor >= 0;
		assert pMinor >= 0;
		assert pPatch >= 0;
		assert pMajor + pMinor + pPatch > 0;
		aMajor = pMajor;
		aMinor = pMinor;
		aPatch = pPatch;
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
