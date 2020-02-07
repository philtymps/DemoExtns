/**
  * OrderExtns.java
  *
  **/

/**
 * @author Phil Tympanick
 *
 */

package com.custom.yantra.api;

import	org.w3c.dom.*;
import	java.util.*;
import com.custom.yantra.util.*;
import com.custom.yantra.xmlwrapper.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCException;
import com.custom.yantra.orders.*;

public class OrderExtns implements YIFCustomApi
{
	public OrderExtns () { }

	public static final String ARG_PROPERTY_KEY = "arg";
	public static final String ARG_REASON_CODE = "ReasonCode";
	public static final String ARG_LINE_TYPE = "LineType";
	
	public Document createShipmentForRelease(YFSEnvironment env, Document inDoc) throws YFCException
	{
	  try {
		if (YFSUtil.getDebug ())
		{
			System.out.println("In Custom API: createShipmentForRelease-Input Document:");
			System.out.println(YFSXMLUtil.getXMLString (inDoc));
		}

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement inElem = yfcInDoc.getDocumentElement();

		YFCDocument yfcShipmentInDoc = YFCDocument.createDocument("Shipment");
		YFCElement yfcShipmentInElem = yfcShipmentInDoc.getDocumentElement();
		yfcShipmentInElem.setAttribute("Action","Create");
		if (inElem.getAttribute("DocumentType") != null)
			yfcShipmentInElem.setAttribute("DocumentType",inElem.getAttribute ("DocumentType"));
		else	
			yfcShipmentInElem.setAttribute("DocumentType",inElem.getAttribute ("0001"));
		
		YFCElement orderReleasesElem = yfcShipmentInElem.createChild("OrderReleases");
		YFCElement orderReleaseElem = orderReleasesElem.createChild("OrderRelease");
		orderReleaseElem.setAttribute("AssociationAction","Add");
	
		orderReleaseElem.setAttribute("OrderReleaseKey",inElem.getAttribute("OrderReleaseKey"));
		if (YFSUtil.getDebug())
		{
			System.out.println("Exiting Custom API: prepareInputForCreateShipment-Output Document:");
			System.out.println(YFSXMLUtil.getXMLString (yfcShipmentInDoc.getDocument()));
		}
		return yfcShipmentInDoc.getDocument();
	  } catch (Exception e) {
	  	throw new YFCException (e);
	  }
	}

	public Document createReturnOrder (YFSEnvironment env, Document docIn) throws YFCException
	{
	  YFSEnvironment oldEnv = YFSUtil.pushYFSEnv();
	  YFSUtil.setYFSEnv (env);

	  try {
		// can pass an Order, OrderSchedule or OrderRelease element to this API.  Root element need only contain
		// the OrderHeaderKey
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		YantraOrder	oOrder = new YantraOrder();
		oOrder.setOrderHeaderKey (docOrder.getDocumentElement().getAttribute ("OrderHeaderKey"));
		oOrder.getOrderDetails ();
		String	sReasonCode, sLineType;

		// get or default the ReasonCode and LineType values		
		sReasonCode = getProperties().getProperty("ReasonCode");
		sLineType = getProperties().getProperty("LineType");
			
		if (sReasonCode == null)
			sReasonCode = "DAMAGED";
		if (sLineType == null)
			sLineType = "CREDIT";
		YFCDocument docRetOrder = YFCDocument.getDocumentFor (oOrder.returnOrder (sReasonCode, sLineType));
		return (docRetOrder.getDocument());
		
	  } catch (Exception e) {
	  	throw new YFCException (e);
	  } finally {
	  	YFSUtil.popYFSEnv (oldEnv);
	  }
	}
	
	public Document SubstituteOnBackOrderEvent (YFSEnvironment env, Document docIn) throws YFCException
	{
	  try {
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to SubstituteOnBackOrderEvent() API:");
			System.out.println (YFSXMLUtil.getXMLString (docIn));
		}

		YFSScheduleOnBackOrderEventDoc	oBackOrder = new YFSScheduleOnBackOrderEventDoc (YFSXMLUtil.getXMLString(docIn));
		Hashtable<String, String> htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", oBackOrder.getOrder().getAttribute("OrderHeaderKey"));
		htOrder.put ("Override", "Y");
				
		YFSXMLParser	inXml = new YFSXMLParser ();
		YIFApi	api = YFSUtil.getYIFApi ();		
		Element	eleOrder = inXml.createRootElement ("Order", htOrder);
		YFSScheduleOnBackOrderEventDoc.OrderLines	oOrderLines = oBackOrder.getOrder().getOrderLines();
		Document	docOut = null;
		
		// if order lines found
		if (oOrderLines != null)
		{
			// create orderlines element
			Element	eleOrderLines = inXml.createChild (eleOrder, "OrderLines", null);	
			Enumeration<?>	enumOrderLines = oOrderLines.getOrderLineList();
			
			// create splitline element
			while (enumOrderLines.hasMoreElements ())
			{
				YFSScheduleOnBackOrderEventDoc.OrderLine oOrderLine = (YFSScheduleOnBackOrderEventDoc.OrderLine)enumOrderLines.nextElement();

				Hashtable<String, String>	htItemToSubstitute = new Hashtable<String, String>();
				htItemToSubstitute.put ("OrganizationCode", oBackOrder.getOrder().getAttribute ("EnterpriseCode"));
				htItemToSubstitute.put ("AssociationType", "Substitutions");
				htItemToSubstitute.put ("ItemID", oOrderLine.getItem().getAttribute ("ItemID"));
				htItemToSubstitute.put ("ProductClass", oOrderLine.getItem().getAttribute ("ProductClass"));
				htItemToSubstitute.put ("UnitOfMeasure", oOrderLine.getItem().getAttribute ("UnitOfMeasure"));

				// get substitutions for backordered item
				Hashtable<String, String>	htSubstitutedItem = getItemAssociation (env, htItemToSubstitute);
				YFSScheduleOnBackOrderEventDoc.StatusBreakupForBackOrderedQty  oStatusBreakupForBackorderedQty = oOrderLine.getStatusBreakupForBackOrderedQty ();				

				// if substitute found for the first backordered item
				if (htSubstitutedItem != null && oStatusBreakupForBackorderedQty != null)
				{
					String	sBackOrderedQty, sStatus;
					oOrderLine.getAttribute ("OrderedQty");
					sBackOrderedQty = "0";
					Properties	oProp = getProperties();
					// custom backorder statuses can be passed using the Arguments tab for this API
					// set BackOrderSatus=Your Extended Backorder Status Code
					sStatus = oProp.getProperty("BackOrderStatus");
					if (sStatus == null || sStatus.length() == 0)										
						sStatus = "1300"; // assume plain old back ordered status
						
					Enumeration<?> enumBackOrderedFrom = oStatusBreakupForBackorderedQty.getBackOrderedFromList ();						

					// split quantity with substitute
					if (!enumBackOrderedFrom.hasMoreElements ())
					  continue;

					YFSScheduleOnBackOrderEventDoc.BackOrderedFrom oBackOrderedFrom = (YFSScheduleOnBackOrderEventDoc.BackOrderedFrom)enumBackOrderedFrom.nextElement();
					sBackOrderedQty = oBackOrderedFrom.getAttribute ("BackOrderedQuantity");
					//sStatus = oBackOrderedFrom.getAttribute ("Status");

					Hashtable<String, String>	htOrderLine	= new Hashtable<String, String>();
					htOrderLine.put ("OrderLineKey", oOrderLine.getAttribute ("OrderLineKey"));
					htOrderLine.put ("QuantityToSplit", sBackOrderedQty);
					htOrderLine.put ("FromStatus", sStatus);
					Element eleOrderLine = inXml.createChild (eleOrderLines, "OrderLine", htOrderLine);
					
					Hashtable<String, String>	htSplitLine = new Hashtable<String, String>();
					htSplitLine.put ("OrderedQty", sBackOrderedQty);
					htSplitLine.put ("LineType", oOrderLine.getAttribute ("LineType"));

					Element eleSplitLines = inXml.createChild (eleOrderLine, "SplitLines", null);
					Element eleSplitLine  = inXml.createChild (eleSplitLines, "SplitLine", htSplitLine);
					inXml.createChild (eleSplitLine, "Item", htSubstitutedItem);
					if (YFSUtil.getDebug ())
					{
						System.out.println ("splitLine() API Input: ");
						System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
					}
					docOut = api.splitLine (env, inXml.getDocument());					
					if (YFSUtil.getDebug())
					{
						System.out.println ("splitLine() API Output:");
						System.out.println (YFSXMLUtil.getXMLString (docOut));
					}
				}
			}
		}
	  } catch (Exception e) {
	  	throw new YFCException (e);
	  }
	  return docIn;	
	}
	
	protected	Hashtable<String, String>	getItemAssociation (YFSEnvironment env, Hashtable<String, String> htItem) throws Exception
	{
		new Hashtable<Object, Object> ();
		YIFApi	api = YFSUtil.getYIFApi ();								
		Hashtable<String, String>	htAssociatedItem = null;
		
		YFSXMLParser	inXml = new YFSXMLParser ();
		inXml.createRootElement ("AssociationList", htItem);

		if (YFSUtil.getDebug ())
		{
			System.out.println ("getItemAssociations() API Input: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		YFCDocument	docOut = YFCDocument.getDocumentFor (api.getItemAssociations(env, inXml.getDocument()));
		YFCElement eleAssociationList = docOut.getDocumentElement ();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getItemAssociations() API:");
			System.out.println (docOut.getString());
		}	
		
		if (eleAssociationList != null)
		{		
			Iterator<?> iAssociationList = eleAssociationList.getChildren();
			if (iAssociationList.hasNext ())
			{
				YFCElement	eleAssociation = (YFCElement)iAssociationList.next();
				YFCElement	eleItem = eleAssociation.getChildElement ("Item");
				htAssociatedItem = new Hashtable<String, String>();
				htAssociatedItem.put ("ItemID", eleItem.getAttribute ("ItemID"));				
				htAssociatedItem.put ("ProductClass", eleItem.getAttribute ("ProductClass"));
				htAssociatedItem.put ("UnitOfMeasure", eleItem.getAttribute ("UnitOfMeasure"));
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Assocation Found for Item: "+htItem.get("ItemID"));
					System.out.println ("ItemID="+htAssociatedItem.get("ItemID")+" UnitOfMeasure="+htAssociatedItem.get ("UnitOfMeasure")+" ProductClass="+htAssociatedItem.get("ProductClass"));
				}
			}
		}
		return htAssociatedItem;		
	}
	
	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
	}

	public Properties getProperties()
	{
		return mProp;
	}

	private Properties mProp;	
	
}
