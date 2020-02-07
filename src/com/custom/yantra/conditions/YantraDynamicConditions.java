/**
  * YantraDynamicConditions.java
  *
  **/

// PACKAGE
package com.custom.yantra.conditions;

import com.yantra.ycp.japi.*;
import java.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.*;
import com.custom.yantra.util.*; 
import org.w3c.dom.*;
import javax.xml.xpath.*;

public class YantraDynamicConditions implements YCPDynamicCondition, YCPDynamicConditionEx 
{
    public YantraDynamicConditions()
    {
    }

	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document xmlData)
	{
		try {
			return evaluateCondition (env, name, mapData, YFSXMLUtil.getXMLString (xmlData));
		} catch (Exception ignore) {
			return false;
		}
	}

  	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, String xmlData)
	{
		boolean	bRet = false;
		try {

			// The line below threw and exception when dynamic condition used inside
			// a pipeline.  Is this a doc bug?
			// YFCDocument xml = YFCDocument.getDocumentFor(xmlData);
			if (YFSUtil.getDebug())
			{					
				System.out.println ("Yantra Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				YFSUtil.dumpMapData (mapData);
			}
			if (name.startsWith ("TestProperty"))
			{
				Map.Entry prop = (Map.Entry)(m_props.entrySet ().iterator().next());
				YFCDocument	docIn = YFCDocument.getDocumentFor ("<GetProperty PropertyName=\""+(String)prop.getKey()+"\"/>");
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to GetProperty API:");
					System.out.println (docIn.getString ());
				}
				YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument docOut = YFCDocument.getDocumentFor (api.getProperty (env, docIn.getDocument()));
				YFCElement	eleProperty = docOut.getDocumentElement();
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from GetProperty API:");
					System.out.println (docOut.getString());
				}		
				bRet = ((String)prop.getValue()).equals (eleProperty.getAttribute ("PropertyValue"));
			}
			else if (name.startsWith ("TestAttribute"))
			{
				String	 sRoot	= (String)m_props.get("DocumentName");
				String	 sName 	= (String)m_props.get("AttributeName");
				String	 sTest 	= (String)m_props.get("AttributeValue");
				String 	 sValue = null;
				YFCDocument	inDoc = null;
				
				// default to Order if no Document Name passed
				if (sRoot == null)
					sRoot = "Order";

				if (xmlData != null && xmlData.length() > 0)
					inDoc = YFCDocument.getDocumentFor (xmlData);
				else
					inDoc = YFCDocument.getDocumentFor ("<" + sRoot + " " + YFSUtil.mapDataToAttributes(mapData) + "/>");
				
				if (YFSUtil.getDebug())
				{
					System.out.println ("DocumentElement="+sRoot);
					System.out.println ("AttributeName="+sName);
					System.out.println ("AttributeValue="+sTest);
				}
				if (sName != null && sTest != null && inDoc != null)
				{
					sValue = evaluateXPathExpression (sName, inDoc.getDocument());
					if (YFSUtil.getDebug())
					{
						System.out.println ("XPATH Expression Result="+sValue);
					}
					if (sValue != null)
						bRet = sValue.equalsIgnoreCase (sTest);
				}
				else
					bRet = (sTest == null && sValue == null);
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in YantraDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("YantraDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output From YantraDynamicCondition: "+bRet);
		}
		return bRet;
	}
	
	@SuppressWarnings("rawtypes")
	public void setProperties(Map props)
	{
			m_props = props;
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
	@SuppressWarnings("rawtypes")
	private Map	m_props;
}

