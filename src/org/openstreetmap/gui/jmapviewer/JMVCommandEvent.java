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

import java.util.EventObject;

/**
 * Used for passing events between UI components and other
 * objects that register as a JMapViewerEventListener.
 * 
 * @author Jason Huntley
 *
 */
public class JMVCommandEvent extends EventObject 
{
    private static final long serialVersionUID = 8701544867914969620L;
	
    /**
     * The type of command.
     */
    public static enum CommandType 
    {
        MOVE,
        ZOOM
    }

    private CommandType aCommand;
    

    /**
     * Create a new command event with the type.
     * @param pCommandType The command type.
     * @param pSource The event source.
     */
    public JMVCommandEvent(CommandType pCommandType, Object pSource) 
    {
        super(pSource);
        setCommand(pCommandType);
    }

    /**
     * Create a new command event with no specified type.
     * @param pSource The event source.
     */
    public JMVCommandEvent(Object pSource) 
    {
        super(pSource);
    }

    /**
     * @return the command type
     */
    public CommandType getCommand() 
    {
        return aCommand;
    }

    /**
     * @param pCommandType the command to set
     */
    public void setCommand(CommandType pCommandType) 
    {
        aCommand = pCommandType;
    }
}
