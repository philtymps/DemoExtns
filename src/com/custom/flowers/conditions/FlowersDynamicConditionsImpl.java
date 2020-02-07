/**
  * FlowersDynamicConditionsImpl.java
  *
  **/

// PACKAGE
package com.custom.flowers.conditions;

import com.custom.yantra.util.*;
import com.yantra.ycp.japi.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.*;

import java.util.*;
import org.w3c.dom.*;

public class FlowersDynamicConditionsImpl implements YCPDynamicCondition, YCPDynamicConditionEx
{
    public FlowersDynamicConditionsImpl()
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
				System.out.println ("\nFlowers Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				YFSUtil.dumpMapData (mapData);
			}
			// if "IsCreateRequest" (expects an Siebel Order Interface document)
			if (name.equalsIgnoreCase ("Is Drop Ship User"))
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument	docUserDetails = YFCDocument.getDocumentFor ("<User Loginid=\""+env.getUserId ()+"\"/>");
				docUserDetails = YFCDocument.getDocumentFor (api.getUserHierarchy (env, docUserDetails.getDocument()));
				YFCElement	eleUserDetails = docUserDetails.getDocumentElement ();
				YFCElement	eleGroupLists = eleUserDetails.getChildElement ("UserGroupLists");
				if (eleGroupLists != null)
				{
					Iterator	iGroupLists = eleGroupLists.getChildren();
					while (!bRet && iGroupLists.hasNext ())
					{
						YFCElement	eleGroupList = (YFCElement)iGroupLists.next();
						Iterator	iGroups = eleGroupList.getChildren ();
						while (!bRet && iGroups.hasNext ())
						{
							YFCElement	eleGroup = (YFCElement)iGroups.next();
							String	sUserGroupId = eleGroup.getAttribute ("UsergroupId");
							if (sUserGroupId != null && sUserGroupId.equals("DROP_SHIPPER"))
								bRet = true;
						}
					}
				}
			}	

			if (YFSUtil.getDebug())
			{
				System.out.println ("Flowers Dynamic Condition Results");
				System.out.println ("bRet="+bRet);
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in FlowersDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("FlowersDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}		
		return bRet;
	}	
	
	@SuppressWarnings("rawtypes")
	public void setProperties(Map props)
	{
			m_props = props;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private Map	m_props;

}
