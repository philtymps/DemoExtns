package com.custom.diab.demos.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;

@SuppressWarnings("deprecation")
public class SEFairShareAllocation implements YIFCustomApi {

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

	public SEFairShareAllocation ()
	{
		
	}
	public class OrderLineReservation {
		public	OrderLineReservation()
		{
			m_sOrderLineReservationKey = "";
			m_sOrderHeaderKey = "";
			m_sOrderLineKey = "";
			m_sOrderNo = "";
			m_sNode = "";
			m_sDemandType = "";
			m_sItemID = "";
			m_sUOM = "";
			m_sProductClass = "";
			m_sItemID = "";
			m_sTargetPercentageKey = "";
			m_sReqReservationDate = "";
			m_bIsFutureDemand = false;
			m_bIsReservationUpdReq = !m_bIsFutureDemand;
			m_dblQty = m_dblUpdatedQty = 0.0;
			m_dblMinTargetQty = m_dblMinTargetPercentage = 0.0;
			m_iIncrementBy = 0;
		}
		public void	OutputOrderLineReservation ()
		{
			System.out.println ("BEGIN OF ORDER LINE RESERVATION:");
			System.out.println ("OrderLineReservationKey:" + m_sOrderLineReservationKey);
			System.out.println ("OrderHeaderKey:         " + m_sOrderHeaderKey);
			System.out.println ("OrderLineKey:           " + m_sOrderLineKey);
			System.out.println ("Req. Resevation Date:   " + m_sReqReservationDate);
			System.out.println ("Node:                   " + m_sNode);
			System.out.println ("DemandType:             " + m_sDemandType);
			System.out.println ("ItemID:                 " + m_sItemID);
			System.out.println ("UOM:                    " + m_sUOM);
			System.out.println ("Product Class:          " + m_sProductClass);
			System.out.println ("IBA Quantity:           " + m_dblQty);
			System.out.println ("Fair Share Quantity:    " + m_dblUpdatedQty);
			System.out.println ("Mininmum Targeted Qty:  " + m_dblMinTargetQty);
			System.out.println ("Minimum Target Percent: " + m_dblMinTargetPercentage);
			System.out.println ("Customer Level:         " + m_sTargetPercentageKey);
			System.out.println ("Increment By:           " + m_iIncrementBy);
			System.out.println ("Is Future Demand:       " + m_bIsFutureDemand);
			System.out.println ("Is ReservationReq       " + m_bIsReservationUpdReq);
			System.out.println ("END OF ORDER LINE RESERVATION:");
			return;
		}
		
		public String	m_sOrderLineReservationKey;
		public String	m_sOrderHeaderKey;
		public String	m_sOrderLineKey;
		public String	m_sOrderNo;
		public String	m_sNode;
		public String	m_sReqReservationDate;
		public String	m_sItemID;
		public String	m_sUOM;
		public String	m_sProductClass;
		public String	m_sDemandType;
		public String	m_sTargetPercentageKey;
		public boolean	m_bIsFutureDemand;
		public boolean	m_bIsReservationUpdReq;
		public double	m_dblQty;
		public double	m_dblUpdatedQty;
		public double	m_dblMinTargetQty;
		public double	m_dblMinTargetPercentage;
		public int		m_iIncrementBy;
	}
	
	@SuppressWarnings("rawtypes")
	protected	Hashtable	m_htOLRData;
	@SuppressWarnings("rawtypes")
	protected	Collection	m_colOHK;
	@SuppressWarnings("rawtypes")
	protected	Collection	m_colOLK;
	protected	String		m_sOrderHoldType = "CPG_IBA_HOLD";
	protected	Properties	m_Props;
	
	@SuppressWarnings("rawtypes")
	protected	TreeMap		m_htAggregatePercentages;
	protected	double		m_dblAggregatePercentage;
	
	protected	String[]	m_sDemandTypePriority = new String[] {"BACKORDER", "SCHEDULED"};
	protected	double		m_dblPercentTotal;

	protected	String		m_sTriggerOrgCode;
	protected	String		m_sTriggerItemID;
	protected	String		m_sTriggerUOM;
	protected	String		m_sTriggerPC;
	protected	String		m_sTriggerNode;
	
	@SuppressWarnings("rawtypes")
	public	Document doFairShareAllocation (YFSEnvironment env, Document docIn)
	{
		// get Order Line reservations created by the IBA Agent using HoldType passed as parameter to the Agent
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SEFairShareAllocation - doFairShareAllocation() - Input:");
			System.out.println (YFCDocument.getDocumentFor (docIn).getString());
		}
		m_htAggregatePercentages = new TreeMap();
		m_htOLRData = new Hashtable();
		
		// get IBA Trigger Details - only want to update reservations created for the active trigger
		getIBATriggerDetails (env, docIn);
		
		// get order line reservations that need to be updated
		getOrderLineReservations (env);
		
		// sum of all fair share targets should add up to 100%
		recalculateFairShareTargetsToTotal100Percent ();
	
		// apply the fair share targets to existing order line reservations
		applyNewFairShareTargetsToOrderLineReservations();

		// recalculate fair share demands
		recalculateFairShareDemands();
		
		// generate changeOrder call's to update reservations from IBA process
		Document	docMultiApi = generateMultiApiXML();
		

		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			api.multiApi(env, docMultiApi);
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SEFairShareAllocation - doFairShareAllocaiton() - Output:");
			System.out.println (YFCDocument.getDocumentFor(docMultiApi));
		}
		return docMultiApi;
	}
	
	protected	void getIBATriggerDetails (YFSEnvironment env, Document docIn) throws YFSException
	{
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument docInput = YFCDocument.getDocumentFor(docIn); 
			YFCElement	eleInput = docInput.getDocumentElement();
			YFCElement	eleXML   = eleInput.getChildElement("XML");
			YFCDocument docManageItemBasedAllocationInput = YFCDocument.getDocumentFor ("<manageItemBasedAllocationTrigger IBATriggerKey=\""+eleXML.getAttribute("IBATriggerKey")+"\"/>");

			if (YFSUtil.getDebug ())
			{
				System.out.println ("manageItemBasedAllocation - Input:");
				System.out.println (docManageItemBasedAllocationInput.getString());
			}
			
			YFCDocument	docItemBasedAllocationTrigger = YFCDocument.getDocumentFor(api.manageItemBasedAllocationTrigger(env, docManageItemBasedAllocationInput.getDocument()));
			YFCElement	eleItemBasedAllocationTrigger = docItemBasedAllocationTrigger.getDocumentElement();
			if (YFSUtil.getDebug ())
			{
				System.out.println ("manageItemBasedAllocation - Input:");
				System.out.println (docItemBasedAllocationTrigger.getString());
			}
			
			// save the trigger details needed - only update reservations associated with this IBA event trigger
			m_sTriggerOrgCode = eleItemBasedAllocationTrigger.getAttribute("OrganizationCode");
			m_sTriggerItemID = eleItemBasedAllocationTrigger.getAttribute("ItemID");
			m_sTriggerUOM    = eleItemBasedAllocationTrigger.getAttribute("UnitOfMeasure");
			m_sTriggerPC     = eleItemBasedAllocationTrigger.getAttribute ("ProductClass");
			m_sTriggerNode   = eleItemBasedAllocationTrigger.getAttribute ("Node");
			
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return;
	}
	
	@SuppressWarnings({ "rawtypes" })
	protected void getOrderLineReservations (YFSEnvironment env) throws YFSException
	{
		YFCDocument	docOrderListInput = YFCDocument.getDocumentFor ("<Order><OrderHoldType HoldType=\""+m_sOrderHoldType+"\" EnterpriseCode=\""+m_sTriggerOrgCode+"\"/></Order>");
		YFCDocument	docOrderListOutputTemplate = YFCDocument.getDocumentFor("<OrderList><Order EnterpriseCode=\"\" OrderNo=\"\" BillToID=\"\" OrderHeaderKey=\"\"></Order></OrderList>");
		try {
			
			if (YFSUtil.getDebug ())
			{
				System.out.println ("getOrderLineReservations - Input to getOrderList:");
				System.out.println (docOrderListInput.getString());
			}
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			env.setApiTemplate("getOrderList", docOrderListOutputTemplate.getDocument());
			YFCDocument	docOrderList = YFCDocument.getDocumentFor(api.getOrderList(env, docOrderListInput.getDocument()));
			env.clearApiTemplate ("getOrderList");
			if (YFSUtil.getDebug ())
			{
				System.out.println ("getOrderLineReservations - Output from getOrderList:");
				System.out.println (docOrderList.getString());
			}
			
			YFCElement	eleOrderList = docOrderList.getDocumentElement();
			if (eleOrderList != null)
			{
				Iterator	iOrderList = eleOrderList.getChildren();
			
				while (iOrderList.hasNext())
				{
					YFCElement	eleOrder = (YFCElement)iOrderList.next();
					if (YFSUtil.getDebug())
					{
						System.out.println ("Getting Reservations for Order No: "+eleOrder.getAttribute("OrderNo"));
					}
					getOrderLineReservationsForOrder (env, eleOrder);
				}
			}
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected	void	getOrderLineReservationsForOrder (YFSEnvironment env, YFCElement eleOrder) throws YFSException
	{
		try {
			
			YFCDocument	docOrderDetailsOutputTemplate = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"\" ReqShipDate=\"\"><OrderLines><OrderLine OrderLineKey=\"\"><OrderLineReservations><OrderLineReservation OrderHeaderKey=\"\" OrderLineKey=\"\" OrderLineReservationKey=\"\" ItemID=\"\" UnitOfMeasure=\"\" ProductClass=\"\" OrderNo=\"\" Node=\"\" DemandType=\"\" Quantity=\"\" RequestedReservationDate=\"\"/></OrderLineReservations></OrderLine></OrderLines></Order>");
			
			String		sEnterpriseCode = eleOrder.getAttribute("EnterpriseCode");
			String		sCustomerID = eleOrder.getAttribute("BillToID");
			String		sOrderHeaderKey = eleOrder.getAttribute("OrderHeaderKey");

			YFCDocument	docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\""+sOrderHeaderKey+"\"/>");
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
			
			if (YFSUtil.getDebug ())
			{
				System.out.println ("getOrderLineReservationsForOrder - Input to getOrderDetails:");
				System.out.println (docOrder.getString());
			}
			env.setApiTemplate("getOrderDetails", docOrderDetailsOutputTemplate.getDocument());
			docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
			env.clearApiTemplate("getOrderDetails");
			eleOrder = docOrder.getDocumentElement();
			if (YFSUtil.getDebug())
			{
				System.out.println ("getOrderLineReservationsForOrder - Output from getOrderDetails:");
				System.out.println (docOrder.getString());
			}

			YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
			Iterator	iOrderLines = eleOrderLines.getChildren();
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				YFCElement	eleOrderLineReservations = eleOrderLine.getChildElement("OrderLineReservations");
				if (eleOrderLineReservations != null)
				{
					Iterator	iOrderLineReservations = eleOrderLineReservations.getChildren();
					while (iOrderLineReservations.hasNext())
					{
						YFCElement	eleOrderLineReservation = (YFCElement)iOrderLineReservations.next();
						YFCDate dtReqShipDateTime        = eleOrder.getDateTimeAttribute("ReqShipDate");
						YFCDate	dtReqReservationDateTime = eleOrderLineReservation.getDateTimeAttribute("RequestedReservationDate", new YFCDate());
						long	lReqReservationTime = dtReqReservationDateTime.getTime();
						long	lReqShipTime;

						// if no specified requested ship date assume shipping today
						if (YFCObject.isVoid(dtReqShipDateTime))
								lReqShipTime = System.currentTimeMillis();
						else
								lReqShipTime = dtReqShipDateTime.getTime();
						
						OrderLineReservation olrReservation = new OrderLineReservation ();
						olrReservation.m_sOrderLineReservationKey = eleOrderLineReservation.getAttribute("OrderLineReservationKey");
						olrReservation.m_sOrderHeaderKey = eleOrderLineReservation.getAttribute("OrderHeaderKey");
						olrReservation.m_sOrderLineKey = eleOrderLineReservation.getAttribute("OrderLineKey");
						olrReservation.m_bIsFutureDemand = (lReqReservationTime > lReqShipTime);
						
						olrReservation.m_dblQty = eleOrderLineReservation.getDoubleAttribute("Quantity");
						olrReservation.m_dblUpdatedQty = olrReservation.m_dblQty;
						olrReservation.m_sDemandType = eleOrderLineReservation.getAttribute("DemandType");
						olrReservation.m_sItemID = eleOrderLineReservation.getAttribute("ItemID");
						olrReservation.m_sUOM = eleOrderLineReservation.getAttribute ("UnitOfMeasure");
						olrReservation.m_sProductClass = eleOrderLineReservation.getAttribute ("ProductClass");
						olrReservation.m_sNode = eleOrderLineReservation.getAttribute("Node");
						olrReservation.m_sReqReservationDate = eleOrderLineReservation.getAttribute("RequestedReservationDate");
						olrReservation.m_iIncrementBy = (olrReservation.m_dblQty > 0) ? 1 : -1;

						// only process reservations created for the active trigger message
						if (olrReservation.m_sNode.equals(m_sTriggerNode) && olrReservation.m_sItemID.equals(m_sTriggerItemID) &&  olrReservation.m_sProductClass.equals (m_sTriggerPC))
						{
							olrReservation.m_bIsReservationUpdReq = true; //  use this code to remove future demands from fair share allocation instead of 'true': (!olrReservation.m_bIsFutureDemand);
							olrReservation.m_sTargetPercentageKey = getFairShareTargets (env, sEnterpriseCode, sCustomerID, olrReservation);

							// if this is a unique OLR record add it to OLR's Hash Table to update
							if (m_htOLRData.get(olrReservation.m_sOrderLineReservationKey) == null)
								m_htOLRData.put(olrReservation.m_sOrderLineReservationKey, (OrderLineReservation)olrReservation);
						}
					}
				}
			}		
		}
		catch (Exception e)
		{
			throw new YFSException (e.getMessage());
		}
	}
		
	@SuppressWarnings("rawtypes")
	protected	void	recalculateFairShareDemands()
	{
		if (YFSUtil.getDebug())
		{
			Enumeration	enumReservations = m_htOLRData.keys();
			System.out.println ("Order Line Reservations before Recalculation of Fair Share Demands:");
			
			while (enumReservations.hasMoreElements())
			{
				OrderLineReservation	olr = (OrderLineReservation)m_htOLRData.get(enumReservations.nextElement());
				olr.OutputOrderLineReservation();
			}
		}
		return;
	}	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected	Document	generateMultiApiXML ()
	{
		YFCDocument	docMultiApi	= YFCDocument.createDocument("MultiApi");
		YFCElement	eleMultiApi = docMultiApi.getDocumentElement();
		YFCElement	eleOrder;
		YFCElement	eleOrderLines = null;
		YFCElement	eleOrderLine;
		YFCElement	eleOrderLineReservations = null;
		YFCElement	eleOrderLineReservation;

		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering generateMultiApiXML():");
		}

		Enumeration	enumReservations = m_htOLRData.keys();
		Hashtable	htDistinctOrders = new Hashtable();
		Hashtable	htDistinctLines = new Hashtable();
		
		while (enumReservations.hasMoreElements())
		{
			OrderLineReservation	olr = (OrderLineReservation)m_htOLRData.get(enumReservations.nextElement());

			// if reservation no longer required or needed skip it
			if (!olr.m_bIsReservationUpdReq)
				continue;
			
			// if an order element doesn't already exists for this order header key
			if (htDistinctOrders.get(olr.m_sOrderHeaderKey) == null)
			{
				YFCElement	eleAPI = eleMultiApi.createChild("API");
				eleAPI.setAttribute ("Name", "changeOrder");
				
				YFCElement	eleInput = eleAPI.createChild("Input");
				eleOrder = eleInput.createChild("Order");
				eleOrder.setAttribute ("OrderHeaderKey", olr.m_sOrderHeaderKey);
				eleOrder.setAttribute ("CustomerLevel", olr.m_sTargetPercentageKey);
				htDistinctOrders.put(olr.m_sOrderHeaderKey, eleOrder);

				eleOrderLines = eleOrder.createChild("OrderLines");
			}
			// else modify the existing order
			else
			{
				eleOrder = (YFCElement)htDistinctOrders.get(olr.m_sOrderHeaderKey);
				eleOrderLines = eleOrder.getChildElement("OrderLines");
			}
			// if an order line element doesn't already exist for this order line key
			if (htDistinctLines.get(olr.m_sOrderLineKey) == null)
			{
				eleOrderLine  = eleOrderLines.createChild("OrderLine");
				htDistinctLines.put(olr.m_sOrderLineKey, eleOrderLine);
				
				eleOrderLine.setAttribute("OrderLineKey", olr.m_sOrderLineKey);
				eleOrderLineReservations = eleOrderLine.createChild("OrderLineReservations");
				eleOrderLineReservations.setAttribute("Reset", "Y");
			}
			// else modify an existing order line element
			else
			{
				eleOrderLine = (YFCElement)htDistinctLines.get(olr.m_sOrderLineKey);
				eleOrderLineReservations = eleOrderLine.getChildElement("OrderLineReservations");
			}
			eleOrderLineReservation = eleOrderLineReservations.createChild("OrderLineReservation");
			
			// modify the order line reservation quantity to reflect the new fair share quantity
			//eleOrderLineReservation.setAttribute("OrderLineReservationKey", olr.m_sOrderLineReservationKey);
			//eleOrderLineReservation.setAttribute("OrderLineKey", olr.m_sOrderLineKey);
			//eleOrderLineReservation.setAttribute("OrderHeaderKey", olr.m_sOrderHeaderKey);
			eleOrderLineReservation.setAttribute("Node", olr.m_sNode);
			eleOrderLineReservation.setAttribute("ItemID", olr.m_sItemID);
			eleOrderLineReservation.setAttribute("UnitOfMeasure", olr.m_sUOM);
			eleOrderLineReservation.setAttribute("ProductClass", olr.m_sProductClass);
			eleOrderLineReservation.setAttribute("RequestedReservationDate", olr.m_sReqReservationDate);
			eleOrderLineReservation.setAttribute("DemandType", olr.m_sDemandType);
			eleOrderLineReservation.setDoubleAttribute("Quantity", olr.m_dblUpdatedQty);
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting generateMultiApiXML():");
		}
		return docMultiApi.getDocument();
	}
	
	@SuppressWarnings("unchecked")
	protected	String	getFairShareTargets (YFSEnvironment env, String sEnterpriseCode, String sCustomerID, OrderLineReservation olr) throws YFSException
	{
		String	sCustomerLevel;
		
		try {
			YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
			YFCElement	eleCustomer = docCustomer.getDocumentElement();
			
			YFCDocument	docCustomerOutputTemplate = YFCDocument.getDocumentFor ("<Customer CustomerID=\"\" CustomerLevel=\"\" />");
			eleCustomer.setAttribute("OrganizationCode", sEnterpriseCode);
			eleCustomer.setAttribute("CustomerID", sCustomerID);
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
			
			// get fair share fixed targets from Commmon Code table CPG_FAIRSHARE_ALLOC
			getFairShareAllocationConfiguration (env, sEnterpriseCode, sCustomerLevel, olr);

			if (m_htAggregatePercentages.get(sCustomerLevel) == null)
			{
				m_dblAggregatePercentage += olr.m_dblMinTargetPercentage;
				m_htAggregatePercentages.put(sCustomerLevel, new Double(olr.m_dblMinTargetPercentage));
			}
			// if target percentage results in 0 Quantity use 1 or -1;
			if ((olr.m_dblMinTargetQty = new Double(olr.m_dblMinTargetPercentage * olr.m_dblQty).intValue()) == 0)
				olr.m_dblMinTargetQty = olr.m_iIncrementBy;
			
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return (sCustomerLevel);
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void getFairShareAllocationConfiguration (YFSEnvironment env, String sEnterpriseCode, String sCustomerLevel, OrderLineReservation olr) throws YFSUserExitException
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		eleCommonCode.setAttribute ("CodeType", "CPG_FAIRSHARE_ALLOC");
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception e) {
			throw (new YFSUserExitException (e.getMessage()));
		}
		// if the CPG_FAIRSHARE_ALLOC common code table was found
		// Note Entries must be in the following format
		// CodeValue: Customer Level (TIER_0 - TIER_5)
		// CodeShortDescription: Fair Share Allocation Percentage
		// CodeLongDescription: Description
		
		if (eleCommonCodes != null)
		{
			Iterator	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{	
				eleCommonCode = (YFCElement)iCommonCodes.next();
				if (YFCCommon.equals(sCustomerLevel, eleCommonCode.getAttribute ("CodeValue")))
				 olr.m_dblMinTargetPercentage = eleCommonCode.getDoubleAttribute ("CodeShortDescription");
			}
		}
		else
		{
			
			// Hard Code Percentages for now...Use COMMON CODE table later...
			if (sCustomerLevel.equals("TIER_0"))
			{
				olr.m_dblMinTargetPercentage = .48; // should compute to 50 after recalc
			}
			else if (sCustomerLevel.equals("TIER_1"))
			{
				olr.m_dblMinTargetPercentage = .24;  // should compute to 26 after recalc
			}
			else if (sCustomerLevel.equals("TIER_2"))
			{
				olr.m_dblMinTargetPercentage = .12;  // should compute to 14 after recalc
			}
			else if (sCustomerLevel.equals("TIER_3"))
			{
				olr.m_dblMinTargetPercentage = .09;	// should compute to 10 after recalc
			}
			else if (sCustomerLevel.equals("TIER_4"))
			{
				olr.m_dblMinTargetPercentage = .04;				
			}
			else
				olr.m_dblMinTargetPercentage = .03;
		}	
	}

	
	@SuppressWarnings("rawtypes")
	protected	void applyNewFairShareTargetsToOrderLineReservations()
	{
		Enumeration	enumReservations = m_htOLRData.keys();
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering applyNewFairShareTargetsToOrderLineReservations():");
		}
		while (enumReservations.hasMoreElements())
		{
			OrderLineReservation	olr = (OrderLineReservation)m_htOLRData.get(enumReservations.nextElement());
			// if reservation update not required
			if (!olr.m_bIsReservationUpdReq)
				continue;
			if (YFSUtil.getDebug())
			{
				System.out.println ("*******************************");
				System.out.println ("OLR Before Fair Share Applied: ");
				olr.OutputOrderLineReservation();
				System.out.println ("");
			}

			double dblMinTagetPercentage = ((Double)m_htAggregatePercentages.get(olr.m_sTargetPercentageKey)).doubleValue();
			BigDecimal dbMinTargetPercentage = new BigDecimal(dblMinTagetPercentage).setScale(2, RoundingMode.DOWN);
			olr.m_dblMinTargetPercentage = dbMinTargetPercentage.doubleValue();

			// for negative quantities invert the fair share percentage
			if (olr.m_dblQty < 0)
				olr.m_dblMinTargetPercentage = 1.00000000 - olr.m_dblMinTargetPercentage;

			// compute new min target qty and update the reservation quantity
			olr.m_dblMinTargetQty = new Double(olr.m_dblMinTargetPercentage * olr.m_dblQty).intValue();
			
			// ensure target quantity never is zero - either 1 or -1 but never 0
			if (olr.m_dblMinTargetQty > -1 && olr.m_dblMinTargetQty < 1)
				olr.m_dblMinTargetQty = olr.m_iIncrementBy;
			olr.m_dblUpdatedQty = olr.m_dblMinTargetQty;		

			if (YFSUtil.getDebug())
			{
				System.out.println ("OLR After Fair Share Applied: ");
				olr.OutputOrderLineReservation();				
				System.out.println ("*******************************");

			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting applyNewFairShareTargetsToOrderLineReservations():");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected	void recalculateFairShareTargetsToTotal100Percent()
	{
		
		Iterator	iAggregatePercentages = m_htAggregatePercentages.keySet().iterator();
		String	sCustomerLevel;
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Aggregate Fair Share Percentage of All Targeted Customers Before Recalculation=" + m_dblAggregatePercentage);
			System.out.println ("Recalculating to Total 100%");
			Iterator	debugAggregatePercentages = m_htAggregatePercentages.keySet().iterator();
			while (debugAggregatePercentages.hasNext())
			{
				String sKey = (String)debugAggregatePercentages.next();
				System.out.println ("Customer Level: " + sKey + " Percentage: " + ((Double)m_htAggregatePercentages.get(sKey)).toString());
			}
		}
		
		// TODO: We should sort the m_htAggregatePercentages Hashtable by CustomerLevel
		
		// make sure the aggregate percentages total 100% and add to Min Percentages until it does
		if (iAggregatePercentages.hasNext())
		{
			for (double dblPercentage = m_dblAggregatePercentage; dblPercentage < 1; dblPercentage += .01)
			{
				sCustomerLevel = (String)iAggregatePercentages.next();
				Double	dblMinTargetPercentage = (Double)m_htAggregatePercentages.get(sCustomerLevel);
				double	dblNewMinTargetPercentage = dblMinTargetPercentage.doubleValue() + .01;
				m_dblAggregatePercentage += .01;
				m_htAggregatePercentages.put(sCustomerLevel, new Double(dblNewMinTargetPercentage));
				if (!iAggregatePercentages.hasNext())
					iAggregatePercentages = m_htAggregatePercentages.keySet().iterator();
			}
			// round all target values to 2 digit percentage
			iAggregatePercentages = m_htAggregatePercentages.keySet().iterator();
			while (iAggregatePercentages.hasNext())
			{
				sCustomerLevel = (String)iAggregatePercentages.next();
				double	dblMinTargetPercentage = ((Double)m_htAggregatePercentages.get(sCustomerLevel)).doubleValue();
				dblMinTargetPercentage = new BigDecimal (dblMinTargetPercentage).setScale(2, RoundingMode.HALF_UP).doubleValue();
				m_htAggregatePercentages.put(sCustomerLevel, new Double(dblMinTargetPercentage));
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Aggregate Fair Share Percentage of All Targeted Customers After Recalculation=" + m_dblAggregatePercentage);
			System.out.println ("Should Add up to 1 or 100%");
			Iterator	debugAggregatePercentages = m_htAggregatePercentages.keySet().iterator();
			while (debugAggregatePercentages.hasNext())
			{
				String sKey = (String)debugAggregatePercentages.next();
				System.out.println ("Customer Level: " + sKey + " Percentage:" + ((Double)m_htAggregatePercentages.get(sKey)).toString());
			}
		}	
	}
}
