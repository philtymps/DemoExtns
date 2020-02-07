/**
  * BankDynamicConditions.java
  *
  **/

// PACKAGE
package com.custom.boa.conditions;

import com.yantra.ycp.japi.*;
import java.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;
import com.custom.yantra.util.*; 
import org.w3c.dom.*;

public class BankDynamicConditions implements YCPDynamicCondition, YCPDynamicConditionEx 
{
    public BankDynamicConditions()
    {

    }

	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document xmlData)
	{
		boolean	bRet = false;
		try {

			// The line below threw and exception when dynamic condition used inside
			// a pipeline.  Is this a doc bug?
			//YFCDocument xml = YFCDocument.getDocumentFor(xmlData);
			if (YFSUtil.getDebug())
			{					
				System.out.println ("Bank Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+YFCDocument.getDocumentFor (xmlData).getString());
				YFSUtil.dumpMapData (mapData);
			}
			if (name.startsWith ("Is New Communications Profile"))
			{
				YFCDocument	docEXCommProfile = YFCDocument.getDocumentFor (xmlData);
				YFCElement	eleEXCommProfile = docEXCommProfile.getDocumentElement ();
				String sCommProfileKey = eleEXCommProfile.getAttribute ("CommProfileKey");
				bRet = sCommProfileKey == null || sCommProfileKey.length() == 0;
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in BankDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("BANKDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}		
		return bRet;
	}

  	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, String xmlData)
	{
		return evaluateCondition (env, name, mapData, YFCDocument.getDocumentFor (xmlData).getDocument());
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

