package com.custom.diab.demos.api;

import java.util.Properties;
import java.math.BigDecimal;
import java.util.Iterator;

import com.custom.diab.demos.api.SEFairShareAllocation.OrderLineReservation;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import org.w3c.dom.Document;

public class CPGGetCustomerItemDemand implements YIFCustomApi {

	private Properties	m_Props;
	
	public CPGGetCustomerItemDemand() {
		// TODO Auto-generated constructor stub
	}
	
	public Document getCustomerItemDemand (YFSEnvironment env, Document docIn) throws YFSException
	{
		YFCDocument	docCustomerItem = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleCustomerItemToGetDemandFor = docCustomerItem.getDocumentElement();
		String		sOrganizationCode = eleCustomerItemToGetDemandFor.getAttribute("OrganizationCode");
		String		sCustomerID = eleCustomerItemToGetDemandFor.getAttribute("CustomerID");
		String		sItemID = eleCustomerItemToGetDemandFor.getAttribute("ItemID");
		String		sUnitOfMeasure = eleCustomerItemToGetDemandFor.getAttribute("UnitOfMeasure");
		double		dblRequestedQty = eleCustomerItemToGetDemandFor.getDoubleAttribute("RequestedQty");

		String		sFileName="/var/oms/Scripts/SampleCustomerItemDemand.xml";

		YFCDocument docCustomerItemDemand = YFCDocument.getDocumentForXMLFile (sFileName);
		YFCElement	eleCustomerItemDemand = docCustomerItemDemand.getDocumentElement();
		
		// Default NormalDemand for any Customer/Item = 0
		YFCDocument	docOut = YFCDocument.getDocumentFor("<Item OrganizationCode=\"" + sOrganizationCode + "\"" +
				 										" ItemID=\"" + sItemID + "\"" + 
														" UnitOfMeasure=\"" + sUnitOfMeasure + "\"" +
														" RequestedQty=\"" + dblRequestedQty + "\"" +
														" SplitQty=\"" + -dblRequestedQty + "\"" +  
														" NormalDemand=\"0\"" + "/>");
		Iterator	iCustomers = eleCustomerItemDemand.getChildElement("Customers").getChildren();
		while (iCustomers.hasNext())
		{
			YFCElement	eleCustomer = (YFCElement)iCustomers.next();
			if (eleCustomer.getAttribute("CustomerID").equals(sCustomerID))
			{
				Iterator iItems = eleCustomer.getChildElement("Items").getChildren();
				while (iItems.hasNext())
				{
					YFCElement	eleItem = (YFCElement)iItems.next();
					if (eleItem.getAttribute("ItemID").equals(sItemID) && eleItem.getAttribute("UnitOfMeasure").equals(sUnitOfMeasure))
					{
						double  dblNormalDemandQty = eleItem.getDoubleAttribute("NormalDemand");
						double  dblSplitQty = dblRequestedQty - dblNormalDemandQty;
						eleItem.setDoubleAttribute("RequestedQty", dblRequestedQty);
						eleItem.setDoubleAttribute("SplitQty", dblSplitQty);
						docOut = YFCDocument.getDocumentFor(eleItem.getString());
						return docOut.getDocument();
					}
				}
			}
		}
		return docOut.getDocument();
	}

	public Document scheduleNormalDemands(YFSEnvironment env, Document docIn) throws YFSException
	{
		// TODO Auto-generated method stub
		YFCDocument docOrder = YFCDocument.getDocumentFor(docIn);		
		System.out.println ("Input to ScheduleNormalDemands:");
		System.out.println (docOrder.getString());
		YIFApi	api = null;
		try {
			api = YFSUtil.getYIFApi ();
			docOrder = YFCDocument.getDocumentFor(api.getOrderDetails (env, docIn));
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		
		System.out.println ("Output from getOrderDetails:");
		System.out.println (docOrder.getString());

		YFCElement				eleOrder = docOrder.getDocumentElement(); 
		YFCElement				eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator<YFCElement>	iOrderLines = eleOrderLines.getChildren();
		YFCDocument				docCancelOrder = YFCDocument.createDocument ("Order");
		YFCElement				eleCancelOrder = docCancelOrder.getDocumentElement();
		YFCElement				eleCancelOrderLines = eleCancelOrder.createChild("OrderLines");
		YFCDocument				docScheduleOrder = YFCDocument.createDocument("Promise");
		YFCElement				eleScheduleOrder = docScheduleOrder.getDocumentElement();
		YFCElement				elePromiseLines = eleScheduleOrder.createChild("PromiseLines");
		YFCDocument				docChangeOrder = YFCDocument.createDocument ("Order");
		YFCElement				eleChangeOrder = docChangeOrder.getDocumentElement();
		YFCElement				eleChangeOrderLines = eleChangeOrder.createChild("OrderLines");
		boolean					bScheduleOrder = false;
		boolean 				bCancelOrder = false;
		boolean					bScheduleAndRelease = false;
		boolean 				bIsFillIn = false;
		boolean					bIsPriorityCustomer = false;
		boolean 				bIsUnusualDemand = false;

		// calculate lead time (optional arg passed to API)
		String		sLeadTime = m_Props.getProperty("LeadTime");
		int			iLeadTime = 0;
		if (!YFCObject.isVoid(sLeadTime))
			iLeadTime = Integer.parseInt(sLeadTime);
		else
			iLeadTime = 10;
		String		sVariance = m_Props.getProperty("Variance");
		double		dblVariance = 0;
		if (!YFCObject.isVoid(sVariance))
			dblVariance = new BigDecimal(sVariance).doubleValue();
		else
			dblVariance = .10;
		
		// DOT Use Case reserve if normal demand and > 10 days lead time
		// Determine if Order is to be Delivered in more than 10 days
		bIsPriorityCustomer = (getCustomerLevel(env, eleOrder).compareTo(m_Props.getProperty("PriorityTierMax")) <= 0);
		System.out.println ("bIsPriorityCustomer=" + bIsPriorityCustomer);
		YFCDate		dtReqDeliveryDate;
		YFCDate		dtCurrentDate = new YFCDate();
		
		if (YFCObject.isVoid(eleOrder.getAttribute("ReqDeliveryDate")))
			dtReqDeliveryDate = dtCurrentDate;
		else
			dtReqDeliveryDate = eleOrder.getDateAttribute("ReqDeliveryDate");
		
		System.out.println ("Lead Time until Delivery: " + dtCurrentDate.diffDays(dtReqDeliveryDate) +" Days");
		System.out.println ("Lead Days Required: " + sLeadTime);
		System.out.println ("Variance Allowed: " + sVariance);
		if (dtCurrentDate.diffDays(dtReqDeliveryDate) > iLeadTime)
			bScheduleAndRelease = true;

		String	sScheduledOrReleased = bScheduleAndRelease ? "Released" : "Scheduled";
		System.out.println ("bSchedulAndRelease=" + bScheduleAndRelease);
		System.out.println ("Order " + eleOrder.getAttribute("OrderNo") + " is Elidgible to be " + sScheduledOrReleased + " Immediately");
		eleScheduleOrder.setAttribute("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
		eleCancelOrder.setAttribute ("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
		eleCancelOrder.setAttribute ("ModificationReasonCode", "EARLYSCHEDULING");

		// soft allocate or cancel fill-in's 
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = iOrderLines.next();
			YFCElement	eleItem = eleOrderLine.getChildElement("Item");
			YFCDocument	docGetCustomerItemDemand = YFCDocument.createDocument("GetCustomerItemDemand");
			YFCElement	eleGetCustomerItemDemand = docGetCustomerItemDemand.getDocumentElement();
			eleGetCustomerItemDemand.setAttribute("OrganizationCode", eleOrder.getAttribute("EnterpriseCode"));
			eleGetCustomerItemDemand.setAttribute("ItemID", eleItem.getAttribute("ItemID"));
			eleGetCustomerItemDemand.setAttribute("UnitOfMeasure", eleItem.getAttribute("UnitOfMeasure"));
			if (!YFCObject.isVoid(eleOrder.getAttribute("CustomerID")))
				eleGetCustomerItemDemand.setAttribute("CustomerID", eleOrder.getAttribute("CustomerID"));
			else
				eleGetCustomerItemDemand.setAttribute("CustomerID",  eleOrder.getAttribute("BillToID"));
			eleGetCustomerItemDemand.setAttribute("RequestedQty", eleOrderLine.getAttribute("OrderedQty"));
			
			System.out.println ("Input to GetCustomerItemDemand:");
			System.out.println (docGetCustomerItemDemand.getString());
			docGetCustomerItemDemand = YFCDocument.getDocumentFor(getCustomerItemDemand(env, docGetCustomerItemDemand.getDocument()));
			eleGetCustomerItemDemand = docGetCustomerItemDemand.getDocumentElement();
			System.out.println ("Output from GetCustomerItemDemand:");
			System.out.println (docGetCustomerItemDemand.getString());

			double	dblRequestedQty = eleGetCustomerItemDemand.getDoubleAttribute("RequestedQty");
			double  dblNormalQty = eleGetCustomerItemDemand.getDoubleAttribute("NormalDemand");

			// if Requested < Normal use Requested vs. Normal (lower of the two)
			if (dblRequestedQty < dblNormalQty)
				dblNormalQty = dblRequestedQty;

			eleGetCustomerItemDemand.setAttribute("OrganizationCode", eleOrder.getAttribute("EnterpriseCode"));
			double	dblAvailableQty = getAvailability (env, eleGetCustomerItemDemand);
			
			// if fill in order (i.e. never ordered before)
			if (dblNormalQty == 0)
			{
				bIsFillIn = true;
				bScheduleOrder = false;
			}
			// determine if this is "unusual demand" - demand > x% of normal demand
			bIsUnusualDemand = (!bIsFillIn && dblAvailableQty >= dblRequestedQty && dblRequestedQty > (dblNormalQty + dblNormalQty * dblVariance));
			double dblScheduleQty = (bScheduleAndRelease || bIsPriorityCustomer || dblNormalQty == dblRequestedQty) ? dblRequestedQty : bIsFillIn ? dblRequestedQty : dblNormalQty;
			
			
			if (!bIsFillIn  && dblAvailableQty >= dblScheduleQty 
			|| bIsUnusualDemand
			|| bIsPriorityCustomer)
			{
				System.out.println ("Order Line eleidgible for early scheduling " + eleItem.getAttribute("ItemID") + " because " + (bIsPriorityCustomer ? " Customer Meets Priority Level " : bIsUnusualDemand ? "Unusual Demand Requested but Inventory is Sufficient to Cover" : "Order Qty within " + dblVariance*100 + "% of Normal Demand or the Normal Demand does not exceed available qty"));
				if (bScheduleAndRelease || bIsPriorityCustomer || dblNormalQty == dblRequestedQty)
					System.out.println ("Allocating Requested Qty: " + dblRequestedQty);
				else
					System.out.println ("Allocating Normal Qty: " + dblNormalQty);
				YFCElement	elePromiseLine = elePromiseLines.createChild("PromiseLine");
				elePromiseLine.setAttribute ("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
				elePromiseLine.setDoubleAttribute ("Quantity", dblScheduleQty);
				elePromiseLine.setDateAttribute ("DeliveryDate", dtReqDeliveryDate);
				elePromiseLine.setAttribute ("ShipNode", eleGetCustomerItemDemand.getAttribute ("ShipNode"));
				elePromiseLine.setAttribute ("FromStatus", "1100");
				bScheduleOrder = true;
			}
			else if (bIsFillIn && dblAvailableQty < dblScheduleQty)
			{
				System.out.println ("Fill In has Insufficient Quantity Available for Item " + eleItem.getAttribute("ItemID") + ". Line will be Cancelled");
				YFCElement	eleCancelOrderLine = eleCancelOrderLines.createChild("OrderLine");
				eleCancelOrderLine.setAttribute ("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
				eleCancelOrderLine.setDoubleAttribute("QuantityToCancel", dblRequestedQty);
				eleCancelOrderLine.setAttribute ("Action", "CANCEL");
				// manageItemBasedAllocation (env, eleGetCustomerItemDemand);
				bCancelOrder = true;
			}
			else
			{
				YFCElement	eleChangeOrderLine = eleChangeOrderLines.createChild("OrderLine");
				YTimestamp	dtNextScheduleDate = YTimestamp.newMutableTimestamp (System.currentTimeMillis());
				dtNextScheduleDate.addHours(72);
				eleChangeOrderLine.setAttribute("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
				eleChangeOrderLine.setDateTimeAttribute("EarliestScheduleDate", dtNextScheduleDate);
				System.out.println ("Fill In Order Line NOT eleidgible for early scheduling - Order Line " + eleItem.getAttribute("ItemID") + " will not be scheduled until " + dtNextScheduleDate.getString());
			}
			
		}
		if (bScheduleOrder) {
			try {
				eleScheduleOrder.setAttribute ("IgnoreReleaseDate", "Y");
				eleScheduleOrder.setAttribute ("ScheduleAndRelease", bScheduleAndRelease ? "Y" : "N");
				System.out.println ("Input to scheduleOrderLines API:");
				System.out.println (docScheduleOrder.getString());
				api.scheduleOrderLines (env, docScheduleOrder.getDocument());
			} catch (Exception e) {
				throw new YFSException (e.getMessage());
			}			
		}
		if (bCancelOrder) {
			try {
				// mark order as AT RISK
				System.out.println ("Creating ORDER_AT_RISK Alert");
				api.executeFlow(env, "CPG_OnEarlyScheduleFailed", docOrder.getDocument());

				System.out.println ("Input to cancelOrder API:");
				System.out.println (docCancelOrder.getString());
				api.cancelOrder (env, docCancelOrder.getDocument());

			} catch (Exception e) {
				throw new YFSException (e.getMessage());
			}			
		}
		else if (bIsFillIn)
		{
			try {
				eleChangeOrder.setAttribute ("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
				eleChangeOrder.setAttribute ("PriorityCode", "TIER_6");
				api.changeOrder(env, docChangeOrder.getDocument());
			} catch (Exception e) {
				throw new YFSException (e.getMessage());
			}			
		}
		return docIn;
	}
	
	public	String	getCustomerLevel (YFSEnvironment env, YFCElement eleOrder) throws YFSException
	{
		String	sCustomerLevel;
		
		try {
			YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
			YFCElement	eleCustomer = docCustomer.getDocumentElement();
			
			YFCDocument	docCustomerOutputTemplate = YFCDocument.getDocumentFor ("<Customer CustomerID=\"\" CustomerLevel=\"\" />");
			eleCustomer.setAttribute("OrganizationCode", eleOrder.getAttribute("EnterpriseCode"));
			eleCustomer.setAttribute("CustomerID", eleOrder.getAttribute ("BillToID"));
			env.setApiTemplate ("getCustomerDetails", docCustomerOutputTemplate.getDocument());
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getCustomerDetails:");
				System.out.println (docCustomer.getString());
			}
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			docCustomer = YFCDocument.getDocumentFor (api.getCustomerDetails (env, docCustomer.getDocument()));
			env.clearApiTemplate ("getCustomerDetails");
			eleCustomer = docCustomer.getDocumentElement();
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getCustomerDetails:");
				System.out.println (docCustomer.getString());
			}

			// get customer level
			sCustomerLevel = eleCustomer.getAttribute("CustomerLevel");
			if (YFSUtil.getDebug())
			{
				System.out.println ("Customer Level: "+sCustomerLevel);
			}
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return sCustomerLevel;
	}
	
	public	double	getAvailability (YFSEnvironment env, YFCElement eleCustomerItemDemand) throws YFSException
	{
		double	dblAvailableQty = 0;
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument	docGetATP = YFCDocument.createDocument("CheckAvailability");
			YFCElement	eleGetATP = docGetATP.getDocumentElement();
			eleGetATP.setAttribute("OrganizationCode", eleCustomerItemDemand.getAttribute("OrganizationCode"));
			eleGetATP.setAttribute("ItemID", eleCustomerItemDemand.getAttribute("ItemID"));
			eleGetATP.setAttribute("UnitOfMeasure", eleCustomerItemDemand.getAttribute ("UnitOfMeasure"));
			eleGetATP.setAttribute("RequiredQty", eleCustomerItemDemand.getAttribute("RequestedQty"));
			eleGetATP.setAttribute("ConsiderUnassignedDemand", "Y");
			
			System.out.println ("Input to checkAvailability API:");
			System.out.println (docGetATP.getString());
			docGetATP = YFCDocument.getDocumentFor(api.checkAvailability(env, docGetATP.getDocument()));
			System.out.println ("Output from checkAvailability API:");
			System.out.println (docGetATP.getString());
			YFCElement eleAvailableInformation = docGetATP.getDocumentElement();
			dblAvailableQty = eleAvailableInformation.getDoubleAttribute("AvailableQty");
			
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return dblAvailableQty;
	}
	
	public void manageItemBasedAllocation (YFSEnvironment env, YFCElement eleCustomerItemDemand) throws YFSException
	{
		YFCDocument	docManageItemBasedAllocationTrigger = YFCDocument.createDocument("ItemBasedAllocation");
		YFCElement	eleManageItemBasedAllocationTrigger = docManageItemBasedAllocationTrigger.getDocumentElement();
		eleManageItemBasedAllocationTrigger.setAttribute("OrganizationCode", eleCustomerItemDemand.getAttribute("OrganizationCode"));
		eleManageItemBasedAllocationTrigger.setAttribute ("ItemID", eleCustomerItemDemand.getAttribute("ItemID"));
		eleManageItemBasedAllocationTrigger.setAttribute ("UnitOfMeasure", eleCustomerItemDemand.getAttribute("UnitOfMeasure"));
		eleManageItemBasedAllocationTrigger.setAttribute("IBARequired", "Y");
		eleManageItemBasedAllocationTrigger.setAttribute("ProcessingByAgent", "N");
		eleManageItemBasedAllocationTrigger.setAttribute("Node", eleCustomerItemDemand.getAttribute("ShipNode"));
		try {
			YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to manageItemBaseAllocationTrigger API:");
				System.out.println (docManageItemBasedAllocationTrigger.getString());
			}
			api.manageItemBasedAllocationTrigger(env, docManageItemBasedAllocationTrigger.getDocument());
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

}
