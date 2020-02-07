/**
  * SEProcessValidationResponse.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.api;

import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.interop.japi.YIFApi;
import com.custom.yantra.util.YFSUtil;
import java.util.Iterator;
import java.util.Properties;
import org.w3c.dom.Document;
import javax.xml.xpath.*;

public class SEProcessValidationResponse implements YIFCustomApi
{
    public SEProcessValidationResponse()
    {
    }


    public void setProperties(Properties prop)
        throws Exception
    {
        props = prop;
    }


	public	Document processValidationResponse (YFSEnvironment env, Document docIn) throws Exception
	{

		// This API processes a response from an external validation service that either fails or succeeds.  if the validation fails
		// the order is place on a validation hold and a change status transaction is called to update the line status accordingly
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to processValidationResonse:");
			System.out.println (YFCDocument.getDocumentFor (docIn).getString());
		}
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		String		sValidationResponseCode = eleOrder.getAttribute ("ValidationResponseCode");
		boolean		bSuccess 	= sValidationResponseCode.equalsIgnoreCase ("SUCCESS");
		boolean		bException = sValidationResponseCode.equalsIgnoreCase("EXCEPTION");

		// get order lines in the validation status		
		YFCElement	eleOrderLinesAwaitingValidation = getOrderLinesAwaitingValidation (env, eleOrder);
		
		// if successful
		if (bSuccess)
		{
			// success - update order line with SerialNumber
			YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
			Iterator<?>	iOrderLines = eleOrderLines.getChildren();
			while (iOrderLines.hasNext ())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				if (!isAwaitingValidation (eleOrderLine.getAttribute ("OrderLineKey"), eleOrderLinesAwaitingValidation))
					eleOrderLines.removeChild (eleOrderLine);
			}
		}
		else if (bException)
		{
			YFSException	eException = new YFSException ("Validation Exception", "DEMO_1000", "Validation_Exception");
			throw eException;
		}
		else
		{
			// failed validation put order on a new Validation Hold
			YFCElement	eleOrderHoldTypes = eleOrder.createChild ("OrderHoldTypes");
			YFCElement	eleOrderHoldType = eleOrderHoldTypes.createChild ("OrderHoldType");
			eleOrderHoldType.setAttribute ("HoldType", "YCD_VALIDATE_ORDER");
			eleOrderHoldType.setAttribute ("Status", "1100"); // apply the order hold
			eleOrderHoldType.setAttribute ("ReasonText", eleOrder.getAttribute ("ValidationResponseReasonText"));
		}
		
		// update the order
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to changeOrder API");
			System.out.println (docOrder.getString());
		}
		Document docOut = api.changeOrder (env, docOrder.getDocument());
		YFCDocument	docOrderOut = YFCDocument.getDocumentFor (docOut);
		YFCElement	eleOrderOut = docOrderOut.getDocumentElement ();
		eleOrderOut.setAttribute ("ValidationResponseCode", sValidationResponseCode);
		eleOrderOut.setAttribute ("ValidationResponseReasonText", eleOrder.getAttribute ("ValidationResponseReasonText"));
		
		// update order line status accordingly
		changeOrderLineStatus (env, eleOrder.getAttribute ("OrderHeaderKey"), eleOrder.getAttribute ("TransactionId"), eleOrderLinesAwaitingValidation, bSuccess);

		//
		return docOut;		
	}

	private	YFCElement getOrderLinesAwaitingValidation (YFSEnvironment env, YFCElement eleOrder) throws Exception
	{
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
		String		sOrderHeaderKey = eleOrder.getAttribute ("OrderHeaderKey");
		String		sTransactionId = eleOrder.getAttribute ("TransactionId");
		
		// get all the lines on the current order in the pickup status for this transaction					
		eleOrderLineStatusList.setAttribute ("TransactionId", sTransactionId);
		eleOrderLineStatusList.setAttribute ("OrderHeaderKey", sOrderHeaderKey);		
		docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
		return (docOrderLineStatusList.getDocumentElement());
	}

	private	boolean	isAwaitingValidation (String sOrderLineKey, YFCElement eleOrderLineStatusList)
	{
		Iterator<?>	iOrderLineStatus = eleOrderLineStatusList.getChildren();
		while (iOrderLineStatus.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatus.next();
			
			if (eleOrderLineStatus.getNodeName().equals ("OrderStatus"))
			{
				if (eleOrderLineStatus.getAttribute ("OrderLineKey").equals (sOrderLineKey))
					return true;
			}
		}
		return false;
	}
	
	private void 	changeOrderLineStatus (YFSEnvironment env, String sOrderHeaderKey, String sTransactionId, YFCElement eleOrderLineStatusList, boolean bSuccess) throws Exception
	{
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();


		// Compute Drop status based on bSuccess - Custom TX must have two drop statuses with Success Status ending in .20 and Failure Status ending in .10)
		YFCElement	eleDropStatuses = eleOrderLineStatusList.getChildElement ("DropStatuses");
		Iterator<?>	iDropStatuses = eleDropStatuses.getChildren();
		String 		sDropStatus = "";

		while (iDropStatuses.hasNext())
		{
			YFCElement	eleDropStatus = (YFCElement)iDropStatuses.next();
			sDropStatus = eleDropStatus.getAttribute ("Status");
			if (bSuccess)
			{
				// look for success sub-status (ends in .20)
				if (sDropStatus.indexOf(".20") >= 0)
					break;
			}
			else
			{
				// look for failure sub-status (ends in .10)
				if (sDropStatus.indexOf(".10") >= 0)
					break;
			}				
		}

		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
		
		eleOrderStatusChange.setAttribute ("OrderHeaderKey", sOrderHeaderKey);
		eleOrderStatusChange.setAttribute ("TransactionId", sTransactionId);
		YFCElement	eleOrderLines = eleOrderStatusChange.createChild ("OrderLines");

		// for each of the orderlines in this order header key
		Iterator<?>	iOrderLineStatus = eleOrderLineStatusList.getChildren();

		while (iOrderLineStatus.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatus.next();
			
			if (eleOrderLineStatus.getNodeName().equals ("OrderStatus"))
			{
				YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
				eleOrderLine.setAttribute ("OrderLineKey", eleOrderLineStatus.getAttribute ("OrderLineKey"));
				eleOrderLine.setAttribute ("BaseDropStatus", sDropStatus);
				eleOrderLine.setAttribute ("ChangeForAllAvailableQty", "Y");
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to changeOrderStatus:");
			System.out.println (docOrderStatusChange.getString());
		}
		api.changeOrderStatus (env, docOrderStatusChange.getDocument());		
	}	


	@SuppressWarnings("unused")
	private String evaluateXPathExpression (String sXPathExpr, Document docIn) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, docIn, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;			
		}
		return sResult;
	}	
	
	public Properties getProperties() {
		return props;
	}

	private Properties props;
}

