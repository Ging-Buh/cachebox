/*
 * Copyright (c) 2007 by the University of Applied Sciences Northwestern Switzerland (FHNW)
 * 
 * This program can be redistributed or modified under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 * This program is distributed without any warranty or implied warranty
 * of merchantability or fitness for a particular purpose.
 *
 * See the GNU General Public License for more details.
 */

package ch.fhnw.imvs.gpssimulator.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLParser extends DefaultHandler {

	private List<XMLData> xmlData;
	private int time = 0;
	
	private XMLParser(){
        time = 0;
        xmlData = new ArrayList<XMLData>();
	}
	
	public static List<XMLData> parse(File file) throws IOException {
		try {
			XMLParser h = new XMLParser();

	        XMLReader xr = XMLReaderFactory.createXMLReader();
	        xr.setContentHandler(h);
	        xr.setErrorHandler(h);
	        FileReader r = new FileReader(file.getAbsoluteFile());
	        xr.parse(new InputSource(r));
	        
	        return h.xmlData;
		} catch (SAXException e) {
			return null;
		}
	}

//	private double convert(double deg){
//		int sig = deg>0?1:-1;
//		deg = Math.abs(deg);
//		int degInt = (int) deg;
//		double decFrac = deg - degInt;
//		return sig * (100*degInt + 60*decFrac);
//	}
	
	@Override
	public void startElement (String uri, String name, String qName, Attributes atts) {
		if (qName.equals("waypoint")) {
			int    timeDiff = Integer.parseInt(atts.getValue("time"));
			double latitude = Double.parseDouble(atts.getValue("latitude"));
			double longitude = Double.parseDouble(atts.getValue("longitude"));
			double altitude = Double.parseDouble(atts.getValue("altitude"));
			time = time + timeDiff;
			xmlData.add(new XMLData(time, latitude, longitude, altitude));
		}
	}
}
