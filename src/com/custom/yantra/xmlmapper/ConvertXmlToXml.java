/**
  * ConvertXmlToXml.java
  *
  **/

// PACKAGE
package com.custom.yantra.xmlmapper;

public class ConvertXmlToXml extends Converter
{
    public ConvertXmlToXml()
    {
    }
	
	public String convert (String sDoc) throws Exception
	{
		try {
			XMLMapperParametersDoc m_docParams = new XMLMapperParametersDoc (sDoc);
			if (m_docParams.getXMLMapperParameters ().getAttribute ("ISVALUESQUOTED").equalsIgnoreCase ("Y"))
				setIsQuoted (true);
			else		
				setIsQuoted (false);	
			setFileName (m_docParams.getXMLMapperParameters ().getAttribute ("DATA"));
			setMapName (m_docParams.getXMLMapperParameters ().getAttribute ("MAP"));

			return (convert (null, null, MAP_XML_TO_XML, null));
		} catch (Exception e) {
			return "Transformation Exception: " + e.toString();
		}	
	}
	
	public String convert (String sMapName, String sFileName) throws Exception
	{
		return convert (sMapName, sFileName, false);
	}
	
	public String convert (String sMapName, String sFileName, boolean bIsQuoted) throws Exception
	{
		setMapName (sMapName);
		setFileName (sFileName);
		setIsQuoted (bIsQuoted);
		return (convert (null, null, MAP_FLATFILE_TO_XML, null));
	}
}

