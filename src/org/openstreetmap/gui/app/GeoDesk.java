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

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.xml.KMLReader;
import org.openstreetmap.gui.xml.KMLReader.MarkerData;
import org.openstreetmap.gui.xml.XMLWriter;

@SuppressWarnings("serial")
public class GeoDesk extends JFrame implements JMapViewerEventListener  
{   
    private static final String APP_NAME = "GeoDesk 0.1";
    
    private TileSource[] aTileSources = {new OsmTileSource.Mapnik(),
            new OsmTileSource.CycleMap(), new BingAerialTileSource(), 
            new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource()};
    private JMapViewer aMap = null;

    private JLabel zoomValue=null;
    private JLabel mperpLabelValue = null;

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
                aMap.addMapMarker(new MapMarkerDot(lPoint.aLatitude, lPoint.aLongitude, 
                        lPoint.aName, lPoint.aDescription));
            }
        }
        
        // So that the map layout runs in the AWT event thread.
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowOpened(WindowEvent e) 
            {
                super.windowOpened(e);
                aMap.setDisplayToFitMapMarkers();
                String lDataFileName = SettingManager.getInstance().getDataFileName();
                if( lDataFileName == null )
                {
                    lDataFileName = SettingManager.getInstance().getDefaultDataFileName();
                    SettingManager.getInstance().setDataFileName(lDataFileName);
                    JOptionPane.showMessageDialog(aMap,"Your marker data will be stored at the default location:\n" + 
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
        JMenu lMapMenu = new JMenu("Map");
        lMapMenu.setMnemonic(KeyEvent.VK_M);
        lMenuBar.add(lMapMenu);
        
        JMenuItem lMap = new JMenuItem("Mapnik",KeyEvent.VK_N);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - Mapnik Map");
                aMap.setTileSource(aTileSources[0]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("OSM Cycle",KeyEvent.VK_C);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - OSM Cycle Map");
                aMap.setTileSource(aTileSources[1]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("Bing Aerial",KeyEvent.VK_B);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - Bing Aerial Map");
                aMap.setTileSource(aTileSources[2]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("MapQuest OSM",KeyEvent.VK_O);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle(APP_NAME + " - MapQuest OSM Map");
                aMap.setTileSource(aTileSources[3]);
            }
        });
        lMapMenu.add(lMap);
        
        lMap = new JMenuItem("MapQuest Aerial",KeyEvent.VK_A);
        lMap.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                setTitle("GeoDesk - MapQuest Open Aerial Map");
                aMap.setTileSource(aTileSources[4]);
            }
        });
        lMapMenu.add(lMap);
        
        JMenu lDataMenu = new JMenu("Data");
        lMapMenu.setMnemonic(KeyEvent.VK_D);
        lMenuBar.add(lDataMenu);
        
        JMenuItem lDataFile = new JMenuItem("Data File",KeyEvent.VK_F);
        lDataFile.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "XML Files", "xml");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose where to save your data");
                int returnVal = chooser.showSaveDialog(aMap);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                    SettingManager.getInstance().setDataFileName(chooser.getSelectedFile().getAbsolutePath());
                    List<MapMarker> lMarkers = aMap.getMapMarkerList();
                    try
                    {
                        XMLWriter.backup(chooser.getSelectedFile().getAbsolutePath());
                        XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), chooser.getSelectedFile().getAbsolutePath());
                    }
                    catch( Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
        });
        lDataMenu.add(lDataFile);
        
        JMenuItem lImport = new JMenuItem("Import Data",KeyEvent.VK_I);
        lImport.addActionListener( new ActionListener() 
        {
            public void actionPerformed(ActionEvent pEvent) 
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(SettingManager.getInstance().getDataFileName()));
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "XML and KML Files", "xml", "kml");
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Choose existing data file");
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
                                aMap.addMapMarker(new MapMarkerDot(lPoint.aLatitude, lPoint.aLongitude, 
                                        lPoint.aName, lPoint.aDescription));
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
                        XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), SettingManager.getInstance().getDataFileName());
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
        
        // Markers
        JMenu lMarkersMenu = new JMenu("View");
        lMarkersMenu.setMnemonic(KeyEvent.VK_K);
        lMenuBar.add(lMarkersMenu);
        
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
                aMap.setZoomContolsVisible(((JCheckBoxMenuItem)pEvent.getSource()).isSelected());
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
        
        
        
        setJMenuBar(lMenuBar);
        
    }

    public static void main(String[] args) throws Exception
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SettingManager.getInstance().initialize();
        new GeoDesk();
    }

    private void updateZoomParameters() 
    {
        if (mperpLabelValue!=null)
            mperpLabelValue.setText(String.format("%s",aMap.getMeterPerPixel()));
        if (zoomValue!=null)
            zoomValue.setText(String.format("%s", aMap.getZoom()));
    }

    public void processCommand(JMVCommandEvent command) 
    {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }

}
