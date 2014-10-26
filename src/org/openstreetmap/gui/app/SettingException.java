package org.openstreetmap.gui.app;

/**
 * Represents any problem with obtaining or storing the user settings.
 * CSOFF:
 */
@SuppressWarnings("serial")
public class SettingException extends Exception 
{
    public SettingException() 
    {}

    public SettingException(String pMessage) 
    {
        super(pMessage);
    }

    public SettingException(Throwable pException)
    {
        super(pException);
    }

    public SettingException(String pMessage, Throwable pException) 
    {
        super(pMessage, pException);
    }
} // CSON:
