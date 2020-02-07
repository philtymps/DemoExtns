/**
  * DendreonBeforeCreateWorkOrderUEImpl.java
  *
  **/

// PACKAGE
package com.custom.dendreon.ue;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.YFSException;
import com.yantra.vas.japi.ue.*;
import com.yantra.yfc.util.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;
import com.custom.yantra.util.*;

import java.util.*;

public class DendreonBeforeCreateWorkOrderUEImpl implements VASBeforeCreateWorkOrderUE 
{
    public DendreonBeforeCreateWorkOrderUEImpl()
    {
    }

	public String beforeCreateWorkOrder(YFSEnvironment env, String inXML)
                                       throws YFSUserExitException
	{
		try {
			return YFSXMLUtil.getXMLString (beforeCreateWorkOrder (env, YFCDocument.createDocument (inXML).getDocument()));
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	}
	
	public Document beforeCreateWorkOrder(YFSEnvironment env, Document inXML)
                                       throws YFSUserExitException
	{
	  try {
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeCreateWorkOrder() UE is: ");
				System.out.println (YFSXMLUtil.getXMLString (inXML));
		}
		YFCDocument	docWorkOrder = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleWorkOrder = docWorkOrder.getDocumentElement();
		
		YFCDocument	docOrder = YFCDocument.getDocumentFor (eleWorkOrder.getChildElement("Order").getString());
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("getOrderDetails API Input: ");
			System.out.println (docOrder.getString());
		}
		docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
		eleOrder = docOrder.getDocumentElement();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("getOrderDetails API Output: ");
			System.out.println (docOrder.getString());
		}

		// if work order is for an APHERESIS procedure
		if (IsWorkOrderForProvidedService(env, "APHERESIS", eleWorkOrder, eleOrder))
			// compute the "suggested" appointment date using the "expected" apheresis start & end date
			computeAppointmentDate (env, eleWorkOrder, eleOrder);
		
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from beforeCreateOrder() UE is: ");
			System.out.println (docWorkOrder.getString());
		}
		return docWorkOrder.getDocument();
	  } 
	  catch (YFSException e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }		
	  catch (Exception e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }
	}

	@SuppressWarnings("rawtypes")
	private boolean IsWorkOrderForProvidedService (YFSEnvironment env, String sItemID, YFCElement eleWorkOrder, YFCElement eleOrder)
	{
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		YFCElement	eleWorkOrderServiceLines = eleWorkOrder.getChildElement ("WorkOrderServiceLines");
		String		sWorkOrderServiceLineKey = "";
		boolean		bIsWorkOrderForProvidedService = false;
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering IsWorkOrderForProvidedService");
		}
		// get the work order's service line key
		if (eleWorkOrderServiceLines != null)
		{
			for (Iterator iWorkOrderServiceLines = eleWorkOrderServiceLines.getChildren(); iWorkOrderServiceLines.hasNext(); )
			{
				YFCElement	eleWorkOrderServiceLine = (YFCElement)iWorkOrderServiceLines.next();
				YFCElement	eleOrderLine = eleWorkOrderServiceLine.getChildElement ("OrderLine");

				if (eleOrderLine != null)
				{
					sWorkOrderServiceLineKey = eleOrderLine.getAttribute ("OrderLineKey");
					if (YFSUtil.getDebug())
					{
						System.out.println ("Work Order Service Line Key="+sWorkOrderServiceLineKey);
					}
					break;
				}
			}
		}	
		else
			return bIsWorkOrderForProvidedService;

		if (YFSUtil.getDebug())
		{
			System.out.println ("Searching for Corresponding OrderLineKey:" + sWorkOrderServiceLineKey);
		}

		// now search order for corresponding service line
		if (eleOrderLines != null)
		{
			for (Iterator iOrderLines = eleOrderLines.getChildren (); iOrderLines.hasNext(); )
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
				if (YFSUtil.getDebug())
				{
					System.out.println ("OrderLineKey=" + eleOrderLine.getAttribute ("OrderLineKey"));
					System.out.println ("ItemGroupCode=" + eleOrderLine.getAttribute ("ItemGroupCode"));
					System.out.println ("ItemID=" + eleItem.getAttribute ("ItemID"));
				}
				if (eleOrderLine.getAttribute ("OrderLineKey").equals(sWorkOrderServiceLineKey))
				{								
					String		sItemGroupCode = eleOrderLine.getAttribute("ItemGroupCode");
					if (sItemGroupCode != null && sItemGroupCode.equals ("PS"))
					{
						if (eleItem != null)
						{
							if (sItemID.equals (eleItem.getAttribute ("ItemID")))
							{
								bIsWorkOrderForProvidedService = true;
								break;
							}
						}
					}
				}
			}	
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting IsWorkOrderForProvidedService");
		}
		return bIsWorkOrderForProvidedService;
	}	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	private void computeAppointmentDate (YFSEnvironment env, YFCElement eleWorkOrder, YFCElement eleOrder)
	{
		YFCElement	eleOrderDates = eleOrder.getChildElement ("OrderDates");
		YFCDate		dtAphApptStart = null, dtAphApptEnd = null;
		YFCDate		dtExpectedStart = null, dtExpectedEnd = null;
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering computeAppointmentDate: ");
			if (eleOrderDates != null)
				System.out.println ("Order Dates Found: " + eleOrderDates.getString());
			else
				System.out.println ("WARNING-No Order Dates Found");
			
		}
				
		if (eleOrderDates == null)
			return;

		for (Iterator iOrderDates = eleOrderDates.getChildren(); iOrderDates.hasNext(); )
		{
			YFCElement	eleOrderDate = (YFCElement)iOrderDates.next();
			String		sDateType = eleOrderDate.getAttribute ("DateTypeId");
			
			if (sDateType.equals ("AphStartDateTime"))
			{
				dtExpectedStart = eleOrderDate.getDateTimeAttribute ("ExpectedDate");				
				dtAphApptStart = new YFCDate(dtExpectedStart, true);
			}
			else if (sDateType.equals ("AphEndDateTime"))
			{
				dtExpectedEnd = eleOrderDate.getDateTimeAttribute ("ExpectedDate");				
				dtAphApptEnd = new YFCDate (dtExpectedEnd, true);
			}
		}
		if (dtAphApptStart != null && dtAphApptEnd != null)
		{
			// add the "suggested" appointment to the work order
			YFCElement	eleWorkOrderAppointments = eleWorkOrder.createChild ("WorkOrderAppointments");
			YFCElement	eleWorkOrderAppointment = eleWorkOrderAppointments.createChild("WorkOrderAppointment");
							
			int iHour = dtExpectedStart.getHours();
			// appointments are scheduled in two slots of 9-12 or 1-4
			if (iHour <= 12)
			{
				dtAphApptStart.setHours(9);
				dtAphApptEnd.setHours(12);
			}
			else
			{
				dtAphApptStart.setHours(13);
				dtAphApptEnd.setHours(17);
			}
	
			eleWorkOrder.setDateTimeAttribute ("StartNoEarlierThan", dtExpectedStart);
			eleWorkOrder.setDateTimeAttribute ("FinishNoLaterThan", dtExpectedEnd);
			eleWorkOrderAppointment.setDateTimeAttribute ("PromisedApptStartDate", dtAphApptStart);
			eleWorkOrderAppointment.setDateTimeAttribute ("PromisedApptEndDate", dtAphApptEnd);
			eleWorkOrderAppointment.setAttribute ("RequestedQuantity", "1");
			eleWorkOrderAppointment.setAttribute ("IsAppointmentOverridden", "N");
		}
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting computeAppointmentDate: ");
		}
		return;
	}
}

