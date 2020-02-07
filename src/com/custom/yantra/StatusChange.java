/**
  * StatusChange.java
  *
  **/

/*
1)	This code Prints the input parameters that evaluate condtion gets from Yantra Application.
2)	Checks to see if the condition name that invokes this dynamic condition is "IsQueryPending".(That implies that this code can handle more than 1 dynamic conditions configured in the configurator).
3)	Returns true if condition name is IsQueryPending and base drop status is "1100.1000" and false otherwise.

*/
package com.custom.yantra;

import java.util.*; 
import org.w3c.dom.*;
import com.yantra.yfs.japi.*; 
import com.yantra.ycp.japi.*; 
import com.custom.yantra.util.*;

public class StatusChange implements YCPDynamicCondition
{
	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, String xmlData) {
		try
		{
			Set keys = mapData.keySet();
			Iterator iter = keys.iterator();

			System.out.println( "printing map data ...");
			while (iter.hasNext())
			{
				String key = (String)iter.next();
				String val = (String)mapData.get(key);
				System.out.println( key + " : " + val );
			}//while
			System.out.println( "map data end ... ");

			System.out.println( "printing xml data ..." );
			System.out.println( xmlData );

			//Here I am using my own Util classes to parse the XML.
			//The Util classes are in Appendix D for your reference.
YFSXMLParser parser      = new YFSXMLParser(xmlData, false);
			Element	eOrderStatus   = parser.getRootElement();
			NodeList lstOrderLines     = eOrderStatus.getElementsByTagName( "OrderLines" );
			Element eOrderLines       = (Element)lstOrderLines.item(0);
			NodeList lstOrderLine      = eOrderLines.getElementsByTagName( "OrderLine" );
			Element eOrderLine         = (Element)lstOrderLine.item(0);
			String baseDropStatus      = eOrderLine.getAttribute( "BaseDropStatus" );	
			
			if (name.trim().equals("IsQueryPending"))
			{
				if (baseDropStatus.equals("1100.1000"))
					return true;
				
			}//if condition, IsQueryPending

			return false;
		}//try
		catch (Exception ex)
		{
			throw new RuntimeException("Dynamic condition failed.");
		}//catch
	}//evaluateCondition

};//StatusChange


