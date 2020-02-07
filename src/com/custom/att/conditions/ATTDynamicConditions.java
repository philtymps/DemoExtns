/**
  * ATTDynamicConditions.java
  *
  **/

// PACKAGE
package com.custom.att.conditions;

import com.yantra.ycp.japi.*;
import java.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;
import com.custom.yantra.util.*; 
import org.w3c.dom.*;

public class ATTDynamicConditions implements YCPDynamicCondition, YCPDynamicConditionEx 
{
    public ATTDynamicConditions()
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
			//YFCDocument xml = YFCDocument.getDocumentFor(xmlData);
			if (YFSUtil.getDebug())
			{					
				System.out.println ("AT&T Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				YFSUtil.dumpMapData (mapData);
			}
			if (name.startsWith ("Is Port Available"))
			{
				YFCDocument	docWorkOrder = YFCDocument.getDocumentFor (xmlData);
				YFCElement	eleWorkOrder = docWorkOrder.getDocumentElement();
				if (eleWorkOrder != null)
				{
					String sPortStatus = eleWorkOrder.getAttribute ("BaseDropStatus");
					
					if (sPortStatus.equals ("PortAvailable"))
						bRet = true;
				}
			}	
			else if (name.startsWith ("Is RM Exception"))
			{
				YFCDocument	docWorkOrder = YFCDocument.getDocumentFor (xmlData);
				YFCElement	eleWorkOrder = docWorkOrder.getDocumentElement();
				if (eleWorkOrder != null)
				{
					YFCElement	eleErrorMsg = eleWorkOrder.getChildElement ("ErrorMsg");
					if (eleErrorMsg != null)
						bRet = true;		
				}
			}
			else if (name.startsWith ("Is Data Valid"))
			{
				YFCDocument	docWorkOrder = YFCDocument.getDocumentFor (xmlData);
				YFCElement	eleWorkOrder = docWorkOrder.getDocumentElement();
				if (eleWorkOrder != null)
				{
					String sPortStatus = eleWorkOrder.getAttribute ("BaseDropStatus");
					
					if (sPortStatus != null && sPortStatus.length() > 0)
						bRet = true;
				}
			}
			
			if (YFSUtil.getDebug())
			{
				System.out.println ("Dynamic Condition Results");
				System.out.println ("bRet="+bRet);
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in ATTDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("ATTDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}		
		return bRet;
	}
	
	@SuppressWarnings("rawtypes")
	public void setProperties(Map props)
	{
			m_props = props;
	}
	
	@SuppressWarnings("rawtypes")
	public Map getProperties() {
		return m_props;
	}

	@SuppressWarnings({ "rawtypes" })
	private Map	m_props;

}

