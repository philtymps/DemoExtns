/**
  * AT21BeforeCreateOrderUEImpl.java
  *
  **/

// PACKAGE
package com.custom.at21.ue;

import com.custom.yantra.util.*;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.*;
import com.yantra.yfc.util.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;

public class AT21BeforeCreateOrderUEImpl implements YFSBeforeCreateOrderUE
{
    public AT21BeforeCreateOrderUEImpl()
    {
    }
	
	public String beforeCreateOrder(YFSEnvironment env, String inXML)
                                       throws YFSUserExitException
	{
		return YFCDocument.getDocumentFor (beforeCreateOrder (env, YFCDocument.getDocumentFor (inXML).getDocument())).getString();
	}
	
	@SuppressWarnings("deprecation")
	public Document beforeCreateOrder(YFSEnvironment env, Document inXML)
                                       throws YFSUserExitException
	{

	  try {
		YFCDocument	docOrder = YFCDocument.getDocumentFor (inXML);
/*
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeCreateOrder() API is: ");
				System.out.println (docOrder.getString());
		}
*/		
		YFCElement	eleOrder = docOrder.getDocumentElement ();
/*
		// NOTE:
		// This code MOVED into finishOrder() of the TmsInterfaceApiImpl code
		
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		BigDecimal	bdWeightTotal = new BigDecimal ("0.00");
		if (eleOrderLines != null)
		{
			Iterator	iOrderLines = eleOrderLines.getChildren ();
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				YFCElement	eleOrderLineReferences = eleOrderLine.getChildElement ("References");
				if (eleOrderLineReferences != null)
				{
					Iterator	iReferences = eleOrderLineReferences.getChildren ();
					while (iReferences.hasNext())
					{
						YFCElement	eleReference = (YFCElement)iReferences.next();
						if (eleReference.getAttribute ("Name").equalsIgnoreCase ("EXPECTED_WEIGHT"))
						{
							String	sExpectedWeight = eleReference.getAttribute ("Value");
							if (sExpectedWeight != null)
							{
								bdWeightTotal = bdWeightTotal.add (new BigDecimal (sExpectedWeight));
							}
							break;
						}
					}
				}
			}
		}

		// add total weight of all lines to header reference field
		bdWeightTotal = bdWeightTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
		if (bdWeightTotal.compareTo (new BigDecimal ("0.00")) != 0)
		{
			YFCElement	eleOrderReferences = eleOrder.getChildElement ("References");
			if (eleOrderReferences == null)
				eleOrderReferences = eleOrder.createChild ("References");
			YFCElement	eleOrderReference = eleOrderReferences.createChild ("Reference");
			eleOrderReference.setAttribute ("Name", "EXPECTED_WEIGHT");
			eleOrderReference.setAttribute ("Value", bdWeightTotal.toString ());
		}
		// set search criteria to 'Y' if weight exceeds 1300
		if (bdWeightTotal.compareTo (new BigDecimal ("1300.00")) > 0)
			eleOrder.setAttribute ("SearchCriteria1", "Y");
		else
			eleOrder.setAttribute ("SearchCriteria1", "N");
		
		
*/
		if (YFSUtil.getDebug())
		{
			// System.out.println ("Output from beforeCreateOrder() API is: ");
			// System.out.println (docOrder.getString());
			System.out.println ("Creating Order No: " + eleOrder.getAttribute ("OrderNo") + " at " + new YFCDate().getString ());
		}
		return docOrder.getDocument();
	  } catch (Exception e) {
		throw new YFSUserExitException (e.getMessage());
	  }		
	}	
}
