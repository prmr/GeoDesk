package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.openstreetmap.gui.app.SettingManager;
import org.openstreetmap.gui.xml.XMLWriter;

/**
 * Default map controller which implements map moving by pressing the right
 * mouse button and zooming by double click or by mouse wheel.
 *
 * @author Jan Peter Stotz
 * @author Martin P. Robillard Marker display
 *
 */
public class JMapController implements MouseListener, MouseMotionListener, MouseWheelListener 
{
    private static final int MOUSE_BUTTONS_MASK = MouseEvent.BUTTON3_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
    private static final int MAC_MOUSE_BUTTON3_MASK = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
    
    private JMapViewer aMap;
    
    public JMapController(JMapViewer pMap) 
    {
        aMap = pMap;
        setMovementMouseButton(MouseEvent.BUTTON1); 
        aMap.addMouseListener((MouseListener) this);
        aMap.addMouseWheelListener((MouseWheelListener) this);
        aMap.addMouseMotionListener((MouseMotionListener) this);
    }
    
    private Point lastDragPoint;
    private Point lastClickedPoint;

    private boolean isMoving = false;

    private boolean movementEnabled = true;

    private int movementMouseButton = MouseEvent.BUTTON3;
    private int movementMouseButtonMask = MouseEvent.BUTTON3_DOWN_MASK;

    private boolean wheelZoomEnabled = true;
    private boolean doubleClickZoomEnabled = true;
    
    private void maybeShowPopup(MouseEvent e) 
    {
        if( e.isPopupTrigger() )
        {
            lastClickedPoint = e.getPoint();
            MapMarker[] lMarkers = aMap.getMapMarkersAt(lastClickedPoint);
            if( lMarkers.length > 1 )
            {
                JOptionPane.showMessageDialog(aMap, "Multiple markers selected. Select either a single marker, or an unmarked area of the map.", "Marker Selection Error", JOptionPane.ERROR_MESSAGE);
            }
            else if( lMarkers.length == 0 )
            {
                JPopupMenu lPopup = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem("Add Marker");
                menuItem.addActionListener(new ActionListener() 
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        MarkerInputPanel lMIP = new MarkerInputPanel();
                        int lResult = JOptionPane.showConfirmDialog(aMap, lMIP, "New Marker Data", JOptionPane.OK_CANCEL_OPTION);
                        Coordinate lCoord = aMap.getPosition((int)lastClickedPoint.getX(), (int)lastClickedPoint.getY());
                        if( lResult == JOptionPane.OK_OPTION )
                        {    
                            aMap.addMapMarker(new MapMarkerDot(lCoord.getLatitude(),lCoord.getLongitude(),lMIP.getName(),lMIP.getDescription()));
                        
                            List<MapMarker> lMarkers = aMap.getMapMarkerList();
                            try
                            {
                                String lFile = SettingManager.getInstance().getDataFileName();
                                XMLWriter.backup(lFile);
                                XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), lFile);
                            }
                            catch( Exception exception)
                            {}
                        }
                    }
                });
                lPopup.add(menuItem); 
                lPopup.show(e.getComponent(), e.getX(), e.getY());
            }
            else if( lMarkers.length == 1 )
            {
                // Delete and edit are supported
                JPopupMenu lPopup = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem("Delete Marker");
                final MapMarker lSelected = lMarkers[0];
                menuItem.addActionListener( new ActionListener() 
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        int lResult = JOptionPane.showConfirmDialog(aMap, "Delete maker \"" + lSelected.getName() + "\"\nAre you sure?" , "Confirm Marker Deletion", JOptionPane.YES_NO_OPTION);
                        if( lResult == JOptionPane.YES_OPTION )
                        {
                            aMap.removeMapMarker(lSelected);
                            List<MapMarker> lMarkers = aMap.getMapMarkerList();
                            try
                            {
                                String lFile = SettingManager.getInstance().getDataFileName();
                                XMLWriter.backup(lFile);
                                XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), lFile);
                            }
                            catch( Exception exception)
                            {}
                        }
                    }
                });
                lPopup.add(menuItem);
                
                menuItem = new JMenuItem("Edit Marker");
                menuItem.addActionListener( new ActionListener() 
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        MarkerInputPanel lMIP = new MarkerInputPanel(lSelected.getName(), lSelected.getDescription());
                        int lResult = JOptionPane.showConfirmDialog(aMap, lMIP, "Edit Marker Data", JOptionPane.OK_CANCEL_OPTION);
                        if( lResult == JOptionPane.OK_OPTION )
                        {    
                            lSelected.setName(lMIP.getName());
                            lSelected.setDescription(lMIP.getDescription());
                            List<MapMarker> lMarkers = aMap.getMapMarkerList();
                            try
                            {
                                String lFile = SettingManager.getInstance().getDataFileName();
                                XMLWriter.backup(lFile);
                                XMLWriter.write((MapMarker[])lMarkers.toArray(new MapMarker[lMarkers.size()]), lFile);
                            }
                            catch( Exception exception)
                            {}
                        }
                    }
                });
                
                lPopup.add(menuItem); 
                lPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (!movementEnabled || !isMoving)
            return;
        // Is only the selected mouse button pressed?
        if ((e.getModifiersEx() & MOUSE_BUTTONS_MASK) == movementMouseButtonMask) {
            Point p = e.getPoint();
            if (lastDragPoint != null) {
                int diffx = lastDragPoint.x - p.x;
                int diffy = lastDragPoint.y - p.y;
                aMap.moveMap(diffx, diffy);
            }
            lastDragPoint = p;
        }
    }

    public void mouseClicked(MouseEvent e) 
    {
        if( doubleClickZoomEnabled && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) 
        {
            aMap.zoomIn(e.getPoint());
        }
        else if( e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON2 )
        {
            MapMarker[] lMarkers = aMap.getMapMarkersAt(e.getPoint());
            String lMessage = "";
            for( MapMarker marker : lMarkers )
            {
                lMessage += marker.getName() + "\n" + marker.getDescription() + "\n";
            }
            JOptionPane.showMessageDialog(aMap, lMessage, "Location Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void mousePressed(MouseEvent e) 
    {
        maybeShowPopup(e);
        if (e.getButton() == movementMouseButton || isPlatformOsx() && e.getModifiersEx() == MAC_MOUSE_BUTTON3_MASK) {
            lastDragPoint = null;
            isMoving = true;
        }
    }

    public void mouseReleased(MouseEvent e) 
    {
        maybeShowPopup(e);
        if (e.getButton() == movementMouseButton || isPlatformOsx() && e.getButton() == MouseEvent.BUTTON1) {
            lastDragPoint = null;
            isMoving = false;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (wheelZoomEnabled) {
            aMap.setZoom(aMap.getZoom() - e.getWheelRotation(), e.getPoint());
        }
    }

    public boolean isMovementEnabled() {
        return movementEnabled;
    }

    /**
     * Enables or disables that the map pane can be moved using the mouse.
     *
     * @param movementEnabled
     */
    public void setMovementEnabled(boolean movementEnabled) {
        this.movementEnabled = movementEnabled;
    }

    public int getMovementMouseButton() {
        return movementMouseButton;
    }

    /**
     * Sets the mouse button that is used for moving the map. Possible values
     * are:
     * <ul>
     * <li>{@link MouseEvent#BUTTON1} (left mouse button)</li>
     * <li>{@link MouseEvent#BUTTON2} (middle mouse button)</li>
     * <li>{@link MouseEvent#BUTTON3} (right mouse button)</li>
     * </ul>
     *
     * @param movementMouseButton
     */
    public void setMovementMouseButton(int movementMouseButton) {
        this.movementMouseButton = movementMouseButton;
        switch (movementMouseButton) {
            case MouseEvent.BUTTON1:
                movementMouseButtonMask = MouseEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                movementMouseButtonMask = MouseEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                movementMouseButtonMask = MouseEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new RuntimeException("Unsupported button");
        }
    }

    public boolean isWheelZoomEnabled() {
        return wheelZoomEnabled;
    }

    public void setWheelZoomEnabled(boolean wheelZoomEnabled) {
        this.wheelZoomEnabled = wheelZoomEnabled;
    }

    public boolean isDoubleClickZoomEnabled() {
        return doubleClickZoomEnabled;
    }

    public void setDoubleClickZoomEnabled(boolean doubleClickZoomEnabled) {
        this.doubleClickZoomEnabled = doubleClickZoomEnabled;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        // Mac OSX simulates with  ctrl + mouse 1  the second mouse button hence no dragging events get fired.
        //
        if (isPlatformOsx()) {
            if (!movementEnabled || !isMoving)
                return;
            // Is only the selected mouse button pressed?
            if (e.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK) {
                Point p = e.getPoint();
                if (lastDragPoint != null) {
                    int diffx = lastDragPoint.x - p.x;
                    int diffy = lastDragPoint.y - p.y;
                    aMap.moveMap(diffx, diffy);
                }
                lastDragPoint = p;
            }

        }

    }

    /**
     * Replies true if we are currently running on OSX
     *
     * @return true if we are currently running on OSX
     */
    public static boolean isPlatformOsx() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("mac os x");
    }
}
