package com.custom.diab.demos.api;

import java.util.Iterator;
import java.util.Properties;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.w3c.dom.Document;

public class CPGUpdateForecastForOrder implements YIFCustomApi {

	
	
	public	Document	UpdateForecastForOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCElement	eleOrder  = docOrder.getDocumentElement();
        
        // create a multiApi Input document to create any exceptions when updating the forecast
        YFCDocument	docMultiApi = YFCDocument.createDocument("MultiApi");
        YFCElement	eleMultiApi = docMultiApi.getDocumentElement();
        eleMultiApi.setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey"));
        eleMultiApi.setAttribute("OrderNo", eleOrder.getAttribute("OrderNo"));
        eleMultiApi.setAttribute("EnterpriseCode", eleOrder.getAttribute ("EnterpriseCode"));
        eleMultiApi.setAttribute("BuyerOrganizationCode", eleOrder.getAttribute("BuyerOrganizationCode"));
        
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering UpdateForecastForOrder - Input:");
        	System.out.println (docOrder.getString());
        }
        if (eleOrder.getAttribute("OrderType").equalsIgnoreCase("REPLEN"))
        {
        	YFCDocument	docForecastOrder = YFCDocument.createDocument("Order");
        	YFCElement	eleForecastOrder = docForecastOrder.getDocumentElement();
        	eleForecastOrder.setAttribute("DocumentType", "0002");
        	eleForecastOrder.setAttribute("BuyerOrganizationCode", eleOrder.getAttribute ("BuyerOrganizationCode"));
        	eleForecastOrder.setAttribute("EnterpriseCode", eleOrder.getAttribute ("EnterpriseCode"));
        	eleForecastOrder.setAttribute("OrderType", "Contract");

        	if (YFSUtil.getDebug())
        	{
        		System.out.println ("Input to getOrderList API:");
        		System.out.println (docForecastOrder.getString());
        	}
        	try {
    			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
    			YFCDocument	docForecastList = YFCDocument.getDocumentFor(api.getOrderList(env, docForecastOrder.getDocument()));
    			YFCElement	eleForecastList = docForecastList.getDocumentElement();
    			
    			if (eleForecastList.getIntAttribute("TotalOrderList") > 0)
    			{
    				// get first child order - assumes only one contract order for each customer
    				eleForecastOrder = eleForecastList.getFirstChildElement();
    				
    				// create API template for updating the planned order
    				YFCDocument		docForecastOrderTemplate = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"\" OrderNo=\"\" BuyerOrganizationCode=\"\" EnterpriseCode=\"\" DocumentType=\"\" />");
    				YFCElement		eleForecastOrderTemplate = docForecastOrderTemplate.getDocumentElement();
    				YFCElement		eleForecastOrderTemplateLines = eleForecastOrderTemplate.createChild("OrderLines");
    				YFCElement		eleForecastOrderTemplateLine = eleForecastOrderTemplateLines.createChild("OrderLine");
    				YFCElement		eleForecastOrderTemplateItem = eleForecastOrderTemplateLine.createChild("Item");
    				
    				eleForecastOrderTemplateLine.setAttribute("OrderLineKey", "");
    				eleForecastOrderTemplateLine.setAttribute("OrderedQty", "");
    				eleForecastOrderTemplateItem.setAttribute("ItemKey", "");
    				eleForecastOrderTemplateItem.setAttribute("ItemID", "");
    				eleForecastOrderTemplateItem.setAttribute("UnitOfMeasure", "");
    				env.setApiTemplate("getOrderDetails", docForecastOrderTemplate.getDocument());
    				
    				// call getOrderDetails to get the Planned Order Details
    				docForecastOrder = YFCDocument.getDocumentFor(api.getOrderDetails(env, YFCDocument.getDocumentFor(eleForecastOrder.getString()).getDocument()));

    				// clear the getOrderDetails template
    				env.clearApiTemplate("getOrderDetails");        		
    				
    				// if exception generated from updating the forecast
    				if (PrepareForecastOrder (docOrder, docForecastOrder, docMultiApi))
    					api.multiApi(env, docMultiApi.getDocument());

    				if (YFSUtil.getDebug())
    				{
    					System.out.println("Updating Forecast Order - Input to changeOrder API:");
    					System.out.println(docForecastOrder.getString());
    				}
					api.changeOrder (env, docForecastOrder.getDocument());
    			}
        	} catch (Exception e) {
        		throw new YFSException(e.getMessage());
        	}
        }
        return docIn;
	}
	
	@SuppressWarnings("rawtypes")
	protected	boolean	PrepareForecastOrder (YFCDocument docOrder, YFCDocument docForecastOrder, YFCDocument docMultiApi)
	{
		
		YFCElement	eleOrderLines	=	docOrder.getDocumentElement().getChildElement("OrderLines");
		YFCElement	eleForecastOrderLines = docForecastOrder.getDocumentElement().getChildElement("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
		boolean		bException = false;
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering PrepareForecastOrder");
			System.out.println ("Order Document:" + docOrder.getString());
			System.out.println ("Forecast Document:" + docForecastOrder.getString());
		}

		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			YFCElement	eleForecastLine = findMatchingForecastLine (eleOrderLine, eleForecastOrderLines);
			if (!YFCObject.isNull(eleForecastLine))
			{
				if (updateForecastLine(docMultiApi, eleOrderLine, eleForecastLine))
					bException = true;
			}
		}
		return bException;
	}
	
	@SuppressWarnings("rawtypes")
	protected	YFCElement	findMatchingForecastLine (YFCElement eleOrderLine, YFCElement eleForecastOrderLines)
	{
		Iterator	iForecastOrderLines = eleForecastOrderLines.getChildren();
		while (iForecastOrderLines.hasNext())
		{
			YFCElement	eleForecastOrderLine = (YFCElement)iForecastOrderLines.next();
			YFCElement	eleForecastItem = eleForecastOrderLine.getChildElement("Item");
			YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
			
			if (eleItem.getAttribute ("ItemID").equals(eleForecastItem.getAttribute("ItemID"))
			&&  eleItem.getAttribute ("UnitOfMeasure").equals(eleForecastItem.getAttribute("UnitOfMeasure")))
				return eleForecastOrderLine;
		}
		return null;
	}
	
	protected	boolean	updateForecastLine (YFCDocument docMultiApi, YFCElement eleOrderLine, YFCElement eleForecastOrderLine)
	{

		double	dblOrderLineQty = eleOrderLine.getDoubleAttribute("OrderedQty");
		double  dblForecastOrderLineQty = eleForecastOrderLine.getDoubleAttribute("OrderedQty");		

		// if we haven't exceeded our forecasted quantity
		if (dblOrderLineQty < dblForecastOrderLineQty)
		{
			dblForecastOrderLineQty = dblForecastOrderLineQty - dblOrderLineQty;
			eleForecastOrderLine.setDoubleAttribute ("OrderedQty", dblForecastOrderLineQty);
			return false;
		}
		else
		{
			eleForecastOrderLine.setDoubleAttribute ("OrderedQty", 0.00);
			createContractViolationExceptionXML (docMultiApi, eleOrderLine);
			
			return true;
		}
	}
	
	public void createContractViolationExceptionXML (YFCDocument docMultiApi, YFCElement eleOrderLine)
	{
		YFCElement	eleMultiApi = docMultiApi.getDocumentElement();
		YFCElement	eleAPI = eleMultiApi.createChild("API");
		eleAPI.setAttribute("Name", "createException");
		YFCElement	eleInput = eleAPI.createChild("Input");
		YFCElement	eleException = eleInput.createChild("Inbox");
		
		
		//eleException.setAttribute("QueueId", "YCD_ANNOUNCEMENT_"+sShipNode.toUpperCase());
		eleException.setAttribute("Description", "A Contract Violoation Has Occured");
		eleException.setAttribute("DetailDescription", "A Contract Violoation Has Occured for Customer " + eleMultiApi.getAttribute("BuyerOrganizationCode") + " for Item ID " + eleOrderLine.getChildElement("Item").getAttribute("ItemID"));
		eleException.setAttribute("ExceptionType", "CPG_CONTRACT_VIOLATION");
		eleException.setAttribute("ExpirationDays", "1");
		eleException.setAttribute("EnterpriseKey", eleMultiApi.getAttribute("EnterpriseCode"));
		eleException.setAttribute("OrderHeaderKey", eleMultiApi.getAttribute("OrderHeaderKey"));
		eleException.setAttribute("OrderNo", eleMultiApi.getAttribute("OrderNo"));
		eleException.setAttribute("OrderLineKey", eleOrderLine.getAttribute ("OrderLineKey"));
		eleException.setAttribute("ItemKey", eleOrderLine.getChildElement("Item").getAttribute("ItemKey"));
		eleException.setAttribute("OwnerKey", eleMultiApi.getAttribute("EnterpriseCode"));
	}
	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}
	Properties	m_Props;
}
