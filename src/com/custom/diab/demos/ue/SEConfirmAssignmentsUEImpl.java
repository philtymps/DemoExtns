package com.custom.diab.demos.ue;

import org.w3c.dom.Document;

import java.util.Hashtable;
import java.util.Iterator;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCDate;


import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSConfirmAssignmentsUE;

@SuppressWarnings("deprecation")
public class SEConfirmAssignmentsUEImpl implements YFSConfirmAssignmentsUE {


	public Document confirmAssignments(YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		String		sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
		Hashtable<String, ?>	htTruckCapacities = new Hashtable<String, Object>();
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to SEConfirmAssignmentsUEImpl:");
			System.out.println (docOrder.getString());
		}

		// only do this on RELEASE.0001 transaction
		if (!docIn.getDocumentElement().getAttribute("TransactionId").equals("RELEASE.0001")) {
			return docIn;
		}
		
		Iterator<?>	iOrderLines = eleOrderLines.getChildren();
		YFCDocument	docAssignments = YFCDocument.createDocument("Order");
		YFCElement	eleAssignments = docAssignments.getDocumentElement();
		eleAssignments.setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey"));
		YFCElement	eleAssignedLines = eleAssignments.createChild("OrderLines");
		boolean	bRejectAssignments = false;
		Hashtable<String, String>	htRemainingCapacitiesByNode;
		Hashtable<String, Hashtable<String, String>>	htRemainingCapacitiesByShipToIDs = new Hashtable<String, Hashtable<String, String>>();
		
		// any rejected order lines should be immediately available for release
		YFCDate dtNextScheduleDate = new YFCDate ();
		dtNextScheduleDate.changeDate(-1);
		

		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			YFCElement  eleSchedules = eleOrderLine.getChildElement("Schedules");
			Iterator<?>	iSchedules = eleSchedules.getChildren();
			String		sShipToID  = eleOrderLine.getAttribute ("ReceivingNode");
	
			if (YFCObject.isVoid(sShipToID))
				sShipToID = eleOrderLine.getAttribute("ShipToID");

			if (YFCObject.isVoid(sShipToID))
				sShipToID = "UNKNOWN";

			// create one Hashtable of capacities for each ShipToID we encounter
			if ((htRemainingCapacitiesByNode = htRemainingCapacitiesByShipToIDs.get(sShipToID)) == null)
				htRemainingCapacitiesByShipToIDs.put(sShipToID, htRemainingCapacitiesByNode = new Hashtable<String, String>());
				
			// get the truck capacities by node for all nodes configured with capacity
			getTruckCapacities (env, sEnterpriseCode, htTruckCapacities);
			

			YFCElement  eleAssOrderLine = eleAssignedLines.createChild("OrderLine");
			eleAssOrderLine.setAttribute("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
			YFCElement	eleAssSchedules = eleAssOrderLine.createChild("Schedules");
			
			// determine the unit weight of the item using ItemWeight attribute
			double		dblUnitWeight = eleOrderLine.getChildElement("Item").getDoubleAttribute("ItemWeight");
			
			while (iSchedules.hasNext())
			{
				YFCElement	eleSchedule = (YFCElement)iSchedules.next();
				boolean		bCapacitiesMissing = false;

				// only process those schedules with a ScheduleId
				if (!eleSchedule.hasAttribute("ScheduleId"))
					continue;

				// get schedule's assigned qty by node and decrement total node capacity at each node by that qty * unit weight
				bCapacitiesMissing = getRemainingTruckCapacitiesByNode (env, eleSchedule, sShipToID, htTruckCapacities, htRemainingCapacitiesByNode, dblUnitWeight);

				YFCElement	eleShipNodes = eleSchedule.getChildElement("ShipNodes");
				Iterator<?>	iShipNodes = eleShipNodes.getChildren();
				while (iShipNodes.hasNext())
				{
					YFCElement	eleShipNode = (YFCElement)iShipNodes.next();
					String sRemainingCapacity = htRemainingCapacitiesByNode.get(eleShipNode.getAttribute("ShipNode"));
					if (!bCapacitiesMissing && Double.parseDouble(sRemainingCapacity) < 0)
					{
						// restore old capacity back to ship node
						Double		dblAssignedQty = new Double(eleShipNode.getDoubleAttribute ("AssignedQty"));
						double		dblAdditionalCapacity = dblAssignedQty.doubleValue() * dblUnitWeight;
						double		dblPriorTotalTruckCapacity = new Double(sRemainingCapacity).doubleValue() + dblAdditionalCapacity;
						htRemainingCapacitiesByNode.put(eleShipNode.getAttribute("ShipNode"), new Double(dblPriorTotalTruckCapacity).toString());
						eleAssignments.setAttribute("NextScheduleDate", dtNextScheduleDate.getString(YFCDate.ISO_DATETIME_FORMAT));
						bRejectAssignments = true;
					}						
				}
				
				YFCElement	eleAssSchedule = eleAssSchedules.createChild("Schedule");
				eleAssSchedule.setAttribute("ScheduleId", eleSchedule.getAttribute ("ScheduleId"));
				eleAssSchedule.setAttribute("RejectAssignment", bRejectAssignments ? "true" : "false");
			}
			bRejectAssignments = false;
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from SEConfirmAssignmentsUEImpl:");
			System.out.println (docAssignments.getString());
		}

		return docAssignments.getDocument();
	}

	
	protected	boolean	getRemainingTruckCapacitiesByNode(YFSEnvironment env, YFCElement eleSchedule, String sShipToID, Hashtable<String, ?> htTruckCapacities, Hashtable<String, String> htRemainingTruckCapacitiesByNode, double dblUnitWeight)
	{
		double		dblTotalTruckCapacity = 0;
		boolean		bCapacitiesMissing = false;
		
		YFCElement	eleShipNodes = eleSchedule.getChildElement("ShipNodes");
		Iterator<?>	iShipNodes = eleShipNodes.getChildren();

		// iterate over all of the ship nodes in the Schedule
		while (iShipNodes.hasNext())
		{
			YFCElement	eleShipNode = (YFCElement)iShipNodes.next();
			String		sShipNode = eleShipNode.getAttribute("ShipNode");
			String		sCapacity;
			Double		dblAssignedQty = new Double(eleShipNode.getDoubleAttribute ("AssignedQty"));
			
			if ((sCapacity = htRemainingTruckCapacitiesByNode.get(sShipNode)) == null)
			{
				dblTotalTruckCapacity = getTruckMaxCapacity(htTruckCapacities, sShipNode);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Initial Truck Capacity for Ship Node " + sShipNode + " Going To " + sShipToID + " = " + dblTotalTruckCapacity);
					System.out.println ("-1 Capacity indicates Truck Capacity for Node Not Configured in Common Code Table");
				}
				if (dblTotalTruckCapacity >= 0)
					dblTotalTruckCapacity -= dblAssignedQty.doubleValue() * dblUnitWeight;
				else
					bCapacitiesMissing = true;
			}
			else
			{
				dblTotalTruckCapacity = Double.parseDouble(sCapacity);
				if (dblTotalTruckCapacity >= 0)
					dblTotalTruckCapacity -= dblAssignedQty.doubleValue() * dblUnitWeight;
			}
			if (YFSUtil.getDebug())
			{
				System.out.println ("Consumed "+ dblAssignedQty.doubleValue() * dblUnitWeight + " LBS at Ship Node " + sShipNode + " Going To " + sShipToID );
				System.out.println ("Remaining Truck Capacity for Ship Node: " + sShipNode + " Going To " + sShipToID + " = " + dblTotalTruckCapacity);
				if (dblTotalTruckCapacity < 0)
					System.out.println ("Truck Capacity Exceeded - Removing " + dblAssignedQty.doubleValue() * dblUnitWeight + " LBS at Ship Node " + sShipNode);
			}

			// subtract the assigned qty from truck capacity to establish a remaining capacity
			htRemainingTruckCapacitiesByNode.put(sShipNode, Double.toString(dblTotalTruckCapacity));
		}
		return bCapacitiesMissing;
	}
	
	protected		double getTruckMinCapacity (Hashtable<?, ?> htTruckCapacities, String sShipNode)
	{
		String	sCapacity = (String)htTruckCapacities.get(sShipNode);
		
		if (sCapacity != null)
		{
			Double dblMaxCapacity = new Double(sCapacity.substring(0, sCapacity.indexOf("-")));
			return (dblMaxCapacity.doubleValue());
		}
		else
			return -1;
	}

	protected		double getTruckMaxCapacity (Hashtable<String, ?> htTruckCapacities, String sShipNode)
	{
		String	sCapacity = (String)htTruckCapacities.get(sShipNode);
		if (sCapacity != null)
		{
			Double dblMaxCapacity = new Double(sCapacity.substring(sCapacity.indexOf("-")+1, sCapacity.length()));
			return (dblMaxCapacity.doubleValue());
		}
		else
			return -1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String	getTruckCapacities (YFSEnvironment env, String sEnterpriseCode, Hashtable htTruckCapacities) 
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		
		eleCommonCode.setAttribute ("CodeType", "CPG_TRUCK_CAPACITY");
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception eIgnore) {
			return null;
		}
		if (!YFCObject.isNull(eleCommonCodes))
		{
			Iterator<?>	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{
				eleCommonCode = (YFCElement)iCommonCodes.next();
				// store capacities by node in the Hashtable as "Min-Max"
				htTruckCapacities.put(eleCommonCode.getAttribute("CodeValue"), eleCommonCode.getAttribute("CodeShortDescription") + "-" + eleCommonCode.getAttribute("CodeLongDescription"));
			}
		}
		return null;
	}
}
