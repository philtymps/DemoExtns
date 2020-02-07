/*
 * Created on 31-Aug-2003
 *
 */
package com.custom.yantra.api;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;
import javax.xml.xpath.*;
import java.net.*;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dblayer.YFCDBContext;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCConfigurator;
import com.yantra.interop.util.PLTResourceLoader;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import com.custom.yantra.util.*;
import com.custom.yantra.xmlmapper.*;

/**
 * @author Phil Tympanick
 *
 */
public class XMLMapper implements YIFCustomApi
{
	public static final String FILENAME_PROPERTY_KEY = "filename";
	public static final String TEMPLATENAME_PROPERTY_KEY = "template";
	public static final String MAPNAME_PROPERTY_KEY = "map";
	public static final String HTDFILE_PROPERTY_KEY = "htd";
	public static final String ISQUOTED_PROPERTY_KEY = "quotes";
	public static final String ROOTNAME_PROPERTY_KEY = "root";
	public static final String PATHNAME_PROPERTY_KEY = "path";
	public static final String SUFFIX_PROPERTY_KEY = "suffix";

	private Properties mProp;

	public XMLMapper () { }

	public Document WriteXML (YFSEnvironment env, Document docIn) throws Exception
	{
		String sFileName = evaluateFileName (docIn);

		if (YFSUtil.getDebug())
		{
			System.out.println ("In WriteXML:");
		}
		if (sFileName.equalsIgnoreCase("stdout"))
			System.out.println (YFCDocument.getDocumentFor(docIn).getString());
		else
		{
			FileOutputStream sOut = new FileOutputStream (sFileName);

			YFCDocument docToWrite = YFCDocument.getDocumentFor (docIn);
			docToWrite.serialize (sOut);
			sOut.flush();
			sOut.close();
		}
		return docIn;
	}

	@SuppressWarnings("rawtypes")
	public Document WriteHTML (YFSEnvironment env, Document docIn) throws Exception
	{
		String sHtmlFileName = evaluateFileName (docIn);
		String sTemplateName = "";
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("In WriteHTML:");
			System.out.println (YFCDocument.getDocumentFor(docIn).getString());
		}

		// get the actual file name of XSL template using platform resource loader
		try {
		PLTResourceLoader	loader = new PLTResourceLoader (evaluateTemplateName(docIn));
		loader.setUseTemplateLookup(YFCConfigurator.getInstance().getBooleanProperty("xslcomponent.usetemplateLoading", false));
		URL url = loader.getResource((YFCDBContext)env, (Map)new HashMap());
		sTemplateName = url.toExternalForm();
		} catch (NullPointerException eIgnore){
			sTemplateName = "jar:file:/opt/Sterling/runtime/jar/platform/9_5/resources.jar!"+evaluateTemplateName(docIn);
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("XSL Template Name="+sTemplateName);
			System.out.println ("Ouput File Name="+sHtmlFileName);
		}
		// perform XSLT transformation (XML to HTML) and save to HTML file
		try {
			YFCDocument			docXML = YFCDocument.getDocumentFor (docIn);
			TransformerFactory factory = TransformerFactory.newInstance();
			StreamSource		xslStream = new StreamSource (sTemplateName);
			Transformer			transformer = factory.newTransformer(xslStream);
			StreamSource		source = new StreamSource (new CharArrayReader(docXML.getString().toCharArray()));
			StreamResult		result = new StreamResult (sHtmlFileName);

			// write the output file 
			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException e) {
			throw new Exception (e.getMessage());
		} catch (TransformerException e){
			throw new Exception (e.getMessage());
		}
		return docIn;
	}

	public Document ReadXML (YFSEnvironment env, Document docIn) throws Exception
	{
		String sFileName = evaluateFileName (docIn);

		if (YFSUtil.getDebug())
		{
			System.out.println ("In ReadXML:");
		}

		// read file and return as XML document
		return YFCDocument.getDocumentForXMLFile (sFileName).getDocument();
	}

	public Document XMLToFlatFile(YFSEnvironment env, Document docIn) throws Exception
	{
		String	docOut;
		String sFileName = evaluateFileName (docIn);
		docOut = convert (env, Converter.MAP_XML_TO_FLATFILE, docIn, sFileName);

		// write the output file
		FileOutputStream sOut = new FileOutputStream  (sFileName);
		sOut.write (docOut.getBytes());
		sOut.flush();
		sOut.close();
		return docIn;
	}

	public Document FlatFileToXML(YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument	docOut = null;
		String sFileName = evaluateFileName (docIn);

		docOut = YFCDocument.getDocumentFor(convert (env, Converter.MAP_FLATFILE_TO_XML, sFileName));
		return docOut.getDocument();
	}

	public Document XMLToProperties (YFSEnvironment env, Document docIn) throws Exception
	{
		String sFileName = evaluateFileName (docIn);
		String sPropsToWrite = convert (env, Converter.MAP_XML_TO_PROPS, docIn, sFileName);

		// write the properties into the output file
		FileOutputStream sOut = new FileOutputStream  (sFileName);
		sOut.write (sPropsToWrite.getBytes());
		sOut.flush();
		sOut.close();
		return docIn;
	}

	public Document PropertiesToXML(YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument	docOut = null;
		String sFileName = evaluateFileName (docIn);
		docOut = YFCDocument.getDocumentFor (convert (env, Converter.MAP_PROPS_TO_XML, sFileName));
		return docOut.getDocument();
	}


	public Document XMLToXML (YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument	docOut = null;
		docOut = YFCDocument.getDocumentFor (convert (env, Converter.MAP_XML_TO_XML, docIn, null));
		return docOut.getDocument ();
	}

	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	@SuppressWarnings("rawtypes")
	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
		if (YFSUtil.getDebug())
		{
			System.out.println ("In setProperties of XMLMapper API's");
			Enumeration enumProps = mProp.propertyNames();
			while (enumProps.hasMoreElements())
			{
				String sPropName = (String)enumProps.nextElement();
				String sPropValue = mProp.getProperty (sPropName);
				System.out.println ("\tArg Name: "+sPropName+"\tArg Value: "+sPropValue);
			}
		}
	}

	public Properties getProperties()
	{
		return mProp;
	}

	private	String convert (YFSEnvironment env, int iMapMethod, String sFileName) throws Exception
	{
		Converter conv = initConverter(iMapMethod, null, sFileName);
		return conv.convert (env, mProp, iMapMethod, null);
	}

	private String convert (YFSEnvironment env, int iMapMethod, Document inDoc, String sFileName) throws Exception
	{
		Converter conv = initConverter (iMapMethod, inDoc, sFileName);
		return conv.convert (env, mProp, iMapMethod, inDoc);
	}

	private String evaluateXPathExpression (String sXPathExpr, Document inDoc) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, inDoc, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;
		}
		return sResult;
	}

	private String evaluateFileName (Document inDoc) throws Exception
	{
		if (mProp == null)
		{
			throw new Exception("Properties not set");
		}
		String sFileName = evaluateXPathExpression((String)mProp.getProperty(FILENAME_PROPERTY_KEY), inDoc);
		if (sFileName == null || sFileName.trim().length() == 0)
		{
			throw new Exception("filename property does not exist or is blank");
		}

		// pre-append the path name if provided to the file name
		String sPathName = mProp.getProperty (PATHNAME_PROPERTY_KEY);
		if (sPathName != null)
			sFileName = sPathName + sFileName;

		// post-append the suffix name if provided, otherwise file name is complete
		String sSuffixName = mProp.getProperty (SUFFIX_PROPERTY_KEY);
		if (sSuffixName != null)
			sFileName = sFileName + sSuffixName;

		return sFileName;
	}

	private String evaluateTemplateName (Document inDoc) throws Exception
	{
		if (mProp == null)
		{
			throw new Exception("Properties not set");
		}
		String sTemplateName = evaluateXPathExpression((String)mProp.getProperty(TEMPLATENAME_PROPERTY_KEY), inDoc);
		if (sTemplateName == null || sTemplateName.trim().length() == 0)
		{
			throw new Exception("template property does not exist or is blank");
		}
		return sTemplateName;
	}
	
	private Converter initConverter (int iMapMethod, Document inDoc, String sFileName) throws Exception
	{
		String sMapName = null;
		String sRootName = null;
		String sHtdFile = null;

		if (iMapMethod == Converter.MAP_FLATFILE_TO_XML || iMapMethod == Converter.MAP_XML_TO_FLATFILE || iMapMethod == Converter.MAP_XML_TO_XML)
		{
			sMapName = mProp.getProperty (MAPNAME_PROPERTY_KEY);
			if (sMapName == null || sMapName.trim().length() == 0)
			{
				throw new Exception("'map' property required and does not exist or is blank");
			}

			if (iMapMethod != Converter.MAP_XML_TO_XML)
			{
				sHtdFile = mProp.getProperty (HTDFILE_PROPERTY_KEY);
				if (sHtdFile == null || sHtdFile.trim().length() == 0)
				{
					throw new Exception("'htd' property required and does not exist or is blank");
				}
			}
		}
		else
		{
			sRootName = mProp.getProperty (ROOTNAME_PROPERTY_KEY);
			if (sRootName == null || sRootName.trim().length() == 0)
			{
				throw new Exception("'root' property required and does not exist or is blank");
			}
			// use map name to store the root name in
			sMapName = sRootName;
		}

		String sIsQuoted = mProp.getProperty (ISQUOTED_PROPERTY_KEY);
		boolean	bIsQuoted = false;
		if (sIsQuoted != null && sIsQuoted.trim().length() > 0)
		{
			bIsQuoted = (sIsQuoted.equalsIgnoreCase("Y") || sIsQuoted.equalsIgnoreCase ("true"));
		}
		return new Converter (sMapName, sHtdFile, sFileName, bIsQuoted);
	}
}
