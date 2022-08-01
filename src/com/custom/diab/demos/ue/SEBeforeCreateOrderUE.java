package com.custom.diab.demos.ue;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;


import com.custom.diab.demos.api.SEWebOrderExtensions;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;
import com.custom.diab.demos.api.CPGGetCustomerItemDemand;

public class SEBeforeCreateOrderUE implements YFSBeforeCreateOrderUE {

	private	Properties m_props;
	
	@Override
	public String beforeCreateOrder(YFSEnvironment env, String sInXML)
			throws YFSUserExitException {
		return YFCDocument.getDocumentFor(beforeCreateOrder (env, YFCDocument.getDocumentFor(sInXML).getDocument())).getString();
	}

	@Override
	public Document beforeCreateOrder(YFSEnvironment env, Document docInXML) throws YFSUserExitException {
		// TODO Auto-generated method stub
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docInXML);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		
		// CPG Demo Only
		String		sBuyerOrganizationCode = eleOrder.getAttribute ("BuyerOrganizationCode");
		String		sEnterpriseCode = eleOrder.getAttribute("EnterpriseCode");
		if (YFSUtil.getDebug())
		{
			System.out.println("Input to beforeCreateOrder UE:");
			System.out.println(docOrder.getString());
		}
		
		if (!eleOrder.getAttribute("DocumentType").equals("0001"))
			return docInXML;
		
		if (!YFCObject.isVoid(sEnterpriseCode) && sEnterpriseCode.startsWith("AuroraCPG"))
		{
			
			if (YFCObject.isVoid(sBuyerOrganizationCode) && YFCObject.isVoid(eleOrder.getAttribute("BillToID")))
				eleOrder.setAttribute("BillToID", eleOrder.getAttribute("CustomerID"));

			convertUPCToCustomerItem (env, eleOrder);
			
			// If no BuyerOrganizationCode or BillToID is passed, use CustomerID
			/*  DOT FOODS Customization
			CPGGetCustomerItemDemand	cpgCID = new CPGGetCustomerItemDemand ();
			eleOrder.setAttribute ("PriorityCode", cpgCID.getCustomerLevel(env, eleOrder));
			*/
			
			/* WINE SHIPPING Customization */
			String	sOrderType = eleOrder.getAttribute("OrderType");
			if (sOrderType.equals("CONTAINERIZED"))
			{
				String	sCrateItem =  getCrateRequired(eleOrder);
				System.out.println ("Crate Required=" + sCrateItem);

				
				if (!YFCObject.isNull(sCrateItem))
				{
					YFCElement	eleOrderLines;
					YFCElement	eleOrderLine;
					YFCElement	eleItem;
					eleOrderLines = eleOrder.getChildElement("OrderLines");
					eleOrderLine = eleOrderLines.createChild("OrderLine");
					eleOrderLine.setAttribute("PrimeLineNo", "1");
					eleOrderLine.setAttribute("SubLineNo", "2");
					eleOrderLine.setAttribute("FulfillmenType", "D2C_PRODUCT_FULFILLMENT");
					eleOrderLine.setAttribute("DeliveryMethod", "SHP");
					eleOrderLine.setAttribute("SCAC", "Y_ANY");
					eleOrderLine.setAttribute("CarrierServiceCode", "STANDARD_AURE");
					eleItem = eleOrderLine.createChild("Item");
					eleItem.setAttribute("ItemID", sCrateItem);
					eleItem.setAttribute("UnitOfMeasure", "EACH");
					eleOrderLine.setAttribute("OrderedQty", "1");
				}
				
			}
			if (YFSUtil.getDebug())
			{
				System.out.println("Output from beforeCreateOrder UE:");
				System.out.println(docOrder.getString());
			}
			return docOrder.getDocument();
		}
		else if (!YFCObject.isVoid (sEnterpriseCode) && sEnterpriseCode.equals("Aurora"))
		{
			SEWebOrderExtensions seWebExt = new SEWebOrderExtensions();
			return seWebExt.updateOrderSourcingClassification (env, seWebExt.manageCustomerOnOrder(env, docInXML));
		}
		else
			return docInXML;
	}
	
	protected	String	getCrateRequired (YFCElement eleOrder)
	{
		String	sCrateTypeRequired = null;
		
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator<YFCElement>	iOrderLines = eleOrderLines.getChildren();
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = iOrderLines.next();
			String		sUnitOfMeasure = eleOrderLine.getChildElement("Item").getAttribute ("UnitOfMeasure");
			if (sUnitOfMeasure.contains("W"))
					return "VIN_CRATE_WOOD";
			else
					return "VIN_CRATE_STANDARD";
		}
		return sCrateTypeRequired;
	}
	
	protected boolean	IsD2C(YFSEnvironment env, YFCElement eleOrder) throws Exception
	{
		String	sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
		String	sCustomerID = eleOrder.getAttribute("CustomerID");
		boolean	bRet = false;
		
		if (YFCObject.isVoid(sCustomerID))
			sCustomerID = eleOrder.getAttribute("BillToID");
		
		if (!YFCObject.isVoid(sCustomerID))
		{
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
			YFCElement	eleCustomer = docCustomer.getDocumentElement();
			
			YFCDocument	docCustomerOutputTemplate = YFCDocument.getDocumentFor ("<Customer CustomerID=\"\" CustomerType=\"\"/>");
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

			// if CustomerType=02
			if (!YFCObject.isVoid(eleCustomer.getAttribute("CustomerType")))
			{
				// get the values to test from Condition Args (OrderType, CustomerLevel)
				String sTestCustomerType = (String) eleCustomer.getAttribute("CustomerType");

				if (sTestCustomerType.equals ("02"))				
					bRet = true;
			}
		}
		return bRet;
	}
		

	public void setProperties (Properties props) throws Exception
	{
		m_props=props;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void convertUPCToCustomerItem (YFSEnvironment env, YFCElement eleOrder)
	{
		String		sBuyerOrganizationCode = eleOrder.getAttribute ("BuyerOrganizationCode");
		String		sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
		
		if (YFCObject.isVoid(sBuyerOrganizationCode))
			return;
		
		Hashtable	htCustomerItems = new Hashtable();
		getCustomerItemTable (env, sEnterpriseCode, sBuyerOrganizationCode, htCustomerItems);
		
		// update the order lines converting UPC to Customer Item
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
			String		sCustomerItemID = (String)htCustomerItems.get(eleItem.getAttribute ("ItemID"));
			if (!YFCObject.isVoid (sCustomerItemID))
			{
				eleItem.setAttribute ("CustomerItem", eleItem.getAttribute("ItemID"));
				eleItem.setAttribute("ItemID", sCustomerItemID);
			}
		}			
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void	getCustomerItemTable (YFSEnvironment env, String sEnterpriseCode, String sBuyerOrganizationCode, Hashtable htCustomerItems) 
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		
		eleCommonCode.setAttribute ("CodeType", sBuyerOrganizationCode);
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception eIgnore) {
			return;
		}
		if (!YFCObject.isNull(eleCommonCodes))
		{
			Iterator	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{
				eleCommonCode = (YFCElement)iCommonCodes.next();
				// Common Code has CodeValue=UPC and CodeShortDescription=ItemID
				htCustomerItems.put(eleCommonCode.getAttribute ("CodeValue"), eleCommonCode.getAttribute("CodeShortDescription"));
			}
		}
		return;
	}
	
	@SuppressWarnings("rawtypes")
	public void updateOrderLineSegments (YFCElement eleOrder)
	{
		// use OrderType, EntryType or AuthorizedClient attribute from Order to segment the order lines
		String		sSegmentName = eleOrder.getAttribute ("OrderType");
		if (YFCObject.isNull(sSegmentName));
			sSegmentName = eleOrder.getAttribute("EntryType");
		if (YFCObject.isNull(sSegmentName));
			sSegmentName = eleOrder.getAttribute("AuthorizedClient");
		
		// if the OrderType, EntryType or AuthorizedClient are all not present don't do anything to the order
		if (YFCObject.isNull(sSegmentName))
			return;

		// segment the order lines using the order header attribute (OrderType, EntrypType or AuthroizedClient) value passed
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			eleOrderLine.setAttribute ("Segment", sSegmentName);
		}			
	
	}

	public Properties getProperties() {
		return m_props;
	}
}
