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
		
		// Only do this calculation for Sales Orders
		String sDocumentType = eleOrder.getAttribute("DocumentType");
		if (!YFCObject.isNull(sDocumentType) && !"0001".equals(sDocumentType))
			return docIn;
		
		YFCElement	eleShipping = eleOrder.getChildElement("Shipping");
		Hashtable	htCarrierServicePrices = getCarrierServicePrices (env, eleOrder.getAttribute ("EnterpriseCode"), eleOrder.getAttribute("Currency"));
		BigDecimal	bdTotalShippingCharges = new BigDecimal (0);
		
		// default values
		eleShipping.setAttribute ("ShippingCharge", "0.00");
		eleShipping.setAttribute ("MinimizeNumberOfShipments", "N");
		
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
					if (eleOrderLine.getAttribute("UnitOfMeasure").equals("JOB") 
					|| eleOrderLine.getAttribute("UnitOfMeasure").equals("UNIT") 
					|| eleOrderLine.getAttribute("UnitOfMeasure").equals("HR"))
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
}
