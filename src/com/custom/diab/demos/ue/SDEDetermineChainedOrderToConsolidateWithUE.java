package com.custom.diab.demos.ue;

import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSDetermineChainedOrderForConsolidationUE;

public class SDEDetermineChainedOrderToConsolidateWithUE implements YFSDetermineChainedOrderForConsolidationUE {

	private Properties	m_props;
	
	// this user exit implementation copies all of the instructions from the associated shipment 
	// release to the shipment so that
	@SuppressWarnings("unused")
	private static YFCLogCategory logger = YFCLogCategory.instance(SDEDetermineChainedOrderToConsolidateWithUE.class);
	

	@Override
	public Document determineChainedOrderForConsolidation(YFSEnvironment env, Document docIn) 
			throws YFSUserExitException {
	
		YFCDocument	docDetermineChainedOrderToConsolidateWith = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleDetermineChainedOrderToConsolidateWith = docDetermineChainedOrderToConsolidateWith.getDocumentElement();

		if (YFSUtil.getDebug())
		{
			System.out.println("Input to DetermineChainedOrderToConsolidateWithUE:");
			System.out.println(docDetermineChainedOrderToConsolidateWith.getString());
		}
		
		String 	sOrderType = eleDetermineChainedOrderToConsolidateWith.getAttribute("OrderType");
		String	sOrderTypeToConsolidateWith = m_props.getProperty("OrderTypeToConsolidate");
		String 	sDocumentTypeToConsolidateWith = m_props.getProperty("DocumentTypeToConsolidateWith");
		String	sEnterpriseCode = eleDetermineChainedOrderToConsolidateWith.getAttribute("EnterpriseCode");
		
		System.out.println ("Order Type to Consolidate With=" + sOrderTypeToConsolidateWith);
		System.out.println ("Document Type to Consolidate With=" +sDocumentTypeToConsolidateWith);
		
		// we're looking for a specific order type to consolidate with passed as an argument to the UE (e.g. FRANCHISE)
		if (YFCObject.isVoid(sOrderTypeToConsolidateWith) || YFCObject.isVoid(sDocumentTypeToConsolidateWith) || YFCObject.isVoid(sEnterpriseCode))
				return null;

		if (!sOrderType.equalsIgnoreCase(sOrderTypeToConsolidateWith))
				return null;
		
		YFCElement eleOrderLines = eleDetermineChainedOrderToConsolidateWith.getChildElement("OrderLines");
		YFCElement eleOrderLine = eleOrderLines.getFirstChildElement();
		YFCElement eleItem = eleOrderLine.getChildElement ("Item");
		String		sMfgName = eleItem.getAttribute("ManufacturerName");
		
		if (YFCObject.isVoid(sMfgName))
			return null;
		
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument	docOrderList = YFCDocument.getDocumentFor("<Order DocumentType=\"" + sDocumentTypeToConsolidateWith + "\" SellerOrganizationCode=\"" + sMfgName + "\" EnterpriseCode=\"" + sEnterpriseCode + "\"/>");
			YFCDocument	docOrderListTemplate = YFCDocument.getDocumentFor("<OrderList><Order OrderHeaderKey=\"\" DocumentType=\"\" EnterpriseCode=\"\" OrderNo=\"\" /></OrderList>");
			env.setApiTemplate("getOrderList", docOrderListTemplate.getDocument());
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getOrderList:");
				System.out.println (docOrderList.getString());
			}
			docOrderList = YFCDocument.getDocumentFor (api.getOrderList(env, docOrderList.getDocument()));
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getOrderList:");
				System.out.println (docOrderList.getString());
			}
			YFCElement	eleOrderList = docOrderList.getDocumentElement();
			YFCElement	eleOrderToConsolidateWith = eleOrderList.getFirstChildElement();
			
			if (!YFCObject.isVoid(eleOrderToConsolidateWith) && !YFCObject.isVoid(eleOrderToConsolidateWith.getAttribute("OrderNo")))
			{
				if (YFSUtil.getDebug())
				{
					System.out.println("Order Found to Consolidate With: " + eleOrderToConsolidateWith.getAttribute("OrderNo"));
					System.out.println ("Output from DetermineChainedOrderToConsolidateWithUE:");
					System.out.println (eleOrderToConsolidateWith.getString());
				}
				
				return YFCDocument.getDocumentFor(eleOrderToConsolidateWith.getString()).getDocument();
			}
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		} finally {
			env.clearApiTemplate("getOrderList");
		}
		return null;
	}
	
	public void setProperties (Properties props) throws Exception
	{
		m_props=props;
	}
	
	public Properties getProperties() {
		return m_props;
	}

}
