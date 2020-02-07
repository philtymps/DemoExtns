/**
  * SiebelXMLMapper.java
  *
  **/

// PACKAGE
package com.custom.siebel.xmlmapper;

import com.custom.yantra.api.XMLMapper;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Document;

import com.custom.yantra.util.*;

public class SiebelXMLMapper extends XMLMapper
{

    public SiebelXMLMapper()
    {
    }

    public Document SiebelXMLToYantraXML(YFSEnvironment env, Document docIn)
        throws Exception
    {
        Document docNew = SiebelMapperUtils.removeSOAPEnvelope(docIn);
        setSiebelXML(docIn);

		if (YFSUtil.getDebug())
		{
			System.out.print ("In SiebelXMLToYantraXML");
		}
		String	sDefaultOrg = getProperties().getProperty ("org");
		if (sDefaultOrg == null || sDefaultOrg.length() == 0)
			sDefaultOrg = "DEFAULT";

		SiebelMapperUtils.setDefaultOrgCode (sDefaultOrg);
		return SiebelPostProcessIn (env, docIn, XMLToXML (env, docNew));
    }

    public Document SiebelPostProcessIn (YFSEnvironment env, Document docIn, Document docOut) throws Exception
	{
		return docOut;
	}
	
	
	public Document YantraXMLToSiebelXML(YFSEnvironment env, Document docIn)
        throws Exception
    {
		if (YFSUtil.getDebug())
		{
			System.out.print ("In YantraXMLToSiebelXML");
		}
		String	sDefaultOrg = getProperties().getProperty ("org");
		if (sDefaultOrg == null || sDefaultOrg.length() == 0)
			sDefaultOrg = "DEFAULT";

		SiebelMapperUtils.setDefaultOrgCode (sDefaultOrg);
        return SiebelMapperUtils.addSOAPEnvelope(SiebelPostProcessOut (env, docIn, XMLToXML(env, docIn)));
    }

    public Document SiebelPostProcessOut (YFSEnvironment env, Document docIn, Document docOut) throws Exception
	{
		String sDoc = YFSXMLUtil.getXMLString(docOut);
		int		iIdx;

		if ((iIdx = sDoc.indexOf ("ATPServiceLineItems")) > 0)		
		{
			do {
				sDoc = sDoc.substring (0, iIdx)+"ATPLineItems"+sDoc.substring(iIdx+19);
				iIdx = sDoc.indexOf("ATPServiceLineItems");
			} while (iIdx >= 0);
			docOut = new YFSXMLParser (sDoc, false).getDocument();
		}
		return docOut;
	/*
		This code can only be used if running 1.4 VM		
		return new YFSXMLParser (sDoc.replaceAll ("ATPServiceLineItems", "ATPLineItems"), false).getDocument();
	*/
	}

    public static Document getSiebelXML()
    {
        return docSiebelXML;
    }

    public static void setSiebelXML(Document docIn)
    {
        docSiebelXML = docIn;
    }

    protected static Document docSiebelXML;
}