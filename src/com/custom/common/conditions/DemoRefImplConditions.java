/**
  * DemoRefImplConditions.java
  *
  **/

// PACKAGE
package com.custom.common.conditions;
import com.yantra.ycp.japi.*;
import com.yantra.interop.japi.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;
import com.custom.yantra.util.*; 

import java.util.Map;
import org.w3c.dom.*;


public class DemoRefImplConditions implements YCPDynamicCondition, YCPDynamicConditionEx 
{
    public DemoRefImplConditions()
    {

    }

  	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, String xmlData)
	{
		YFCDocument	docXmlData = null;
		if (xmlData != null && xmlData.length() > 0)
			docXmlData = YFCDocument.getDocumentFor (xmlData);
		return evaluateCondition (env, name, mapData, docXmlData.getDocument());
	}
	
  	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document xmlData)
	{
		boolean ret = false;
		try {
			YFCDocument	docIn;
			if (xmlData == null)
				docIn = YFCDocument.getDocumentFor ("<InputXML"+YFSUtil.mapDataToAttributes (mapData)+"/>");
			else
				docIn = YFCDocument.getDocumentFor (xmlData);

			if (YFSUtil.getDebug())
			{					
				System.out.println ("Demo Reference Implementation Dynamic Condition Executing");
				System.out.println ("Condition Name="+name);
				System.out.println ("Conditon Map Data:");
				if (mapData != null)
					YFSUtil.dumpMapData (mapData);
				System.out.println ("Condition XML Input:");
				System.out.println (docIn.getString());
			}

			// Note: isCondition method will return true if the condition name contains the string passed.
			// Spaces and the case of the strings being compared are ignored in this comparison 
			// (e.g. "ISRUSH" is equal to "Is Rush")
			if (isCondition (name, "Is Rush"))
			{
				YFCElement	docEle = docIn.getDocumentElement();
				String sOrderHeaderKey = docEle.getAttribute ("OrderHeaderKey");
				String sShipmentKey = docEle.getAttribute ("ShipmentKey");
						
				// if the root element contains an OrderHeaderKey (i.e. Order, Shipment, Release etc.)
				if (sOrderHeaderKey != null && sOrderHeaderKey.trim().length() > 0)
				{
					ret = isRushOrder (env, sOrderHeaderKey);
					
				}
				else if (sShipmentKey != null && sShipmentKey.trim().length() > 0)
				{
					ret = isRushShipment (env, sShipmentKey);
				}
			}			
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in DemoRefImplConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}
		return ret;
	}
	
	@SuppressWarnings("rawtypes")
	public void setProperties(Map props)
	{
			m_props = props;
	}

	private boolean isRushOrder (YFSEnvironment env, String sOrderHeaderKey) throws Exception
	{
		boolean bRet = false;
		YFCDocument	docOrder = null;
		
		YFCDocument	docInput = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"" + sOrderHeaderKey + "\"/>");
		YFCDocument docOutputTemplate = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"\" OrderType=\"\"/>");
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		try {
			env.setApiTemplate ("getOrderDetails", docOutputTemplate.getDocument());
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to getOrderDetails:");
				System.out.println (docInput.getString());
			}
			docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docInput.getDocument()));
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Output from getOrderDetails:");
				System.out.println (docOrder.getString());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			env.clearApiTemplate ("getOrderDetails");
		}
		if (docOrder != null)
		{
			YFCElement	eleOrder = docOrder.getDocumentElement();
			bRet = eleOrder.getAttribute ("OrderType").equalsIgnoreCase ("RUSH");			
		}
		return bRet;
	}

	private boolean isRushShipment (YFSEnvironment env, String sShipmentKey) throws Exception
	{
		boolean bRet = false;
		YFCDocument	docShipment = null;
		
		YFCDocument	docInput = YFCDocument.getDocumentFor ("<Shipment ShipmentKey=\""+ sShipmentKey + "\"/>");
		YFCDocument docOutputTemplate = YFCDocument.getDocumentFor ("<Shipment ShipmentKey=\"\" OrderType=\"\"/>");
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		try {
			env.setApiTemplate ("getShipmentDetails", docOutputTemplate.getDocument());
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to getShipmentDetails:");
				System.out.println (docInput.getString());
			}
			docShipment = YFCDocument.getDocumentFor (api.getShipmentDetails (env, docInput.getDocument()));
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Output from getShipmentDetails:");
				System.out.println (docShipment.getString());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			env.clearApiTemplate ("getShipmentDetails");
		}
		if (docShipment != null)
		{
			YFCElement	eleShipment = docShipment.getDocumentElement();
			bRet = eleShipment.getAttribute ("OrderType").equalsIgnoreCase ("RUSH");			
		}
		return bRet;
	}

	// PRIVATE methods
	private	boolean	isCondition (String sName, String sCondition)
	{
		String	sUcConditionNoSpaces = sCondition.toUpperCase().replaceAll (" ", "");
		String	sUcNameNoSpaces = sName.toUpperCase().replaceAll (" ", "");
		return (sUcNameNoSpaces.indexOf (sUcConditionNoSpaces) >= 0);
	}

	@SuppressWarnings("rawtypes")
	public Map getProperties() {
		return m_props;
	}

	@SuppressWarnings("rawtypes")
	private Map	m_props;
}

