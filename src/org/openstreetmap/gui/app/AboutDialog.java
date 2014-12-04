package org.openstreetmap.gui.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provides basic information about the application.
 * 
 * @author Martin P. Robillard
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog
{
	/**
	 * An about box customized for a specific JFrame and version.
	 * @param pParent The parent frame.
	 * @param pVersion The application version to display in the frame.
	 */
	public AboutDialog(JFrame pParent, Version pVersion) 
	{
	    super(pParent, "About GeoDesk", true);
	    
	    add( new JLabel("GeoDesk " + pVersion.toString()));
//	    Box b = Box.createVerticalBox();
//	    b.add(Box.createGlue());
//	    b.add(new JLabel("Java source code, product and article"));
//	    b.add(new JLabel("By Java source and support"));
//	    b.add(new JLabel("At www.java2s.com"));
//	    b.add(Box.createGlue());
//	    getContentPane().add(b, "Center");

	    JPanel p2 = new JPanel();
	    JButton ok = new JButton("Ok");
	    p2.add(ok);
	    getContentPane().add(p2, "South");

	    ok.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
	        setVisible(false);
	      }
	    });

	    setSize(250, 150);
	  }

	  public static void main(String[] args) {
	    JDialog f = new AboutDialog(new JFrame(), new Version(0, 1, 0));
	    f.setVisible(true);
	  }
}