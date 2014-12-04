package org.openstreetmap.gui.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openstreetmap.gui.jmapviewer.MapMarker;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author Martin P. Robillard
 * 
 * Writes a list of marker into an XML file.
 */
public final class XMLWriter
{   
	private static final int LINE_WIDTH = 65;
	
	private XMLWriter()
	{}
	
    /**
     * Write markers to file.
     * @param pMarkers The markers to write out.
     * @param pFileName The name of the destination file.
     * @throws Exception TODO
     */
    public static void write(MapMarker[] pMarkers, String pFileName) throws Exception
    {
        File lFile = new File(pFileName);
        lFile.createNewFile();
        Document doc = buildDocument(pMarkers);
        OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(LINE_WIDTH);
        format.setIndenting(true);
        format.setIndent(2);
        Writer out = new PrintWriter(lFile);
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(doc);
    }
    
    private static Document buildDocument(MapMarker[] pMarkers) throws Exception
    {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        
        Element root = doc.createElement("Document");
        doc.appendChild(root);
        
        for( MapMarker marker : pMarkers )
        {
            Element lPlacemark = doc.createElement("Placemark");
            root.appendChild(lPlacemark);
            
            Element lName = doc.createElement("name");
            lPlacemark.appendChild(lName);
            Text text = doc.createTextNode(marker.getName());
            lName.appendChild(text);
            
            Element lDescription = doc.createElement("description");
            lPlacemark.appendChild(lDescription);
            CDATASection lDesc = doc.createCDATASection(marker.getDescription());
            lDescription.appendChild(lDesc);
            
            Element lPoint = doc.createElement("Point");
            lPlacemark.appendChild(lPoint);
            Element lCoordinates = doc.createElement("coordinates");
            lPoint.appendChild(lCoordinates);
            text = doc.createTextNode(String.format("%.6f,%.6f,%.6f", marker.getLongitude(), marker.getLatitude(), 0.0));
            lCoordinates.appendChild(text);
        }
        
        return doc;
    }

    /**
     * Make a copy (backup) of the current markers file.
     * @param pFile The file to copy.
     * @throws PersistenceException If there is any problem with the backup.
     */
    public static void backup(String pFile) throws PersistenceException
    {
        File lFile = new File(pFile);
        if( !lFile.exists() )
        {
            return;
        }
        File lBackup = new File(pFile + ".backup");
        if(!lBackup.exists()) 
        {
        	try
        	{
        		lBackup.createNewFile();
        	}
        	catch(IOException exception)
        	{
        		throw new PersistenceException("Cannot created file: " + pFile, exception);
        	}
        }
    
        FileChannel source = null;
        FileChannel destination = null;
    
        try
        {
            source = new FileInputStream(lFile).getChannel();
            destination = new FileOutputStream(lBackup).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        catch( IOException exception )
        {
        	throw new PersistenceException("Problem making a backup of file: " + pFile, exception);
        }
        finally 
        {
            if(source != null)
            {
            	try
            	{
            		source.close();
            	}
            	catch( IOException exception1 )
            	{
            		throw new PersistenceException("Problem making a backup of file: " + pFile, exception1);
            	}
            	finally
            	{
            		if(destination != null)
            		{
            			try
            			{
            				destination.close();
            			}
            			catch( IOException exception2 )
            			{
            				throw new PersistenceException("Problem making a backup of file: " + pFile, exception2);
            			}
            		}
            	}
            }
        }
    }
}
