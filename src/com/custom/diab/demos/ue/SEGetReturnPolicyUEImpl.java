/**
  * SEGetReturnPolicyUEImpl.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.ue;

import com.yantra.pca.ycd.japi.ue.YCDGetReturnPolicyUE;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import java.util.Iterator;
import org.w3c.dom.Document;

public class SEGetReturnPolicyUEImpl 
    implements YCDGetReturnPolicyUE
{

    public SEGetReturnPolicyUEImpl()
    {
    }

    public Document getReturnPolicy(YFSEnvironment env, Document inDoc)
        throws YFSUserExitException
    {
        YFCDocument dIn = YFCDocument.getDocumentFor(inDoc);
        if(dIn == null)
        {
            return null;
        } 
		else
        {
			YFCDocument resultDoc = createUEOutput(dIn);
            return resultDoc.getDocument();
        }
    }
	
	
/*  ORIGINAL CODE FROM THE UE
    public Document getReturnPolicy(YFSEnvironment env, Document inDoc)
        throws YFSUserExitException
    {
        YFCDocument dIn = YFCDocument.getDocumentFor(inDoc);
        if(dIn == null)
        {
            return null;
        } else
        {
            YFCDocument inDocForItemList = createInputXMLForItemList(dIn);
            YFCDocument doc = YCDFoundationBridge.getInstance().getItemList(env, inDocForItemList, null);
            HashMap itemInfoList = createItemInfoList(doc);
            YFCDocument resultDoc = createUEOutput(dIn, itemInfoList);
            return resultDoc.getDocument();
        }
    }

    private YFCDocument createInputXMLForItemList(YFCDocument inDoc)
    {
		YFCElement 	returnPolicyElement = inDoc.getDocumentElement();
		YFCElement 	ordersElement = returnPolicyElement.getChildElement ("Orders");
		Iterator	iOrders = ordersElement.getChildren();
    	YFCDocument outDoc = YFCDocument.createDocument("Item");
        YFCElement	outElement = outDoc.getDocumentElement();
       	YFCElement	complexQueryElement = outElement.createChild("ComplexQuery");
        YFCELement	orElement = complexQueryElement.createChild("Or");
		while (iOrders.hasNext())
		{
        	YFCElement orderElement = (YFCElement)iOrders.next();
	        String enterpriseCode = orderElement.getAttribute("EnterpriseCode");
	        outElement.setAttribute("CallingOrganizationCode", enterpriseCode);
    	    outElement.setAttribute("IgnoreOrdering", "Y");
	        YFCNodeList nodeList = orderElement.getElementsByTagName("OrderLine");
    	    for(int i = 0; i < nodeList.getLength(); i++)
	        {
    	        YFCElement orderLineElement = (YFCElement)nodeList.item(i);
        	    YFCElement itemElement = orderLineElement.getChildElement("Item");
            	String itemId = itemElement.getAttribute("ItemID");
	            String uom = itemElement.getAttribute("UnitOfMeasure");
    	        YFCElement andElement = orElement.createChild("And");
        	    YFCElement expElement = andElement.createChild("Exp");
            	expElement.setAttribute("Name", "ItemID");
	            expElement.setAttribute("QryType", "EQ");
    	        expElement.setAttribute("Value", itemId);
        	    expElement = andElement.createChild("Exp");
            	expElement.setAttribute("Name", "UnitOfMeasure");
	            expElement.setAttribute("QryType", "EQ");
    	        expElement.setAttribute("Value", uom);
        	}
		}
        return outDoc;
    }

    private HashMap createItemInfoList(YFCDocument doc)
    {
        HashMap itemInfoList = new HashMap();
        YFCElement itemListElement = doc.getDocumentElement();
        YFCNodeList nodeList = itemListElement.getElementsByTagName("Item");
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            YFCElement itemElement = (YFCElement)nodeList.item(i);
            String itemId = itemElement.getAttribute("ItemID");
            String uom = itemElement.getAttribute("UnitOfMeasure");
            itemInfoList.put((new StringBuilder()).append(itemId).append(uom).toString(), itemElement);
        }

        return itemInfoList;
    }

    private YFCDocument createUEOutput(YFCDocument orderDocument, HashMap itemInfoList)
    {
        YFCElement originalOrderElement = orderDocument.getDocumentElement();
        String sOrderDate = originalOrderElement.getAttribute("OrderDate");
        YFCDate orderDate = null;
        String format = "";
        if(YFCDate.isBCMode())
            format = YFCDate.XML_DATE_FORMAT;
        else
            format = YFCDate.ISO_DATETIME_FORMAT;
        try
        {
            orderDate = new YFCDate(sOrderDate, format, false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        YFCNodeList nodeList = originalOrderElement.getElementsByTagName("OrderLine");
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            YFCElement orderLineElement = (YFCElement)nodeList.item(i);
            YFCElement itemElement = orderLineElement.getChildElement("Item");
            String itemId = itemElement.getAttribute("ItemID");
            String uom = itemElement.getAttribute("UnitOfMeasure");
            YFCElement itemInfo = (YFCElement)itemInfoList.get((new StringBuilder()).append(itemId).append(uom).toString());
            if(!YFCCommon.isVoid(itemInfo))
            {
                YFCElement primaryInfoElement = itemInfo.getChildElement("PrimaryInformation");
                String creditWOReceipt = primaryInfoElement.getAttribute("CreditWOReceipt");
                String isReturnable = primaryInfoElement.getAttribute("IsReturnable");
                String sReturnWindow = primaryInfoElement.getAttribute("ReturnWindow");
                if(creditWOReceipt == null)
                    creditWOReceipt = "N";
                if(isReturnable == null)
                    isReturnable = "N";
                if(sReturnWindow == null)
                    sReturnWindow = "0";
                if(isReturnable.equals("N"))
                {
                    orderLineElement.setAttribute("IsReturnable", isReturnable);
                    orderLineElement.setAttribute("ReturnPolicy", "Returns_are_not_accepted_on_this_item");
                    orderLineElement.setAttribute("CanOverridePolicy", "Y");
                    orderLineElement.setAttribute("IsReceiptExpected", "N");
                    continue;
                }
                if(compareDates(orderDate, sReturnWindow))
                {
                    orderLineElement.setAttribute("IsReturnable", "N");
                    orderLineElement.setAttribute("ReturnPolicy", "This_item_cannot_be_returned_because_the_return_window_has_expired");
                    orderLineElement.setAttribute("CanOverridePolicy", "Y");
                    orderLineElement.setAttribute("IsReceiptExpected", "N");
                    continue;
                }
                if(creditWOReceipt.equals("Y"))
                {
                    orderLineElement.setAttribute("IsReturnable", "Y");
                    orderLineElement.setAttribute("ReturnPolicy", "Return_credit_without_receipt_are_accepted_for_this_item");
                    orderLineElement.setAttribute("CanOverridePolicy", "Y");
                    orderLineElement.setAttribute("IsReceiptExpected", "Y");
                } else
                {
                    orderLineElement.setAttribute("IsReturnable", "Y");
                    orderLineElement.setAttribute("ReturnPolicy", "Returns_are_accepted_for_this_item");
                    orderLineElement.setAttribute("CanOverridePolicy", "Y");
                    orderLineElement.setAttribute("IsReceiptExpected", "N");
                }
            } else
            {
                orderLineElement.setAttribute("IsReturnable", "N");
                orderLineElement.setAttribute("ReturnPolicy", "Returns_are_not_accepted_on_this_item_because_item_information_is_not_available");
                orderLineElement.setAttribute("CanOverridePolicy", "Y");
                orderLineElement.setAttribute("IsReceiptExpected", "N");
            }
        }

        return orderDocument;
    }
*/

    @SuppressWarnings({ "rawtypes", "deprecation" })
	private YFCDocument createUEOutput(YFCDocument docReturnPolicyIn)
	{
		YFCDocument docReturnPolicyOut = YFCDocument.createDocument ("ReturnPolicy");
		YFCElement	eleReturnPolicyOut = docReturnPolicyOut.getDocumentElement();
		YFCElement	eleOrderLinesOut = null;
		
		YFCElement	eleReturnPolicyIn = docReturnPolicyIn.getDocumentElement();
		YFCElement	eleOrders = eleReturnPolicyIn.getChildElement ("Orders");
		Iterator	iOrders = eleOrders.getChildren();
		while (iOrders.hasNext ())
		{
			if (eleOrderLinesOut == null)
				eleOrderLinesOut = eleReturnPolicyOut.createChild ("OrderLines");
				
			YFCElement	eleOrder = (YFCElement)iOrders.next ();

	        String sOrderDate = eleOrder.getAttribute("OrderDate");
    	    YFCDate orderDate = null;
        	String format = "";
	        if(YFCDate.isBCMode())
    	        format = YFCDate.XML_DATE_FORMAT;
        	else
            	format = YFCDate.ISO_DATETIME_FORMAT;
	        try
    	    {
        	    orderDate = new YFCDate(sOrderDate, format, false);
	        }
    	    catch(Exception e)
        	{
	            e.printStackTrace();
    	    }


			YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
			Iterator	iOrderLines = eleOrderLines.getChildren();
			while (iOrderLines.hasNext ())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next ();
				YFCElement	eleItem = eleOrderLine.getChildElement ("ItemDetails");
				String	creditWOReceipt = null;
				String isReturnable = null;
				String sReturnWindow = null;
				YFCElement 	eleOrderLineOut = (YFCElement)eleOrderLinesOut.importNode ((YFCNode)eleOrderLine);
				if (eleItem != null)
				{
					YFCElement	elePrimaryInformation = eleItem.getChildElement ("PrimaryInformation");
        	        creditWOReceipt = elePrimaryInformation.getAttribute("CreditWOReceipt");
            	    isReturnable = elePrimaryInformation.getAttribute("IsReturnable");
                	sReturnWindow = elePrimaryInformation.getAttribute("ReturnWindow");
					if(creditWOReceipt == null)
						creditWOReceipt = "N";
        			if(isReturnable == null)
						isReturnable = "N";
                	if(sReturnWindow == null)
						sReturnWindow = "0";
					if(isReturnable.equals("N"))
					{
						eleOrderLineOut.setAttribute("IsReturnable", isReturnable);
						eleOrderLineOut.setAttribute("ReturnPolicy", "Returns_are_not_accepted_on_this_item");
                		eleOrderLineOut.setAttribute("CanOverridePolicy", "Y");
						eleOrderLineOut.setAttribute("IsReceiptExpected", "N");
	    	            continue;
					}
					if(compareDates(orderDate, sReturnWindow))
					{
						eleOrderLineOut.setAttribute("IsReturnable", "N");
						eleOrderLineOut.setAttribute("ReturnPolicy", "This_item_cannot_be_returned_because_the_return_window_has_expired");
						eleOrderLineOut.setAttribute("CanOverridePolicy", "Y");
						eleOrderLineOut.setAttribute("IsReceiptExpected", "N");
						continue;
					}
					if(creditWOReceipt.equals("Y"))
					{
						eleOrderLineOut.setAttribute("IsReturnable", "Y");
        	        	eleOrderLineOut.setAttribute("ReturnPolicy", "Return_credit_without_receipt_are_accepted_for_this_item");
						eleOrderLineOut.setAttribute("CanOverridePolicy", "Y");
						eleOrderLineOut.setAttribute("IsReceiptExpected", "N");
					} 
					else
        	  		{
						eleOrderLineOut.setAttribute("IsReturnable", "Y");
						eleOrderLineOut.setAttribute("ReturnPolicy", "Returns_are_accepted_for_this_item");
						eleOrderLineOut.setAttribute("CanOverridePolicy", "Y");
						eleOrderLineOut.setAttribute("IsReceiptExpected", "Y");
					}
				}
				else
		        {
                	eleOrderLineOut.setAttribute("IsReturnable", "N");
	                eleOrderLineOut.setAttribute("ReturnPolicy", "Returns_are_not_accepted_on_this_item_because_item_information_is_not_available");
    	            eleOrderLineOut.setAttribute("CanOverridePolicy", "Y");
        	        eleOrderLineOut.setAttribute("IsReceiptExpected", "N");
            	}

			}
		}
		return docReturnPolicyOut;
		
	}

    @SuppressWarnings("deprecation")
	private boolean compareDates(YFCDate orderDate, String sReturnWindow)
    {
        int returnWindow = Integer.parseInt(sReturnWindow);
        int returnWindowInSeconds = returnWindow * 24 * 60 * 60;
        YFCDate currentDate = new YFCDate();
        return YFCDateUtils.incrementSeconds(orderDate, returnWindowInSeconds).before(currentDate);
    }
}

