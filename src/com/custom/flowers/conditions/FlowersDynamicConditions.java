/**
  * FlowersDynamicConditions.java
  *
  **/

// PACKAGE
package com.custom.flowers.conditions;

import com.yantra.ycp.japi.*;
import java.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;
import com.custom.yantra.util.*; 

import org.w3c.dom.*;

public class FlowersDynamicConditions implements YCPDynamicCondition, YCPDynamicConditionEx 
{
    public FlowersDynamicConditions()
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
				System.out.println ("Flowers Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				YFSUtil.dumpMapData (mapData);
			}
			if (name.startsWith ("Is Rejected By Node"))
			{
				// first look in mapData
				String	strStatus = (String)mapData.get("Status");
				String	strExceptionType = (String)mapData.get("ExceptionType");
				if (YFSUtil.getDebug())
				{
					System.out.println ("Status="+strStatus);
					System.out.println ("ExceptionType="+strExceptionType);
				}
				
				// if status not in mapData and this is a status change event
				if (strStatus == null || strStatus.length() == 0 
				&& (strExceptionType != null && strExceptionType.equals("ON_STATUS_CHANGE")))
				{
					// assume orderstatus change transaction
					YFCDocument docChangeOrderStatus = YFCDocument.getDocumentFor(xmlData);
					strStatus = docChangeOrderStatus.getDocumentElement().getAttribute ("BaseDropStatus");							
				}
				bRet = strStatus.equals ("1300.100");			
			}	
			if (YFSUtil.getDebug())
			{
				System.out.println ("Dynamic Condition Results");
				System.out.println ("bRet="+bRet);
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in FlowersDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
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
	@SuppressWarnings("rawtypes")
	private Map	m_props;

}

