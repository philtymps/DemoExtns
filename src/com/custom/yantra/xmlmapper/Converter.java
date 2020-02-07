/**
  * Converter.java
  *
  **/

// PACKAGE
package com.custom.yantra.xmlmapper;
import com.haht.xml.textdata.*;
import org.w3c.dom.Document;
import java.io.*;
import java.util.*;
import com.custom.yantra.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;

public class Converter 
{
    public Converter()
    {
    }
	
	public Converter (String sMapName, String sHtdFile, String sFileName)
	{
		m_sMapName = sMapName;
		m_sHtdFile = sHtdFile;
		m_sFileName = sFileName;
		m_bIsQuoted = false;
	}

	public Converter (String sMapName, String sHtdFile, String sFileName, boolean bIsQuoted)
	{
		m_sMapName = sMapName;
		m_sHtdFile = sHtdFile;
		m_sFileName = sFileName;
		m_bIsQuoted = bIsQuoted;
	}	
	
	public static final int MAP_XML_TO_FLATFILE = 1;
	public static final int MAP_FLATFILE_TO_XML = 2;
	public static final int MAP_XML_TO_XML = 3;
	public static final int MAP_XML_TO_PROPS = 4;
	public static final int MAP_PROPS_TO_XML = 5;
	
	// public accessor methods
	public	String	getHtdFile () { return m_sHtdFile; }
	public	void	setHtdFile (String sHtdFile) { m_sHtdFile = sHtdFile; }
	public	String	getMapName () { return m_sMapName; }
	public	void	setMapName (String sMapName) { m_sMapName = sMapName; }
	public	String	getFileName () { return m_sFileName; }
	public	void	setFileName (String sFileName) { m_sFileName = sFileName; }
	public	boolean	isQuoted () { return m_bIsQuoted; }
	public	void	setIsQuoted (boolean bIsQuoted) { m_bIsQuoted = bIsQuoted; }

	// public methods
	public String	convert (YFSEnvironment env, Properties props, int iMapMethod, Document inDoc) throws Exception
	{
	  String	sRet = null;
	  try {
		
		if (iMapMethod == MAP_XML_TO_FLATFILE)
		{
			// convert XML to Flat File
			Class<?>  cls = java.lang.Class.forName (getMapName());
			YantraMapper map = (YantraMapper)cls.newInstance();
			map.setEnv (env);
			map.setProps (props);
			
			// convert the XML document into an intermediate XML for Flat File
			byte [] bXML = null;
			if (inDoc == null)
				bXML = map.transform(new FileInputStream (getFileName()));
			else
				bXML = map.transform  (new ByteArrayInputStream (YFSXMLUtil.getXMLString (inDoc).getBytes()));
			
			// get the flat file text description name and create a converter
			TextDataMap mapOut = new TextDataMap(getHtdFile());
			XmlToTextDataConverter conv = new XmlToTextDataConverter ();
	
			// convert the XML document into an intermediate XML
			sRet = new String(conv.convertXmlToTextData (bXML, mapOut));
		}
		else if (iMapMethod == MAP_FLATFILE_TO_XML)
		{
			// convert flat file to XML
			TextDataMap mapIn = new TextDataMap(getHtdFile());
			TextDataToXmlConverter conv = new TextDataToXmlConverter ();
			if (isQuoted())
				conv.setDetectLiterals (true);
			else		
				conv.setDetectLiterals (false);
	
			// convert the flat file to an intermediate XML
			TextData txt = new TextData (new FileInputStream (getFileName()));
			byte [] bXML = conv.convertTextDataToXml(txt, mapIn);
			
			// convert intermediate XML to YANTRA XML
			Class<?>  cls = java.lang.Class.forName (getMapName());
			YantraMapper map = (YantraMapper)cls.newInstance();
			map.setEnv (env);
			map.setProps (props);
						
			// do the transformation on the given data
			sRet = new String(map.transform(new ByteArrayInputStream (bXML)));
		}
		else if (iMapMethod == MAP_XML_TO_XML)
		{
			if (inDoc!=null && YFSUtil.getDebug())
			{
				System.out.println ("Docment Transform Beginning on Input Document");
				System.out.println (YFSXMLUtil.getXMLString (inDoc));
			}
			// convert XML to XML
			Class<?>  cls = java.lang.Class.forName (getMapName());
			YantraMapper map = (YantraMapper)cls.newInstance();
			map.setEnv (env);
			map.setProps (props);

			byte [] bXML = null;
			if (inDoc == null)
				bXML = map.transform(new FileInputStream (getFileName()));
			else
				bXML = map.transform (new ByteArrayInputStream (YFSXMLUtil.getXMLString (inDoc).getBytes()));
			sRet = new String (bXML);
		}
		else if (iMapMethod == MAP_XML_TO_PROPS)
		{
			Properties	propsToWrite = new Properties();
	
			// NOTE:  This will convert the document-level (top) element into a properties object
			YFCDocument	docToConvert = YFCDocument.getDocumentFor (inDoc);
			YFCElement	eleToConvert = docToConvert.getDocumentElement();

			// get the root element to convert to properties
			if (eleToConvert.getChildElement (getMapName()) != null)
				eleToConvert = eleToConvert.getChildElement(getMapName());
				
			Iterator<?>	mapAttributes = eleToConvert.getAttributes().keySet().iterator();
			while (mapAttributes.hasNext())
			{
				String keyToWrite = (String)mapAttributes.next();
				propsToWrite.put (keyToWrite, eleToConvert.getAttribute (keyToWrite));
			}
			ByteArrayOutputStream bProps = new ByteArrayOutputStream();
			propsToWrite.store (bProps, getFileName());
			sRet = bProps.toString();
		}
		else if (iMapMethod == MAP_PROPS_TO_XML)
		{
			Properties propsToRead = new Properties();
			propsToRead.load (new FileInputStream (getFileName()));
			YFCDocument	docToConvert = YFCDocument.createDocument (getMapName());
			YFCElement	eleToConvert = docToConvert.getDocumentElement();
			Enumeration<?> enumProps = propsToRead.propertyNames();
			while (enumProps.hasMoreElements())
			{
				String	sPropName = (String)enumProps.nextElement();
				String	sPropValue = (String)propsToRead.get(sPropName);
				if (sPropValue == null)
					sPropValue = "";
				eleToConvert.setAttribute (sPropName, sPropValue);
			}
			sRet = docToConvert.toString();
		}			
		if(YFSUtil.getDebug())
		{
			System.out.println ("Transformation Completed:\r\n" + sRet);
		}
		return (sRet);	
	  } catch (Exception e) {
	  		e.printStackTrace ();			
			throw new Exception ("Transformation Exception: " + e.toString());
	  }
	}	
	// private member variables
	private String					m_sHtdFile;
	private String					m_sMapName;
	private String					m_sFileName;
	private	boolean					m_bIsQuoted;
}

