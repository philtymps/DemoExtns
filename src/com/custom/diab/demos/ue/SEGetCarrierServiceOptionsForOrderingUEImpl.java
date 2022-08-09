/**
  * SEGetCarrierServiceOptionsForOrderingUEImpl.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.ue;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetCarrierServiceOptionsForOrderingUE;

import java.util.Hashtable;
import java.util.Iterator;
import org.w3c.dom.Document;

@SuppressWarnings("deprecation")
public class SEGetCarrierServiceOptionsForOrderingUEImpl implements OMPGetCarrierServiceOptionsForOrderingUE
{
    public SEGetCarrierServiceOptionsForOrderingUEImpl()
    {
    }

    public Document getCarrierServiceOptionsForOrdering(YFSEnvironment env, Document inDoc) throws YFSUserExitException
    {
        YFCDocument dIn = YFCDocument.getDocumentFor(inDoc);
        Document dOut = null;
        if(dIn != null)
            dOut = processUE(env, dIn);
        return dOut;
    }

    @SuppressWarnings({ "rawtypes" })
	private Document processUE (YFSEnvironment env, YFCDocument doc) throws YFSUserExitException
    {
        YFCElement	eleOrder = doc.getDocumentElement();
        YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();

		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering SEGetCarrierServiceOptionsForOrderingUEImpl Input:");
			System.out.println (doc.getString());
		}
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		eleCommonCode.setAttribute ("CodeType", "DEMO_SHIPPINGCHARGES");
		eleCommonCode.setAttribute("CallingOrganizationCode", eleOrder.getAttribute ("EnterpriseCode"));
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Input to getCommonCodeList() API:");
			System.out.println (docCommonCode.getString());
		}
		YFCElement	eleCommonCodes = null;
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			if (YFSUtil.getDebug()) {
				System.out.println ("Output from getCommonCodeList() API:");
				System.out.println (docOut.getString());
			}
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception e) {
			throw (new YFSUserExitException (e.getMessage()));
		}
		// if the DEMO_SHIPPINGCHARGES common code table was found
		if (eleCommonCodes != null)
		{
			while (iOrderLines.hasNext ())
	        {
	            YFCElement eleOrderLine = (YFCElement)iOrderLines.next();
	            YFCElement eleCarrierServiceList = eleOrderLine.getChildElement("CarrierServiceList");
	            if(YFCCommon.isVoid(eleCarrierServiceList))
	                continue;
	            
	            Iterator iCarrierServiceList = eleCarrierServiceList.getChildren();
	            while (iCarrierServiceList.hasNext())
				{
	                YFCElement carrierServiceElement = (YFCElement)iCarrierServiceList.next();
	                String carrierServiceCode = carrierServiceElement.getAttribute("CarrierServiceCode");
					// Use a common-code lookup if it's configured
		    		if (eleCommonCodes != null)
		    		{
		    			Iterator	iCommonCodes = eleCommonCodes.getChildren();
	    				
		    			while (iCommonCodes.hasNext())
		    			{	
		    				eleCommonCode = (YFCElement)iCommonCodes.next();
		    				if (YFCCommon.equals (carrierServiceCode, eleCommonCode.getAttribute ("CodeValue")))
		    				{
		    					carrierServiceElement.setAttribute("Price", eleCommonCode.getAttribute("CodeShortDescription"));
		    					carrierServiceElement.setAttribute("Currency", eleCommonCode.getAttribute ("CodeLongDescription"));
		    					break;
		    				}
		    			}	  				
		    		}
				}
	        }
		}
		// remove carrier service options that don't apply or make sense for specific items
		String						sBillToID = getBillToID(env, eleOrder);
		Hashtable<String,String>	htCustomerCarriersAndServices = new Hashtable<String, String>();
		
		if (!YFCObject.isNull("sBillToID"))
		{
			docCommonCode = YFCDocument.createDocument("CommonCode");
			eleCommonCode = docCommonCode.getDocumentElement();
			
			eleCommonCode.setAttribute ("CodeType", "CPG_SHIPPING_GROUPS");
			eleCommonCode.setAttribute("CallingOrganizationCode", eleOrder.getAttribute ("EnterpriseCode"));
			eleCommonCodes = null;
			try {
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
				if (YFSUtil.getDebug()) {
					System.out.println ("Input to getCommonCodeList() API:");
					System.out.println (docCommonCode.getString());
				}
				YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
				if (YFSUtil.getDebug()) {
					System.out.println ("Output from getCommonCodeList() API:");
					System.out.println (docOut.getString());
				}
				eleCommonCodes = docOut.getDocumentElement ();
			} catch (Exception e) {
				throw (new YFSUserExitException (e.getMessage()));
			}
			Iterator<YFCElement>	iCommonCodes = eleCommonCodes.getChildren();
			// add any carrier services that are configured for this customer in common code table to the table
			while (iCommonCodes.hasNext())
			{
				eleCommonCode = iCommonCodes.next();
				String	sCodeValue = eleCommonCode.getAttribute("CodeValue");
				if (sCodeValue.startsWith(sBillToID))
					htCustomerCarriersAndServices.put(eleCommonCode.getAttribute("CodeShortDescription"), sCodeValue);				
			}			
		}
		
		iOrderLines = eleOrderLines.getChildren();
		while (iOrderLines.hasNext ())
        {
            YFCElement eleOrderLine = (YFCElement)iOrderLines.next();
            YFCElement eleCarrierServiceList = eleOrderLine.getChildElement("CarrierServiceList");
            if(YFCCommon.isVoid(eleCarrierServiceList))
                continue;

        	Iterator iCarrierServiceList = eleCarrierServiceList.getChildren();
        	while (iCarrierServiceList.hasNext())
        	{
        		YFCElement eleCarrierServiceElement = (YFCElement)iCarrierServiceList.next();
        		String carrierServiceCode = eleCarrierServiceElement.getAttribute("CarrierServiceCode");
        		// if we can't deliver to the customer same day (Configured Miles Max for Same Day in carrier service setup)
        		// remove that option
        		if (carrierServiceCode.contains("SAMEDAY"))
        		{
        			YFCDate	dtToday = new YFCDate (System.currentTimeMillis());
        			YFCDate dtDeliveryEnd   = eleCarrierServiceElement.getDateAttribute("DeliveryEndDate");
            		// if we can't deliver to the customer same day (Configured for 50 Miles Max for Same Day)
            		// remove that option
        			if (dtDeliveryEnd.after(dtToday))
        			{
        				eleCarrierServiceList.removeChild(eleCarrierServiceElement);
        				iCarrierServiceList = eleCarrierServiceList.getChildren();
        			}            				
        		}
        		// if carrier services for this customer are configured in the common code table
        		if (!htCustomerCarriersAndServices.isEmpty())
        		{
            		/* remove any carrier services not configured for this customer	*/
        			if (htCustomerCarriersAndServices.get(carrierServiceCode) == null)
        			{
        				eleCarrierServiceList.removeChild(eleCarrierServiceElement);
            			iCarrierServiceList = eleCarrierServiceList.getChildren();
        			}
        		}
        	}
        	/*
        	 * FED EX DEMO CODE
            String sItemID = eleOrderLine.getChildElement("Item").getAttribute("ItemID");
            if (sItemID.startsWith("FX_SHIP"))
            {	
            	iCarrierServiceList = eleCarrierServiceList.getChildren();
            	while (iCarrierServiceList.hasNext())
            	{
            		YFCElement eleCarrierServiceElement = (YFCElement)iCarrierServiceList.next();
            		String carrierServiceCode = eleCarrierServiceElement.getAttribute("CarrierServiceCode");
            		eleCarrierServiceElement.setAttribute("Price", "0.00");
            		if ("FX_SHIP_GROUND".equals(sItemID))
            		{
            			if (carrierServiceCode.contains("STANDARD"))
            			{
            				eleCarrierServiceList.removeChild(eleCarrierServiceElement);
            				iCarrierServiceList = eleCarrierServiceList.getChildren();
            			}
            			else
            				eleCarrierServiceElement.setAttribute("CarrierServiceDesc", "FedEx Ground");
            		}
            		if ("FX_SHIP_2NDDAY".equals(sItemID))
            		{
            			if (!carrierServiceCode.contains("EXPRESS"))
            			{
            				eleCarrierServiceList.removeChild(eleCarrierServiceElement);
            				iCarrierServiceList = eleCarrierServiceList.getChildren();
            			}	
            			else
            				eleCarrierServiceElement.setAttribute("CarrierServiceDesc", "FedEx 2nd Day");
            		}
            		if ("FX_SHIP_OVERNIGHT".equals(sItemID))
            		{
            			if (!carrierServiceCode.contains("PREMIUM"))
            			{
            				eleCarrierServiceList.removeChild(eleCarrierServiceElement);
            				iCarrierServiceList = eleCarrierServiceList.getChildren();
            			}
            			else
            				eleCarrierServiceElement.setAttribute("CarrierServiceDesc", "FedEx Overnight");
            		}
            	}
        	 
            }
            */
        }
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SEGetCarrierServiceOptionsForOrderingUEImpl Output:");
			System.out.println (doc.getString());
		}
        return doc.getDocument();
    }
    
    private String	getBillToID (YFSEnvironment env, YFCElement eleOrder) throws YFSUserExitException
    {
    	YFCDocument	docOrderDetailsTemplate = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"\" BillToID=\"\" DocumentType=\"\" />");
    	YFCDocument	docOrderDetailsInput = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"" + eleOrder.getAttribute ("OrderHeaderKey")+"\" />");
    	String		sBillToID = null;
    	try {
    		YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
    		if (YFSUtil.getDebug()) {
    			System.out.println ("Input to getOrderDetails() API:");
    			System.out.println (docOrderDetailsInput.getString());
    		}
    		env.setApiTemplate("getOrderDetails", docOrderDetailsTemplate.getDocument());
    		YFCDocument docOrderDetailsOutput = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrderDetailsInput.getDocument()));
    		if (YFSUtil.getDebug()) {
    			System.out.println ("Output from getOrderDetails() API:");
    			System.out.println (docOrderDetailsOutput.getString());
    		}
    		sBillToID = docOrderDetailsOutput.getDocumentElement().getAttribute("BillToID");
    	} catch (Exception e) {
			throw (new YFSUserExitException (e.getMessage()));    		
    	} finally {
    		env.clearApiTemplate("getOrderDetails");
    	}
    	return sBillToID;
    }
}

