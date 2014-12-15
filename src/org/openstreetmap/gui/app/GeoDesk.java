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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openstreetmap.gui.jmapviewer.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.MapMarker;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.CycleOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapnikOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;
import org.openstreetmap.gui.persistence.JSONPersistence;
import org.openstreetmap.gui.persistence.MarkerData;

/**
 * The main application class.
 * 
 * @author Martin P. Robillard
 */
@SuppressWarnings("serial")
public class GeoDesk extends JFrame implements JMapViewerEventListener  
{   
    private static final String APP_NAME = "GeoDesk " + Version.instance().toString();
    
    private TileSource[] aTileSources = {new MapnikOsmTileSource(),
            new CycleOsmTileSource(), new BingAerialTileSource(), 
            new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource()};
    private JMapViewer aMap = null;

    private JLabel aZoomValue = null;

    /**
     * Build and launch the application.
     */
    public GeoDesk() 
    {
        super(APP_NAME + " - MapQuest OSM Map");
        
        aMap = new JMapViewer();
        aMap.addJMVListener(this);
        aMap.setTileSource(aTileSources[3]);
        
        buildMenus();

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        add(aMap, BorderLayout.CENTER);
        
        MarkerData[] lData = null;
        try
        {   
            String lFile = SettingManager.getInstance().getDataFileName();
            if( lFile != null && new File(lFile).exists() )
            {
                lData = JSONPersistence.loadMarkers(lFile);
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        if( lData!= null )
        {
            for( MarkerData lPoint : lData )
            {
                aMap.addMapMarker(new MapMarkerDot(lPoint.getLatitude(), lPoint.getLongitude(), 
                        lPoint.getName(), lPoint.getDescription()));
            }
        }
        
        // So that the map layout runs in the AWT event thread.
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowOpened(WindowEvent pEvent) 
            {
                super.windowOpened(pEvent);
                aMap.setDisplayToFitMapMarkers();
                String lDataFileName = SettingManager.getInstance().getDataFileName();
                if( lDataFileName == null )
                {
                    lDataFileName = SettingManager.getInstance().getDefaultDataFileName();
                    SettingManager.getInstance().setDataFileName(lDataFileName);
                    JOptionPane.showMessageDialog(aMap, "Your marker data will be stored at the default location:\n" + 
                            lDataFileName + ".\nTo change this, use the menu Data | Change Location.", 
                            "Marker Data Location", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        setVisible(true);
    }
    
    private void buildMenus()
    {
        JMenuBar lMenuBar = new JMenuBar();
        createMapMenu(lMenuBar);
        createDataMenu(lMenuBar);
        createViewMenu(lMenuBar);
        createHelpMenu(lMenuBar);
        setJMenuBar(lMenuBar);
    }

	private void createViewMenu(JMenuBar pMenuBar)
	{
		JMenu lMarkersMenu = new JMenu("View");
        lMarkersMenu.setMnemonic(KeyEvent.VK_K);
        pMenuBar.add(lMarkersMenu);
        
        JCheckBoxMenuItem lMarker = new JCheckBoxMenuItem("Show Markers", true);
        lMarker.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                aMap.setMapMarkerVisible(((JCheckBoxMenuItem)pEvent.getSource()).isSelected());
            }
        });
        lMarkersMenu.add(lMarker);
        
        lMarker = new JCheckBoxMenuItem("Show Zoom Controls", true);
        lMarker.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                aMap.setZoomControlsVisible(((JCheckBoxMenuItem)pEvent.getSource()).isSelected());
            }
        });
        lMarkersMenu.add(lMarker);
        
        lMarker = new JCheckBoxMenuItem("Show Legend", true);
        lMarker.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                aMap.setMapLegendVisible(((JCheckBoxMenuItem)pEvent.getSource()).isSelected());
            }
        });
        lMarkersMenu.add(lMarker);
        
        JMenuItem lFit = new JMenuItem("Fit Display", KeyEvent.VK_F);
        lFit.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                aMap.setDisplayToFitMapMarkers();
            }
        });
        lMarkersMenu.add(lFit);
	}

	private void createDataMenu(JMenuBar pMenuBar)
	{
		JMenu lDataMenu = new JMenu("Data");
        lDataMenu.setMnemonic(KeyEvent.VK_D);
        pMenuBar.add(lDataMenu);
        
        JMenuItem viewFileLocation = new JMenuItem("Current Data File Location", KeyEvent.VK_L);
        viewFileLocation.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				JOptionPane.showMessageDialog(aMap, "Your data is stored in " + SettingManager.getInstance().getDataFileName(), 
						"Current Data File Location", JOptionPane.INFORMATION_MESSAGE);				
			}
		});
        lDataMenu.add(viewFileLocation);
        
        JMenuItem lDataFile = new JMenuItem("New Data File Location...", KeyEvent.VK_N);
        lDataFile.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose a new destination data file");
                int returnVal = chooser.showSaveDialog(aMap);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                	String path = chooser.getSelectedFile().getAbsolutePath();
                	String confirmMessage = "";
                	boolean goAhead = true;
                	
                	if( new File(path).exists() )
                	{
                		goAhead = false;
                		confirmMessage = "File " + path + " already exists. Override?";
                		int confirmation = JOptionPane.showConfirmDialog(aMap, confirmMessage, "Confirm new data file", JOptionPane.OK_CANCEL_OPTION);
                		if( confirmation == JOptionPane.OK_OPTION )
                    	{
                			goAhead = true;
                    	}
                	}
                	
                	if( goAhead )
                	{
                		List<MapMarker> lMarkers = aMap.getMapMarkerList();
                		try
                		{
                			JSONPersistence.backup(SettingManager.getInstance().getDataFileName());
                			SettingManager.getInstance().setDataFileName(path);
                			JSONPersistence.storeMarkers((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), 
                					chooser.getSelectedFile().getAbsolutePath());
                		}
                		catch( Exception exception)
                		{
                			exception.printStackTrace();
                		}
                	}
                }
            }
        });
        lDataMenu.add(lDataFile);
        
        JMenuItem lLoadMenu = new JMenuItem("Load from Data File...", KeyEvent.VK_L);
        lLoadMenu.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setApproveButtonText("Load");
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "geojson");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose a new data file to load from");
                int returnVal = chooser.showSaveDialog(aMap);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                	String path = chooser.getSelectedFile().getAbsolutePath();
                	
                	if( !new File(path).exists() )
                	{
                		JOptionPane.showMessageDialog(aMap, path + " does not exist", "Error loading data file", JOptionPane.ERROR_MESSAGE);
                	}
                	else
                	{
                		aMap.removeAllMapMarkers();
                		SettingManager.getInstance().setDataFileName(path);
                		try
                        {   
                            MarkerData[] lData = JSONPersistence.loadMarkers(chooser.getSelectedFile().getAbsolutePath());
                            if( lData!= null )
                            {
                                for( MarkerData lPoint : lData )
                                {
                                    aMap.addMapMarker(new MapMarkerDot(lPoint.getLatitude(), lPoint.getLongitude(), 
                                            lPoint.getName(), lPoint.getDescription()));
                                }
                            }
                        }
                        catch( Exception e )
                        {
                            e.printStackTrace();
                        }
                        aMap.setDisplayToFitMapMarkers();
                	}
                }
            }
        });
        lDataMenu.add(lLoadMenu);
        
        JMenuItem lImport = new JMenuItem("Import Data...", KeyEvent.VK_I);
        lImport.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setApproveButtonText("Import");
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json", "geojson");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose existing data file to import from");
                int returnVal = chooser.showOpenDialog(aMap);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                    try
                    {   
                        MarkerData[] lData = JSONPersistence.loadMarkers(chooser.getSelectedFile().getAbsolutePath());
                        if( lData!= null )
                        {
                            for( MarkerData lPoint : lData )
                            {
                                aMap.addMapMarker(new MapMarkerDot(lPoint.getLatitude(), lPoint.getLongitude(), 
                                        lPoint.getName(), lPoint.getDescription()));
                            }
                        }
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                    
                    List<MapMarker> lMarkers = aMap.getMapMarkerList();
                    try
                    {
                    	JSONPersistence.backup(chooser.getSelectedFile().getAbsolutePath());
            			JSONPersistence.storeMarkers((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), 
            					chooser.getSelectedFile().getAbsolutePath());
                    }
                    catch( Exception exception)
                    {
                        exception.printStackTrace();
                    }
                    aMap.setDisplayToFitMapMarkers();
                }
            }
        });
        lDataMenu.add(lImport);
	}

	private void createMapMenu(JMenuBar pMenuBar)
	{
		JMenu lMapMenu = new JMenu("Map");
        lMapMenu.setMnemonic(KeyEvent.VK_M);
        pMenuBar.add(lMapMenu);
        
        JMenuItem lMap = new JMenuItem("Mapnik", KeyEvent.VK_N);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - Mapnik Map");
                aMap.setTileSource(aTileSources[0]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("OSM Cycle", KeyEvent.VK_C);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - OSM Cycle Map");
                aMap.setTileSource(aTileSources[1]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("Bing Aerial", KeyEvent.VK_B);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - Bing Aerial Map");
                aMap.setTileSource(aTileSources[2]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("MapQuest OSM", KeyEvent.VK_O);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - MapQuest OSM Map");
                aMap.setTileSource(aTileSources[3]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("MapQuest Aerial", KeyEvent.VK_A);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle("GeoDesk - MapQuest Open Aerial Map");
                aMap.setTileSource(aTileSources[4]);
            }
        });
        lMapMenu.add(lMap);
	}
	
	private void createHelpMenu(JMenuBar pMenuBar)
	{
		JMenu lHelpMenu = new JMenu("Help");
		lHelpMenu.setMnemonic(KeyEvent.VK_H);
        pMenuBar.add(lHelpMenu);
        final String message = "GeoDesk " + Version.instance().toString() + "\n\n" +
        		"By Martin P. Robillard based on the JMapViewer\n" + 
        		"framework by Jan Peter Stotz and others\n\n" +
        		"LICENSE: GNU GPL Version 3\n\n" + 
        		"See: http://martinrobillard.com/geodesk";
        
        JMenuItem lMap = new JMenuItem("About", KeyEvent.VK_A);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JOptionPane.showMessageDialog(aMap, message, "About GeoDesk", JOptionPane.NO_OPTION);
            }
        });
        lHelpMenu.add(lMap);
	}

    /**
     * Launch the application.
     * @param pArguments Not used.
     * @throws Exception Anything
     */
    public static void main(String[] pArguments) throws Exception
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SettingManager.getInstance().initialize();
        new GeoDesk();
    }

    private void updateZoomParameters() 
    {
        if(aZoomValue!=null)
        {
            aZoomValue.setText(String.format("%s", aMap.getZoom()));
        }
    }

    @Override
    public void processCommand(JMVCommandEvent pCommand) 
    {
        if (pCommand.getCommand().equals(JMVCommandEvent.CommandType.ZOOM) ||
                pCommand.getCommand().equals(JMVCommandEvent.CommandType.MOVE)) 
        {
            updateZoomParameters();
        }
    }

}
