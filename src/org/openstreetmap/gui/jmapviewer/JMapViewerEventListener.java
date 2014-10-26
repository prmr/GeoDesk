package org.openstreetmap.gui.jmapviewer;

//License: GPL.

import java.util.EventListener;

/**
 * Must be implemented for processing commands while user
 * interacts with map viewer.
 * 
 * @author Jason Huntley
 */
public interface JMapViewerEventListener extends EventListener 
{
	/**
	 * Process the command (what else...).
	 * @param pCommand The command to process.
	 */
	void processCommand(JMVCommandEvent pCommand);
}
