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
