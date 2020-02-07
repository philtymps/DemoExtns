/**
  * SiebelDynamicConditionsImpl.java
  *
  **/

// PACKAGE
package com.custom.siebel.conditions;

import com.custom.siebel.xmlwrapper.*;
import com.custom.yantra.util.*;
import com.yantra.ycp.japi.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.custom.siebel.xmlmapper.*;
import java.util.*;
import org.w3c.dom.*;

public class SiebelDynamicConditionsImpl implements YCPDynamicCondition, YCPDynamicConditionEx
{
    public SiebelDynamicConditionsImpl()
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
			if (YFSUtil.getDebug())
			{					
				System.out.println ("Siebel Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				YFSUtil.dumpMapData (mapData);
			}
			// if "IsCreateRequest" (expects an Siebel Order Interface document)
			if (name.equalsIgnoreCase ("IsCreateOrderRequest"))
			{
				Document	docStripped = SiebelMapperUtils.removeSOAPEnvelope(xmlData);
				
				SiebelOrderInterfaceDoc oSiebelOrderDoc = new SiebelOrderInterfaceDoc (YFSXMLUtil.getXMLString (docStripped));
				SiebelOrderInterfaceDoc.IntegrationId oIntegrationId = oSiebelOrderDoc.getListOfOrderInterface ().getOrders().getIntegrationId();
				bRet = oIntegrationId.getText() == null || oIntegrationId.getText().length() == 0;
			}	

			if (YFSUtil.getDebug())
			{
				System.out.println ("Siebel Dynamic Condition Results");
				System.out.println ("bRet="+bRet);
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in SiebelDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("SiebelDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
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

