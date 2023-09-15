package com.custom.diab.demos.ue;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.ypm.japi.ue.YPMCalculateShippingChargeUE;
import com.custom.yantra.util.*;

public class SECalculateShippingChargeUEImpl implements YPMCalculateShippingChargeUE 
{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Document calculateShippingCharge(YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		// TODO Auto-generated method stub
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering SECalculateShippingChargesUEImpl - Input XML:");
			System.out.println (docOrder.getString());
		}
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleShipping = eleOrder.getChildElement("Shipping");
		BigDecimal	bdTotalShippingCharges = new BigDecimal (0);
		Hashtable	htCarrierServicePrices;
		try {
			// Only do this calculation for B2C Sales Orders
			String sDocumentType = eleOrder.getAttribute("DocumentType");
			if (!YFCObject.isNull(sDocumentType) && !"0001".equals(sDocumentType) || !IsD2C(env, eleOrder))
				return docIn;
			
			htCarrierServicePrices = getCarrierServicePrices (env, eleOrder.getAttribute ("EnterpriseCode"), eleOrder.getAttribute("Currency"));
			
			// default values
			eleShipping.setAttribute ("ShippingCharge", "0.00");
			eleShipping.setAttribute ("MinimizeNumberOfShipments", "N");
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
		// check to see if all lines shipping via the same carrier service
		if (!YFCCommon.isVoid(eleOrder.getAttribute("OrderReference")))
		{
			Hashtable	htCarrierServiceCodesUsed = new Hashtable();
			bdTotalShippingCharges = new BigDecimal (0.00);			// Default Shipping Charges - Free Shipping
			
			// iterate over all the lines to see how many shipping methods are used
			YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
			Iterator	iOrderLines = eleOrderLines.getChildren();
			
			YFCDocument	docOrderLineDetailsInput = YFCDocument.getDocumentFor ("<OrderLineDetail OrderHeaderKey=\""+eleOrder.getAttribute("OrderReference")+"\"/>");
			YFCElement	eleOrderLineDetailsInput = docOrderLineDetailsInput.getDocumentElement();
			YFCDocument	docOrderLineDetailsTemplate = YFCDocument.getDocumentFor ("<OrderLine CarrierServiceCode=\"\" ItemGroupCode=\"\"><ItemDetails ItemID=\"\"/></OrderLine>");
			env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsTemplate.getDocument());
			
			// carrier service code must be obtained via an API call.  Not sure why this is not passed into the UE
			try {
				YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
				
				while (iOrderLines.hasNext())
				{
					YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();

					// if this a FREEGIFT line skip it for now - being auto-added by Free Gift Pricing Rule
					if (eleOrderLine.getAttribute ("LineID").startsWith("YPM"))
						continue;
					// Note this UE gets passed services lines as order lines so we want to skip service lines.
					// adding new UOM's for Services may require adding those new UOM's here.
					String sUOM = eleOrderLine.getAttribute("UnitOfMeasure");
					
					if (!YFCObject.isVoid(sUOM)
					&& (sUOM.equals("JOB") || sUOM.equals("UNIT") || sUOM.equals("HR")))
						continue;
					
					eleOrderLineDetailsInput.setAttribute("OrderLineKey", eleOrderLine.getAttribute ("LineID"));
					if (YFSUtil.getDebug())
					{
						System.out.println ("Input to getOrderLineDetails:");
						System.out.println (docOrderLineDetailsInput.getString());
					}
					YFCDocument	docOrderLineDetails = YFCDocument.getDocumentFor(api.getOrderLineDetails(env,  docOrderLineDetailsInput.getDocument()));
					if (YFSUtil.getDebug())
					{
						System.out.println ("Output from getOrderLineDetails:");
						System.out.println (docOrderLineDetails.getString());
					}
					
					YFCElement	eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
					YFCElement	eleItemDetails = eleOrderLineDetails.getChildElement("ItemDetails");
					

					// consider only the product lines for shipping charges
					if (!YFCObject.isNull(eleOrderLine.getAttribute("ItemGroupCode"))  && !"PROD".equals(eleOrderLine.getAttribute("ItemGroupCode")))
						continue;
					
					// FedEx Shipping items are priced via the line pricing not via line charges
		            if (YFCObject.isNull(eleItemDetails) || eleOrderLineDetails.getChildElement("ItemDetails").getAttribute ("ItemID").startsWith("FX_SHIP"))
		            	continue;

					// if carrier service code assigned to line with non-zero quantity
					BigDecimal	bdQty = new BigDecimal (eleOrderLine.getAttribute("Quantity"));
					if (bdQty.intValue() > 0 && !YFCCommon.isVoid(eleOrderLineDetails.getAttribute("CarrierServiceCode")))
						htCarrierServiceCodesUsed.put(eleOrderLineDetails.getAttribute ("CarrierServiceCode"), htCarrierServicePrices.get(eleOrderLineDetails.getAttribute ("CarrierServiceCode")));
				}
			} catch (Exception e) {
				throw new YFSUserExitException (e.getMessage());
			} finally {
				env.clearApiTemplate("getOrderLineDetails");
			}
			// sum up the cost of all the distinct shipping services used on the order
			Enumeration enumCarrierServiceCodesUsed = htCarrierServiceCodesUsed.elements();
			while (enumCarrierServiceCodesUsed.hasMoreElements())
				bdTotalShippingCharges = bdTotalShippingCharges.add(new BigDecimal ((String)enumCarrierServiceCodesUsed.nextElement()));
		}
//		else if (!YFCCommon.isVoid(eleShipping.getAttribute ("CarrierServiceCode")) && !YFCCommon.isVoid(htCarrierServicePrices.get(eleShipping.getAttribute ("CarrierServiceCode"))))
			// if a single carrier service selected
//			bdTotalShippingCharges = new BigDecimal ((String)htCarrierServicePrices.get(eleShipping.getAttribute ("CarrierServiceCode")));
		
		eleShipping.setAttribute ("ShippingCharge", bdTotalShippingCharges.toPlainString());
		eleShipping.setAttribute ("MinimizeNumberOfShipments", "N");
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SECalculateShippingChargesUEImpl - Output XML:");
			System.out.println (docOrder.getString());
		}
		return docIn;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Hashtable getCarrierServicePrices (YFSEnvironment env, String sEnterpriseCode, String sCurrency) throws YFSUserExitException
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		Hashtable	htCarrierServiceCosts = new Hashtable();
		
		eleCommonCode.setAttribute ("CodeType", "DEMO_SHIPPINGCHARGES");
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception e) {
			throw (new YFSUserExitException (e.getMessage()));
		}
		// if the DEMO_SHIPPINGCHARGES common code table was found
		// Note Entries must be in the following format
		// CodeValue: CarrierServiceCode (e.g. STANDARD_MTRXR or STANDARD_MTRXR_[CURRENCY])
		// CodeShortDescription: Cost in Given Currency
		// CodeLongDescription: Currency
		if (eleCommonCodes != null)
		{
			Iterator	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{	
				eleCommonCode = (YFCElement)iCommonCodes.next();
				if (YFCCommon.equals(sCurrency, eleCommonCode.getAttribute ("CodeLongDescription")))
				{
					String sCarrierService = eleCommonCode.getAttribute ("CodeValue");
					int		iCurrency = sCarrierService.indexOf(sCurrency);
					if (iCurrency >= 0)
						sCarrierService = sCarrierService.substring(0, iCurrency - 1);
					if (YFSUtil.getDebug())
							System.out.println ("Carrier Service Found for Currency: "+ sCurrency+ " Carrier Service: " + sCarrierService);
					htCarrierServiceCosts.put(sCarrierService, eleCommonCode.getAttribute("CodeShortDescription"));
				}
			}
		}
		return htCarrierServiceCosts;
	}

	private boolean IsD2C (YFSEnvironment env, YFCElement eleOrder) 
	{
		boolean	bRet = IsSFO (env, eleOrder);
		
		String	sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
		String	sCustomerID = eleOrder.getAttribute("CustomerID");
		if (!YFCObject.isVoid(sCustomerID))
		{
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
			try {
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				
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
			} catch (Exception ignore) {

			} finally {
				env.clearApiTemplate("getCustomerDetails");
			}
		}
		return bRet;
	}
	
	private boolean IsSFO (YFSEnvironment env, YFCElement eleOrder)
	{
		boolean bRet = false;
		String	sOrderHeaderKey = eleOrder.getAttribute("OrderReference");
		if (YFCObject.isVoid(sOrderHeaderKey))
			return false;
		
		try
        {
            YIFApi api = YIFClientFactory.getInstance().getLocalApi();
            YFCDocument docOrderDetails = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"" + sOrderHeaderKey + "\"/>");
            YFCDocument docOrderDetailsTemplate = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"\" OrderNo=\"\" DocumentType=\"\" ><Extn ExtnOptimizerFlag=\"\"/></Order>");
            env.setApiTemplate("getOrderDetails", docOrderDetailsTemplate.getDocument());
            if (YFSUtil.getDebug())
            {
            	System.out.println ("Input to getOrderDetails");
            	System.out.println (docOrderDetails.getString());
            }
            docOrderDetails = YFCDocument.getDocumentFor(api.getOrderDetails(env, docOrderDetails.getDocument()));
            if (YFSUtil.getDebug())
            {
            	System.out.println ("Output from getOrderDetails");
            	System.out.println (docOrderDetails.getString());
            }
            YFCElement eleOrderDetails = docOrderDetails.getDocumentElement();
    		YFCElement	eleExtn = eleOrderDetails.getChildElement("Extn");
    		if (!YFCObject.isNull(eleExtn))
    		{
        		String		sExtnOptimizerFlag = eleExtn.getAttribute("ExtnOptimizerFlag"); 
        		String		sDocumentType = eleOrderDetails.getAttribute("DocumentType");
        		bRet = !YFCObject.isVoid(sExtnOptimizerFlag) && sExtnOptimizerFlag.equals("Y") && sDocumentType.equals("0001");
    		}
        }
        catch(Exception e)
        {
        	env.clearApiTemplate("getOrderDetails");
        } finally {
        	env.clearApiTemplate("getOrderDetails");
        }
		
		return bRet;
	}
}
