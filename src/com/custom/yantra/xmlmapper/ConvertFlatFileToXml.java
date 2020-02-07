/**
  * ConvertFlatFileToXml.java
  *
  **/

// PACKAGE
package com.custom.yantra.xmlmapper;

import com.custom.yantra.xmlmapper.Converter;

public class ConvertFlatFileToXml extends Converter
{
    public ConvertFlatFileToXml()
    {
		
    }
	
	public String convert (String sDoc) throws Exception
	{
		try {
			XMLMapperParametersDoc m_docParams = new XMLMapperParametersDoc (sDoc);
			setHtdFile (m_docParams.getXMLMapperParameters ().getAttribute ("HTD"));			
			if (m_docParams.getXMLMapperParameters ().getAttribute ("ISVALUESQUOTED").equalsIgnoreCase ("Y"))
				setIsQuoted (true);
			else		
				setIsQuoted (false);	
			setFileName (m_docParams.getXMLMapperParameters ().getAttribute ("DATA"));
			setMapName (m_docParams.getXMLMapperParameters ().getAttribute ("MAP"));

			return (convert (null, null, MAP_FLATFILE_TO_XML, null));
		} catch (Exception e) {
			e.printStackTrace ();
			return "Transform Exception: " + e.toString();
		}
	}
	
	public String convert (String sHtdFile, String sMapName, String sFileName) throws Exception
	{
		return convert (sHtdFile, sMapName, sFileName, false);
	}
	
	public String convert (String sHtdFile, String sMapName, String sFileName, boolean bIsQuoted) throws Exception
	{
		setHtdFile (sHtdFile);
		setMapName (sMapName);
		setFileName (sFileName);
		setIsQuoted (bIsQuoted);
		return (convert (null, null, MAP_FLATFILE_TO_XML, null));
	}
}

