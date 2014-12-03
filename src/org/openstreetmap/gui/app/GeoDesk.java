package org.openstreetmap.gui.app;

//License: GPL. 
// Copyright 2012 by Martin P. Robillard
// Based on code by Jan Peter Stotz

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
import org.openstreetmap.gui.xml.KMLReader;
import org.openstreetmap.gui.xml.KMLReader.MarkerData;
import org.openstreetmap.gui.xml.XMLWriter;

/**
 * The main application class.
 * 
 * @author Martin P. Robillard
 */
@SuppressWarnings("serial")
public class GeoDesk extends JFrame implements JMapViewerEventListener  
{   
    private static final String APP_NAME = "GeoDesk 0.1";
    
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
        
        KMLReader.MarkerData[] lData = null;
        try
        {   
            String lFile = SettingManager.getInstance().getDataFileName();
            if( lFile != null && new File(lFile).exists() )
            {
                lData = KMLReader.extractData(lFile);
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        if( lData!= null )
        {
            for( KMLReader.MarkerData lPoint : lData )
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
        JMenuItem lDataFile = new JMenuItem("New Data File", KeyEvent.VK_N);
        lDataFile.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
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
                		SettingManager.getInstance().setDataFileName(path);
                		List<MapMarker> lMarkers = aMap.getMapMarkerList();
                		try
                		{
                			XMLWriter.backup(chooser.getSelectedFile().getAbsolutePath());
                			XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), 
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
        
        JMenuItem lLoadMenu = new JMenuItem("Load from Data File", KeyEvent.VK_L);
        lLoadMenu.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
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
                            MarkerData[] lData = KMLReader.extractData(chooser.getSelectedFile().getAbsolutePath());
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
        
        JMenuItem lImport = new JMenuItem("Import Data", KeyEvent.VK_I);
        lImport.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML and KML Files", "xml", "kml");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose existing data file to import from");
                int returnVal = chooser.showOpenDialog(aMap);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                    try
                    {   
                        MarkerData[] lData = KMLReader.extractData(chooser.getSelectedFile().getAbsolutePath());
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
                        XMLWriter.backup(SettingManager.getInstance().getDataFileName());
                        XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), 
                        		SettingManager.getInstance().getDataFileName());
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
