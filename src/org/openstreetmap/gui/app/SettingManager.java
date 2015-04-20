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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton that manages and persists the user settings.
 * The <code>initialize()</code> method should be called before
 * any other method call on the setting manager. This design is to limit
 * the amount of error handling that must be done whenever the SettingManager
 * is accessed.
 * 
 * @author Martin P. Robillard
 */
public final class SettingManager 
{
    private static final String P_DEFAULT_APP_DATA_FILE = "defaultAppDataFile";
    private static final String P_APP_DATA_FILE = "appDataFile";
    private static final String PROPERTIES_FILE = ".geodesk";
    private static final String DEFAULT_DATA_FILE = "geodesk-data.json";
    private static final String ENV_VAR_USER_PROFILE = "USERPROFILE";
    private static final String ENV_VAR_APP_DATA = "APPDATA";
    
    private static final SettingManager INSTANCE = new SettingManager();
    
    private final Properties aProperties = new Properties();
    private File aPropertiesFile = null;
    
    private SettingManager()
    {}
    
    /**
     * @return The singleton SettingManager.
     */
    public static SettingManager getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * Initializes the user settings.
     * @throws SettingException If anything goes wrong really.
     */
    public void initialize() throws SettingException
    {
        String lUserProfile = System.getenv(ENV_VAR_USER_PROFILE);
        if( lUserProfile == null )
        {
            throw new SettingException("No " + ENV_VAR_USER_PROFILE + " environment variable");
        }
        else
        {
            File lUPFile = new File(lUserProfile);
            if( !lUPFile.exists() || !lUPFile.isDirectory())
            {
                throw new SettingException("User profile is not an existing directory.\n" + 
                        ENV_VAR_USER_PROFILE + "=" + lUserProfile);
            }
            else if( !lUPFile.canWrite() )
            {
                throw new SettingException("User profile is not writable.\n" + 
                        ENV_VAR_USER_PROFILE + "=" + lUserProfile);
            }
        }
        
        // Attempt to load the properties from the properties file.
        aPropertiesFile = new File(lUserProfile + File.separator + PROPERTIES_FILE);
        
        try
        {
            aPropertiesFile.createNewFile();
        }
        catch( IOException e )
        {
            throw new SettingException("Cannot create new properties file at " + aPropertiesFile.getAbsolutePath(), e);
        }
        
        FileReader lReader = null;
        try
        {
            lReader = new FileReader(aPropertiesFile);
            aProperties.load(lReader);
        }
        catch( IOException e )
        {
            throw new SettingException("Cannot load the properties file: " + aPropertiesFile.getAbsolutePath(), e);
        }
        finally
        {
            if( lReader != null )
            {
                try
                {
                    lReader.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        
        // Generate default data file name
        String lAppData = System.getenv(ENV_VAR_APP_DATA);
        String lAppDataFile = "";
        if( lAppData == null )
        {
            // Store in the user profile, which should be writable if we got here.
            lAppDataFile = lUserProfile + File.separator + DEFAULT_DATA_FILE;
        }
        else
        {
            File lADFile = new File(lAppData);
            if( lADFile.exists() && lADFile.isDirectory() && lADFile.canWrite())
            {
                lAppDataFile = lAppData + File.separator + DEFAULT_DATA_FILE;
            }
            else
            {
                lAppDataFile = lUserProfile + File.separator + DEFAULT_DATA_FILE;
            }
        }
        aProperties.setProperty(P_DEFAULT_APP_DATA_FILE, lAppDataFile);
    }
    
    private void storeProperties()
    {
        FileWriter lWriter = null;
        try
        {
            lWriter = new FileWriter(aPropertiesFile);
            aProperties.store(lWriter, "");
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if( lWriter != null )
            {
                try
                {
                    lWriter.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * @return The name of the data file.
     */
    public String getDataFileName()
    {
        return aProperties.getProperty(P_APP_DATA_FILE);
    }
    
    /**
     * @param pFileName The new name of the data file.
     */
    public void setDataFileName(String pFileName)
    {
        aProperties.setProperty(P_APP_DATA_FILE, pFileName);
        storeProperties();
    }
    
    /**
     * @return The default name of the data file.
     */
    public String getDefaultDataFileName()
    {
        return aProperties.getProperty(P_DEFAULT_APP_DATA_FILE);
    }
}
