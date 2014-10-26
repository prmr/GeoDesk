package org.openstreetmap.gui.jmapviewer;

//License: GPL.

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
