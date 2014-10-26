package org.openstreetmap.gui.jmapviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Used to provide input data for a map marker.
 * 
 * @author Martin P. Robilard
 */
@SuppressWarnings("serial")
public class MarkerInputPanel extends JPanel 
{
	private static final int FIELD_WIDTH = 40;
	private static final int FIELD_HEIGHT = 5;

    private JTextField aNameField = new JTextField(FIELD_WIDTH);
    private JTextArea  aDescriptionField = new JTextArea(FIELD_HEIGHT, FIELD_WIDTH);
    
    /**
     * Creates a panel with no title or description.
     */
    public MarkerInputPanel()
    {
        this("", "");
    }
    
    /**
     * Creates a panel with a panel name and description.
     * @param pName The name already set.
     * @param pDescription The description already set.
     */
    public MarkerInputPanel(String pName, String pDescription)
    {
        aNameField.addAncestorListener(new RequestFocusListener());
        aNameField.setText(pName);
        aDescriptionField.setText(pDescription);
        setLayout(new BorderLayout());
        JPanel lTop = new JPanel();
        lTop.setLayout(new FlowLayout(FlowLayout.LEFT));
        add( lTop, BorderLayout.NORTH);
        lTop.add( new JLabel("Name:"), BorderLayout.NORTH);
        lTop.add( aNameField, BorderLayout.NORTH);
        
        JPanel lCenter = new JPanel();
        add(lCenter);
        lCenter.setLayout(new BorderLayout());
        lCenter.add(new JLabel("Description:"), BorderLayout.NORTH);
        JScrollPane lSPane = new JScrollPane(aDescriptionField);
        lCenter.add(lSPane, BorderLayout.CENTER);     
    }
    
    /**
     * @return The name of the place associated with the marker.
     */
    public String getMarkerName()
    {
        return aNameField.getText();
    }
    
    /**
     * @return The description associated with the marker.
     */
    public String getDescription()
    {
        return aDescriptionField.getText();
    }
}

/**
 *  Convenience class to request focus on a component.
 *
 *  When the component is added to a realized Window then component will
 *  request focus immediately, since the ancestorAdded event is fired
 *  immediately.
 *
 *  When the component is added to a non realized Window, then the focus
 *  request will be made once the window is realized, since the
 *  ancestorAdded event will not be fired until then.
 *
 *  Using the default constructor will cause the listener to be removed
 *  from the component once the AncestorEvent is generated. A second constructor
 *  allows you to specify a boolean value of false to prevent the
 *  AncestorListener from being removed when the event is generated. This will
 *  allow you to reuse the listener each time the event is generated.
 */
class RequestFocusListener implements AncestorListener
{
    private boolean aRemoveListener;

    /**
     *  Convenience constructor. The listener is only used once and then it is
     *  removed from the component.
     */
    public RequestFocusListener()
    {
        this(true);
    }

    /**
     *  Constructor that controls whether this listen can be used once or
     *  multiple times.
     *
     *  @param pRemoveListener when true this listener is only invoked once
     *                        otherwise it can be invoked multiple times.
     */
    public RequestFocusListener(boolean pRemoveListener)
    {
        this.aRemoveListener = pRemoveListener;
    }

    public void ancestorAdded(AncestorEvent pEvent)
    {
        JComponent component = pEvent.getComponent();
        component.requestFocusInWindow();

        if(aRemoveListener)
        {
            component.removeAncestorListener(this);
        }
    }

    public void ancestorMoved(AncestorEvent pEvent) {}

    public void ancestorRemoved(AncestorEvent pEvent) {}
}

