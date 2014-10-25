package org.openstreetmap.gui.jmapviewer;

//License: GPL.

import java.util.EventListener;

/**
 * Must be implemented for processing commands while user
 * interacts with map viewer.
 * 
 * @author Jason Huntley
 *
 */
public interface JMapViewerEventListener extends EventListener {
	public void processCommand(JMVCommandEvent command);
}
