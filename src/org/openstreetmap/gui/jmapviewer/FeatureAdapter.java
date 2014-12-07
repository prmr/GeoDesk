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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Retire this class with Release 0.2. Most of this code is part
 * of an incomplete development to the feature to view terms of use.
 */
public final class FeatureAdapter 
{
	private static BrowserAdapter browserAdapter = new DefaultBrowserAdapter();
    private static TranslationAdapter translationAdapter = new DefaultTranslationAdapter();
	
    private FeatureAdapter()
    {}
    
    private interface BrowserAdapter
    {
        void openLink(String pUrl);
    }

    private interface TranslationAdapter 
    {
        String tr(String pText, Object... pObjects);
        // TODO: more i18n functions
    }

//    public static void registerBrowserAdapter(BrowserAdapter pBrowserAdapter) 
//    {
//        FeatureAdapter.browserAdapter = pBrowserAdapter;
//    }

    // Private because not used.
//    private static void registerTranslationAdapter(TranslationAdapter translationAdapter) 
//    {
//        FeatureAdapter.translationAdapter = translationAdapter;
//    }

    /**
     * Open a link in a browser.
     * @param pUrl The link to open
     */
    public static void openLink(String pUrl) 
    {
        browserAdapter.openLink(pUrl);
    }

    /**
     * Translate a string.
     * @param pText The text to translate
     * @param pObjects No idea what that is.
     * @return The translated string?
     */
    public static String translate(String pText, Object... pObjects)
    {
        return translationAdapter.tr(pText, pObjects);
    }

    private static class DefaultBrowserAdapter implements BrowserAdapter
    {
        //@Override
        public void openLink(String pUrl) 
        {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) 
            {
                try 
                {
                    Desktop.getDesktop().browse(new URI(pUrl));
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                } 
                catch (URISyntaxException e) 
                {
                    e.printStackTrace();
                }
            } 
            else
            {
                System.err.println(translate("Opening link not supported on current platform (''{0}'')", pUrl));
            }
        }
    }

    private static class DefaultTranslationAdapter implements TranslationAdapter
    {
        //@Override
        public String tr(String pText, Object... pObjects)
        {
            return MessageFormat.format(pText, pObjects);
        }
    }
}
