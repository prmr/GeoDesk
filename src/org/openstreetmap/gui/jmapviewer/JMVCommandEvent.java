package org.openstreetmap.gui.jmapviewer;

//License: GPL.

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
