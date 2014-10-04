package org.openstreetmap.gui.xml;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class XMLPrettyPrinter {

    public static String prettifyXml (String input) throws TransformerException {

        Source xmlInput = new StreamSource(new StringReader(input));
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    public static void main (String [] args) throws TransformerException {
        System.out.println(prettifyXml("<aaa><bbb/><ccc/></aaa>"));
    }

}

