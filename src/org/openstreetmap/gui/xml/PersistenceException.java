package org.openstreetmap.gui.xml;

/**
 * @author Martin P. Robillard
 * 
 * Represents any problem with the reading or writing of markers
 * to disk, or the copying of the markers file to backup storage.
 * CSOFF:
 */
@SuppressWarnings("serial")
public class PersistenceException extends Exception
{
	public PersistenceException(String pMessage, Throwable pCause)
	{
		super(pMessage, pCause);
	}

	public PersistenceException(String pMessage)
	{
		super(pMessage);
	}

	public PersistenceException(Throwable pCause)
	{
		super(pCause);
	}
}
