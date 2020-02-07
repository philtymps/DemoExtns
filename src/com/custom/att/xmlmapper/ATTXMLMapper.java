/**
  * ATTXMLMapper.java
  *
  **/

// PACKAGE
package com.custom.att.xmlmapper;

import com.custom.yantra.api.XMLMapper;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Document;

import com.custom.yantra.util.*;
import com.yantra.yfc.dom.YFCDocument;

public class ATTXMLMapper extends XMLMapper
{

    public ATTXMLMapper()
    {
    }

    public Document ATTXMLToYantraXML(YFSEnvironment env, Document docIn)
        throws Exception
    {
        Document docNew = ATTMapperUtils.removeSOAPEnvelope(docIn, getProperties().getProperty("servicename"));

		if (YFSUtil.getDebug())
		{
			System.out.println ("In ATTXMLToYantraXML");
			System.out.println (YFCDocument.getDocumentFor (docNew).getString());
		}
		return ATTPostProcessIn (env, docIn, XMLToXML (env, docNew));
    }

    public Document ATTPostProcessIn (YFSEnvironment env, Document docIn, Document docOut) throws Exception
	{
		return docOut;
	}
		
	public Document YantraXMLToATTXML(YFSEnvironment env, Document docIn)
        throws Exception
    {
		if (YFSUtil.getDebug())
		{
			System.out.print ("In YantraXMLToATTXML");
		}
        return ATTMapperUtils.addSOAPEnvelope(ATTPostProcessOut (env, docIn, XMLToXML(env, docIn)), getProperties().getProperty("servicename"));
    }

    public Document ATTPostProcessOut (YFSEnvironment env, Document docIn, Document docOut) throws Exception
	{
		return docOut;
	}
}