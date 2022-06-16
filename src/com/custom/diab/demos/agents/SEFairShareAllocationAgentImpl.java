package com.custom.diab.demos.agents;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.diab.demos.api.SEFairShareAllocation.OrderLineReservation;
import com.custom.yantra.util.YFSUtil;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.ycp.core.YCPContext;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class SEFairShareAllocationAgentImpl extends YCPBaseTaskAgent implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(SEFairShareAllocationAgentImpl.class);
	private Properties	m_Props;
	
	public SEFairShareAllocationAgentImpl() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}


	@Override
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception {

		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor(inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();
		YFCElement	eleTransactionFilters = eleTaskQueue.getChildElement ("TransactionFilters");
		
		System.out.println ("Input to Fair Share Allocation Agent:");
		System.out.println (docTaskQueue.getString());
		System.out.println ("eleTransactionFilters: " + eleTransactionFilters.getString());
		
		splitLinesOnFairShareOrders (env, eleTaskQueue);
		
		changeOrderLineStatus (env, eleTaskQueue);

		YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
		YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
		String		sTransactionId = eleTransactionFilters.getAttribute ("TransactionId");
		
		eleRaiseEvent.setAttribute ("TransactionId", sTransactionId);
		eleRaiseEvent.setAttribute ("EventId", "ON_SUCCESS");
		YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
		YFCElement	eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TransactionId");
		eleData.setAttribute ("Value", eleTransactionFilters.getAttribute("TransactionId"));		
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "OrganizationCode");
		eleData.setAttribute ("Value", eleTransactionFilters.getAttribute("OrganizationCode"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "OrderHeaderKey");
		eleData.setAttribute ("Value", eleTransactionFilters.getAttribute("OrderHeaderKey"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "CommonCodesForDemand");
		eleData.setAttribute ("Value", "CPG_FAIRSHARE_DEMAND");

		
		YFCElement	eleDataType = eleRaiseEvent.createChild ("DataType");
		eleDataType.setNodeValue ("0");	// Data Type is 1 for TYPE_XML_STRING or 0 for TYPE_JAVA_MAP
		
		//YFCElement	eleXmlData = eleRaiseEvent.createChild ("XMLData");
		//eleXmlData.setNodeValue (docJobXml.getString());		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to RaiseEvent:");
			System.out.println (docRaiseEvent.getString());
		}
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();

		// raise ON_SUCCESS event
		api.raiseEvent (env, docRaiseEvent.getDocument());
		
		registerTaskComplete (env, eleTaskQueue);
		return inXML;
	}
	
	@SuppressWarnings("rawtypes")
	private void 	changeOrderLineStatus (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{

		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
		
		YFCElement	eleTransactionFilters = eleTaskQueue.getChildElement("TransactionFilters");

		// get all the lines on the current order in the pickup status for this transaction					
		eleOrderLineStatusList.setAttribute ("TransactionId", eleTransactionFilters.getAttribute("TransactionId"));
		eleOrderLineStatusList.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getOrderLineStatus API:");
			System.out.println (docOrderLineStatusList.getString());
		}
		docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
		eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getOrderLineStatus API:");
			System.out.println (docOrderLineStatusList.getString());
		}

		// get the drop statuses (assumes multiple status may exist)
		String 		sDropStatus = "1300.200";

		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
		
		eleOrderStatusChange.setAttribute ("TransactionId", eleTransactionFilters.getAttribute("TransactionId"));
		YFCElement	eleOrderLines = eleOrderStatusChange.createChild ("OrderLines");

		// for each of the order lines picked up by the agent
		Iterator	iOrderLineStatus = eleOrderLineStatusList.getChildren();

		while (iOrderLineStatus.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatus.next();
			
			if (eleOrderLineStatus.getNodeName().equals ("OrderStatus"))
			{
				YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
				eleOrderStatusChange.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));
				eleOrderLine.setAttribute ("OrderLineKey", eleOrderLineStatus.getAttribute ("OrderLineKey"));
				eleOrderLine.setAttribute ("BaseDropStatus", sDropStatus);
				eleOrderLine.setAttribute ("ChangeForAllAvailableQty", "Y");
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to changeOrderStatus:");
					System.out.println (docOrderStatusChange.getString());
				}
				api.changeOrderStatus (env, docOrderStatusChange.getDocument());		
			}
		}
		
	}	

	private void	registerTaskComplete (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		
		YFCDocument	docRegisterTaskCompletion = YFCDocument.createDocument ("RegisterProcessCompletionInput");
		YFCElement	eleRegisterTaskCompletion = docRegisterTaskCompletion.getDocumentElement ();
		
		eleRegisterTaskCompletion.setAttribute ("KeepTaskOpen", "N");
		YFCElement	eleCurrentTask = eleRegisterTaskCompletion.createChild ("CurrentTask");
		eleCurrentTask.setAttribute ("TaskQKey", eleTaskQueue.getAttribute ("TaskQKey"));
		eleCurrentTask.setAttribute ("DataKey", eleTaskQueue.getAttribute ("DataKey"));
		eleCurrentTask.setAttribute ("DataType", eleTaskQueue.getAttribute ("DataType"));
		eleCurrentTask.setAttribute ("TransactionId", eleTaskQueue.getChildElement("TransactionFilters").getAttribute ("TransactionId"));
		eleCurrentTask.setAttribute ("AvailableDate", eleTaskQueue.getAttribute ("AvailableDate"));
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Calling registerProcessCompletion API:");
			System.out.println (docRegisterTaskCompletion.getString());
		}
		
		api.registerProcessCompletion (env, docRegisterTaskCompletion.getDocument());	
	}


	private void	splitLinesOnFairShareOrders (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{
	    YFCDocument	docCommonCodeList = YFCDocument.createDocument("CommonCode");
	    YFCElement	eleCommonCodeList = docCommonCodeList.getDocumentElement();
	    eleCommonCodeList.setAttribute("CodeType", "CPG_FAIRSHARE_DEMAND");
	    YFCElement	eleTransactionFilters = eleTaskQueue.getChildElement("TransactionFilters");
	    
	    if (!YFCObject.isVoid(eleTransactionFilters) && !YFCObject.isVoid(eleTransactionFilters.getAttribute("OrganizationCode")))
	    	eleCommonCodeList.setAttribute("OrganizationCode", eleTransactionFilters.getAttribute("OrganizationCode"));
	    else
	    	eleCommonCodeList.setAttribute("OrganizationCode", "DEFAULT");
	    
	    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Input to getCommonCodeList API:");
	    	System.out.println (docCommonCodeList.getString());
	    }
	    docCommonCodeList = YFCDocument.getDocumentFor(api.getCommonCodeList(env, docCommonCodeList.getDocument()));
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Output from getCommonCodeList API:");
	    	System.out.println (docCommonCodeList.getString());
	    }
	    
	    YFCDocument	docOrder = getOrderKeyDetails (env, eleTaskQueue.getAttribute("DataKey"));
	    YFCElement	eleOrder = docOrder.getDocumentElement();
	    
	    eleCommonCodeList = docCommonCodeList.getDocumentElement();
	    Iterator<YFCElement>	iCommonCodes = eleCommonCodeList.getChildren();
	    
		while (iCommonCodes.hasNext())
		{
			YFCElement	eleCommonCode = (YFCElement)iCommonCodes.next();
			String		sOLK = eleCommonCode.getAttribute ("CodeValue");
			List<String> lstSupplyDemand = Arrays.asList(eleCommonCode.getAttribute("CodeShortDescription").split("\\s*#\\s*"));
			List<String> lstNodedQty = Arrays.asList(eleCommonCode.getAttribute("CodeLongDescription").split("\\s*#\\s*"));
			// CommonCode Value			   =  OrderLineKey
			// CommonCode ShortDescription = "TOTALSUPPLY,TOTALDEMAND,TOTALSHORTAGE"
			// CommonCode LongDescription  = "SHIPNODE1#QTY,SHIPNODE2#QTY..."
			YFCDocument docOrderLineDetailTemplate = YFCDocument.getDocumentFor("<OrderLine OrderHeaderKey=\"\" OrderLineKey=\"\" OrderedQty=\"\" ShipToID=\"\"><Item ItemID=\"\" UnitOfMeasure=\"\" ProductClass=\"\"/><Order OrderType=\"\" EnterpriseCode=\"\" OrderHeaderKey=\"\" BillToID=\"\" ShipToID=\"\" OrderNo=\"\"></Order><OrderStatuses><OrderStatus/></OrderStatuses></OrderLine>");
			YFCDocument docOrderLineDetail = YFCDocument.getDocumentFor("<OrderLineDetail OrderLineKey=\"" + sOLK + "\"/>");
		    env.setApiTemplate("getOrderLineDetails", docOrderLineDetailTemplate.getDocument());
		    
		    // sometimes lingering OLK's will be in common code table.  Make sure this one is valid
		    if (IsValidOrderLine (eleOrder, sOLK))
		    {
			    if (YFSUtil.getDebug())
			    {
			    	System.out.println ("Input to getOrderLineDetail:");
			    	System.out.println (docOrderLineDetail.getString());
			    }
		    	docOrderLineDetail = YFCDocument.getDocumentFor(api.getOrderLineDetails(env,  docOrderLineDetail.getDocument()));
		    	YFCElement	eleOrderLineDetail = docOrderLineDetail.getDocumentElement();
		    	env.clearApiTemplate("getOrderLineDetails");
			    if (YFSUtil.getDebug())
			    {
			    	System.out.println ("Output from getOrderLineDetail:");
			    	System.out.println (docOrderLineDetail.getString());
			    }
			    if (eleTaskQueue.getAttribute("DataKey").equals(eleOrderLineDetail.getAttribute("OrderHeaderKey")))
			    {
				    // delete common code demands recorded for this OLK if they exist
					deleteDemandsInCommonCodeTable (env, eleTransactionFilters.getAttribute("OrganizationCode"), eleOrderLineDetail);
			    		
		    		// split line as per fair share or proportional share
		    		splitLineForFairShare (env, eleTransactionFilters, eleOrderLineDetail, lstSupplyDemand, lstNodedQty);
			    }
		    }
		}
	    System.out.println ("Exiting SpliLinesOnFairShareOrders");
		return;
	}
	
	private void	splitLineForFairShare (YFSEnvironment env, YFCElement eleTransactionFilters, YFCElement eleOrderLineDetail, List<String> lstOLKSupplyDemand, List<String> lstScheduleQtys) throws Exception
	{
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		MathContext	mc = new MathContext (2, RoundingMode.HALF_DOWN);
		
		
		String 	sRoundingMethod = eleTransactionFilters.getAttribute("RoundingMethod");
		if (!YFCObject.isVoid(sRoundingMethod))
		{
			if (sRoundingMethod.equals("UP"))
				mc = new MathContext(2, RoundingMode.UP);
			else if (sRoundingMethod.equals("HALF_UP"))
				mc = new MathContext (2, RoundingMode.HALF_UP);
			else if (sRoundingMethod.equals("HALF_EVEN"))
				mc = new MathContext (2, RoundingMode.HALF_EVEN);
			else if (sRoundingMethod.equals ("HALF_DOWN"))
				mc = new MathContext (2, RoundingMode.HALF_DOWN);
			System.out.println ("Rounding Method Being Used:" + sRoundingMethod);
		}
		BigDecimal	bdTotalSupply = new BigDecimal (lstOLKSupplyDemand.get(0));
		BigDecimal	bdTotalDemand = new BigDecimal (lstOLKSupplyDemand.get(1));
		BigDecimal	bdTotalShortage = new BigDecimal (lstOLKSupplyDemand.get(2));

		// get enterprise, method and scheduling delay for split lines from transaction filters (criteria params)
		String		sOrganizationCode = eleTransactionFilters.getAttribute("OrganizationCode");
		String		sMethod = eleTransactionFilters.getAttribute("Method");
		String		sHoursToDelayScheduling = eleTransactionFilters.getAttribute("HoursToDelaySchedulingOfSplitLines");
		int			iHoursToDelayScheduling = 12;

		if (!YFCObject.isVoid(sHoursToDelayScheduling))
			iHoursToDelayScheduling = Integer.valueOf(sHoursToDelayScheduling);
		if (YFCObject.isVoid(sOrganizationCode))
			sOrganizationCode="DEFAULT";
		if (YFCObject.isVoid(sMethod))
			sMethod = "PROPORTIONAL";
		
		
		YFCDocument	docSplitOrderLine = YFCDocument.createDocument ("Order");
		YFCElement	eleSplitOrderLine= docSplitOrderLine.getDocumentElement();
		
	    YFCElement  eleOrder = eleOrderLineDetail.getChildElement("Order");
	    YFCElement	eleItem = eleOrderLineDetail.getChildElement("Item");
	    
		String		sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
		String		sCustomerID = eleOrder.getAttribute("BillToID");

		BigDecimal	bdOrderedQty = new BigDecimal (getQtyToSplit(eleOrderLineDetail));
		
		BigDecimal  bdProportionalPercent = bdOrderedQty.divide(bdTotalDemand, mc);
		
		// 
		//if (bdProportionalPercent.compareTo(BigDecimal.valueOf(1L)) >= 0)
				
				
		System.out.println ("bdTotalSupply = " + bdTotalSupply.toString());
		System.out.println ("bdTotalDemand = " + bdTotalDemand.toString());
		System.out.println ("bdTotalShortageQty  = " + bdTotalShortage.toString());
		System.out.println ("bdOrderedQty  = " + bdOrderedQty.toString());
		BigDecimal	bdSplitQty = null;
		if (sMethod.equals("FAIRSHARE"))
		{
			BigDecimal	bdFairSharePercent = new BigDecimal(getFairShareAllocationConfiguration (env, sEnterpriseCode, sCustomerID), mc);
			bdSplitQty = bdOrderedQty.subtract(bdOrderedQty.multiply(bdFairSharePercent), mc);
			System.out.println ("bdFairSharePercent  = " + bdFairSharePercent.toString());
		}
		else if (sMethod.equals("PROPORTIONAL"))
		{
			System.out.println ("bdProportionalPercent  = " + bdProportionalPercent.toString());
			bdSplitQty = bdOrderedQty.subtract(bdTotalSupply.multiply(bdProportionalPercent, mc));
		}
		else if (sMethod.equals("CUSTOMAPI"))
		{
			String		sDocCustomerItemDemand = "<CustomerItemDemand CustomerID=\"" + sCustomerID + "\" ItemID=\"" + eleItem.getAttribute("ItemID") +
												 "\" UnitOfMeasure=\"" + eleItem.getAttribute("UnitOfMeasure") +
												 "\" RequestedQty=\"" + bdOrderedQty.toString() + "\"/>";
			
			
			System.out.println ("Input to Custom API: " + eleTransactionFilters.getAttribute("FlowName"));
			System.out.println (sDocCustomerItemDemand);

			YFCDocument	docCustomerItemDemand = YFCDocument.getDocumentFor(sDocCustomerItemDemand);
			
			docCustomerItemDemand = getCustomSplitQty (env, docCustomerItemDemand, eleTransactionFilters.getAttribute("FlowName"));
			System.out.println ("Output from Custom API:" + eleTransactionFilters.getAttribute("FlowName"));
			System.out.println (docCustomerItemDemand.getString());
			bdSplitQty = new BigDecimal(docCustomerItemDemand.getDocumentElement().getAttribute("SplitQty"));
			
		}
		
		BigInteger biQtyToSplit = bdSplitQty.toBigInteger();
		System.out.println ("Split Qty: " + biQtyToSplit.toString());
		
		// if QTY to Split is <= 0 then we won't split this line
		if (biQtyToSplit.longValue() >= 0)
		{
			
			eleSplitOrderLine.setAttribute("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
			YFCElement	eleOrderLines = eleSplitOrderLine.createChild("OrderLines");
			YFCElement	eleOrderLine = eleOrderLines.createChild("OrderLine");
			eleOrderLine.setAttribute("OrderLineKey", eleOrderLineDetail.getAttribute("OrderLineKey"));
			
			
			eleOrderLine.setAttribute("QuantityToSplit", biQtyToSplit.toString());
			
			// calculate EarliestScheduleDate for split lines using criteria value HoursToDelaySchedulingOfSplitLines
			YTimestamp	dtNextScheduleDate = YTimestamp.newMutableTimestamp (System.currentTimeMillis());
			
			dtNextScheduleDate.addHours(iHoursToDelayScheduling);
			System.out.println ("Next Schedule Date for Split Lines: " + dtNextScheduleDate.getString());
			
			YFCElement	eleSplitLines = eleOrderLine.createChild("SplitLines");
			YFCElement	eleSplitLine = eleSplitLines.createChild("SplitLine");
			eleSplitLine.setAttribute("OrderedQty", biQtyToSplit.toString());
			eleSplitLine.setDateTimeAttribute ("AllocationDate", dtNextScheduleDate);
			eleSplitLine.setDateTimeAttribute ("EarliestScheduleDate", dtNextScheduleDate);
			eleSplitLine.setDateTimeAttribute ("ReqShipDate", dtNextScheduleDate);
			
		    if (YFSUtil.getDebug())
		    {
		    	System.out.println ("Input to splitLine API:");
		    	System.out.println (docSplitOrderLine.getString());
		    }
		    docSplitOrderLine = YFCDocument.getDocumentFor(api.splitLine(env,  docSplitOrderLine.getDocument()));
		    if (YFSUtil.getDebug())
		    {
		    	System.out.println ("Output from splitLine API:");
		    	System.out.println (docSplitOrderLine.getString());
		    }		
		}
		else
		{
		    if (YFSUtil.getDebug())
		    {
		    	System.out.println ("Split Qty <= 0 - Line Not Split");
		    }

		}
	}

	private YFCDocument getOrderKeyDetails (YFSEnvironment env, String sOrderHeaderKey) throws Exception
	{
	    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
	    YFCDocument docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"" + sOrderHeaderKey + "\"/>");
	    YFCDocument	docOrderTemplate = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"\" OrderNo=\"\"><OrderLines><OrderLine OrderLineKey=\"\"/></OrderLines></Order>");
	    env.setApiTemplate("getOrderDetails", docOrderTemplate.getDocument());
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Input to getOrderDetails:");
	    	System.out.println (docOrder.getString());
	    }
	    docOrder = YFCDocument.getDocumentFor(api.getOrderDetails(env, docOrder.getDocument()));
	    env.clearApiTemplate("getOrderDetails");
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Output from getOrderDetails:");
	    	System.out.println (docOrder.getString());
	    }
	    return docOrder;
	}
	
	private YFCDocument getCustomSplitQty (YFSEnvironment env, YFCDocument docCustomerItemDemand, String sFlowName) throws Exception
	{
	    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Input to getCustomerSplitQty:");
	    	System.out.println (docCustomerItemDemand.getString());
	    }
	    docCustomerItemDemand = YFCDocument.getDocumentFor(api.executeFlow(env, sFlowName, docCustomerItemDemand.getDocument()));
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Output from getCustomeSplitQty:");
	    	System.out.println (docCustomerItemDemand.getString());
	    }
	    return docCustomerItemDemand;
		
	}
	
	private double getQtyToSplit(YFCElement eleOrderLineDetail)
	{
		YFCElement	eleOrderLineStatuses = eleOrderLineDetail.getChildElement("OrderStatuses");
		Iterator <YFCElement>	iOrderLineStatuses = eleOrderLineStatuses.getChildren();
		while (iOrderLineStatuses.hasNext())
		{
			YFCElement	eleOrderLineStatus = iOrderLineStatuses.next();
			if (eleOrderLineStatus.getAttribute("Status").equals("1300.100"))
				return (eleOrderLineStatus.getDoubleAttribute("StatusQty"));
		}
		return (eleOrderLineDetail.getDoubleAttribute("OrderedQty"));
	}
	
	private	boolean IsValidOrderLine (YFCElement eleOrder, String sOrderLineKey)
	{
	    YFCElement	eleOrderLines= eleOrder.getChildElement("OrderLines");
	    Iterator<YFCElement> iOrderLines = eleOrderLines.getChildren();
	    while (iOrderLines.hasNext())
	    {
	    	YFCElement	eleOrderLine = iOrderLines.next();
	    	if (sOrderLineKey.equals(eleOrderLine.getAttribute("OrderLineKey")))
	    			return true;
	    }
	    return false;
	}
	
	private void deleteDemandsInCommonCodeTable (YFSEnvironment env, String sOrganizationCode, YFCElement eleOrderLine) throws Exception
	{
		deleteDemandsInCommonCodeTable (env, sOrganizationCode, eleOrderLine.getAttribute("OrderLineKey"));
	}
	
	private void deleteDemandsInCommonCodeTable (YFSEnvironment env, String sOrganizationCode, String sOLK) throws Exception
	{
	    System.out.println ("Entering deleteDemandsInCommonCodeTable");

	    YFCDocument	docCommonCodeList = YFCDocument.createDocument("CommonCode");
	    YFCElement	eleCommonCodeList = docCommonCodeList.getDocumentElement();
	    eleCommonCodeList.setAttribute("CodeType", "CPG_FAIRSHARE_DEMAND");
	    eleCommonCodeList.setAttribute("OrganizationCode", sOrganizationCode);
	    
	    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Input to getCommonCodeList API:");
	    	System.out.println (docCommonCodeList.getString());
	    }
	    docCommonCodeList = YFCDocument.getDocumentFor(api.getCommonCodeList(env, docCommonCodeList.getDocument()));
	    if (YFSUtil.getDebug())
	    {
	    	System.out.println ("Output from getCommonCodeList API:");
	    	System.out.println (docCommonCodeList.getString());
	    }
	    
	    eleCommonCodeList = docCommonCodeList.getDocumentElement();
	    Iterator<YFCElement>	iCommonCodes = eleCommonCodeList.getChildren();
	    
		while (iCommonCodes.hasNext())
		{
			YFCElement	eleCommonCode = (YFCElement)iCommonCodes.next();
			String		sOrderLineKeyOfCommonCode = eleCommonCode.getAttribute("CodeValue");
			// CommonCode Value			   =  OrderLineKey
			// CommonCode ShortDescription = "TOTALSUPPLY#TOTALDEMAND#TOTALSHORTAGE"
			// CommonCode LongDescription  = "SHIPNODE1#QTY#SHIPNODE2#QTY..."
			if (sOrderLineKeyOfCommonCode.equals(sOLK))
			{
				eleCommonCode.setAttribute("Action", "Delete");
				api.manageCommonCode(env, YFCDocument.getDocumentFor(eleCommonCode.getString()).getDocument());
			}
		}
	    System.out.println ("Exiting deleteDemandsInCommonCodeTable");
		return;
	}
	

	private double getFairShareAllocationConfiguration (YFSEnvironment env, String sEnterpriseCode, String sCustomerID) throws Exception
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		eleCommonCode.setAttribute ("CodeType", "CPG_FAIRSHARE_ALLOC");
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
		eleCommonCodes = docOut.getDocumentElement ();

		// if the CPG_FAIRSHARE_ALLOC common code table was found
		// Note Entries must be in the following format
		// CodeValue: Customer Level (TIER_0 - TIER_5)
		// CodeShortDescription: Fair Share Allocation Percentage
		// CodeLongDescription: Description
		String		sCustomerLevel = getCustomerLevel (env, sEnterpriseCode, sCustomerID);

		if (eleCommonCodes != null)
		{
			Iterator	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{	
				eleCommonCode = (YFCElement)iCommonCodes.next();
				if (YFCCommon.equals(sCustomerLevel, eleCommonCode.getAttribute ("CodeValue")))
				 return eleCommonCode.getDoubleAttribute ("CodeShortDescription");
			}
		}
		else
		{
			// Hard Code Percentages for now...Use COMMON CODE table later...
			if (sCustomerLevel.equals("TIER_0"))
			{
				return .60; // should compute to 50 after recalc
			}
			else if (sCustomerLevel.equals("TIER_1"))
			{
				return .50;  // should compute to 26 after recalc
			}
			else if (sCustomerLevel.equals("TIER_2"))
			{
				return .40;  // should compute to 14 after recalc
			}
			else if (sCustomerLevel.equals("TIER_3"))
			{
				return .30;	// should compute to 10 after recalc
			}
			else if (sCustomerLevel.equals("TIER_4"))
			{
				return .20;				
			}
			else
				return .10;
		}	
		return 0;
	}


	private String getCustomerLevel (YFSEnvironment env, String sEnterpriseCode, String sCustomerID) throws Exception
	{
		YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
		YFCElement	eleCustomer = docCustomer.getDocumentElement();
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();

		YFCDocument	docCustomerOutputTemplate = YFCDocument.getDocumentFor ("<Customer CustomerID=\"\" CustomerLevel=\"\" />");
		eleCustomer.setAttribute("OrganizationCode", sEnterpriseCode);
		eleCustomer.setAttribute("CustomerID", sCustomerID);
		env.setApiTemplate ("getCustomerDetails", docCustomerOutputTemplate.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getCustomerDetails:");
			System.out.println (docCustomer.getString());
		}
		docCustomer = YFCDocument.getDocumentFor (api.getCustomerDetails (env, docCustomer.getDocument()));
		env.clearApiTemplate ("getCustomerDetails");
		eleCustomer = docCustomer.getDocumentElement();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getCustomerDetails:");
			System.out.println (docCustomer.getString());
		}
		return eleCustomer.getAttribute ("CustomerLevel");
	}
}

