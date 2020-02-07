package com.custom.diab.demos.ue;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;


import com.custom.diab.demos.api.SEWebOrderExtensions;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;

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
		
		if (!YFCObject.isVoid(sEnterpriseCode) && sEnterpriseCode.startsWith("AuroraCPG")
		&& !YFCObject.isVoid(sBuyerOrganizationCode) && sBuyerOrganizationCode.startsWith("CPG"))
		{
			convertUPCToCustomerItem (env, eleOrder);
			
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
