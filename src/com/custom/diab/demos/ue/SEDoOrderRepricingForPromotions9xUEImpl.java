/**
  * SEDoOrderRepricingForPromotions9xUEImpl.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.ue;

import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSOrderRepricingUE;
import com.yantra.yfc.util.YFCCommon;
import java.util.*;
import java.math.*;
import org.w3c.dom.*;
import com.custom.yantra.util.YFSUtil;


public class SEDoOrderRepricingForPromotions9xUEImpl implements YFSOrderRepricingUE
{
	protected	final String	ID_FREESHIP = "FREESHIP";
	protected	final String	ID_100ORMORE = "100ORMORE";
	protected	final String	ID_10PERCENTOFF = "10PERCENTOFF";

    public SEDoOrderRepricingForPromotions9xUEImpl()
    {
    }

    public Document orderReprice(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
		// process promotions on the order
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SEDoOrderRepricingForPromotions9xUEImpl:");
			System.out.println ("Order:");
			System.out.println (docOrder.getString());
		}
	
		// *********************************************************************************************
		// FIRST CALCULATE ALL PRICING ON THE ORDER FOR EACH LINE
		// *********************************************************************************************
		// This logic is not required as part of this UE since we're using standard price lists stored 
		// in Yantra.  If you were to get pricing from some other system you would need to add the logic
		// here to add the line price information, and to calculate the overall line extended price and 
		// overall order sub-total
		// 
		
		
		// *********************************************************************************************
		// NEXT CALCULATE ALL CHARGES EXCLUDING ANY PROMOTIONS		
		// *********************************************************************************************
		addShippingCharges (env, eleOrder);
		
		// *********************************************************************************************
		// NOW APPLY ANY PROMOTIONS.  This is not required anymore
		// *********************************************************************************************
		/*
		addPromotions (env, eleOrder);		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Order after Promotions Applied:");
			System.out.println (docOrder.getString());
		}
		 */
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting SEDoOrderRepricingForPromotions9xUEImpl:");
			System.out.println ("Repriced Order:");
			System.out.println (docOrder.getString());
		}
		
		return docOrder.getDocument();
	}

	protected YFCElement addShippingCharges (YFSEnvironment env, YFCElement eleOrder)
	{
		// first calculate the shipping charges for the order based on order value
		YFCElement	eleHeaderCharge = getShippingCharges(eleOrder);
		String sOriginalCharge;
		if (eleHeaderCharge != null)
		{
			sOriginalCharge = eleHeaderCharge.getAttribute ("ChargeAmount");
			if (sOriginalCharge == null)
				sOriginalCharge = "0";
		}
		else
		{
			YFCElement	eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
			if (eleHeaderCharges == null)
				eleHeaderCharges = eleOrder.createChild ("HeaderCharges");
			eleHeaderCharge = eleHeaderCharges.createChild ("HeaderCharge");
			sOriginalCharge = "0";
		}
		eleHeaderCharge.setAttribute ("ChargeCategory", "Shipping");
		eleHeaderCharge.setAttribute ("ChargeName","Shipping");
		eleHeaderCharge.setAttribute ("OriginalCharge", sOriginalCharge);
		eleHeaderCharge.setAttribute ("ChargeAmount", calcShippingCharges (env, eleOrder));
		return eleHeaderCharge;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement	getShippingCharges (YFCElement eleOrder)
	{
		YFCElement	eleHeaderCharge = null;

		// locate the shipping charges element				
		YFCElement	eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
		if (eleHeaderCharges != null)
		{
			Iterator	iHeaderCharges = eleHeaderCharges.getChildren ();
			while (iHeaderCharges.hasNext())
			{
				YFCElement	eleTestHeaderCharge = (YFCElement)iHeaderCharges.next();
				if ("Shipping".equalsIgnoreCase (eleTestHeaderCharge.getAttribute ("ChargeCategory")))
				{
					eleHeaderCharge = eleTestHeaderCharge;
				}
			}
		}	
		return eleHeaderCharge;
	}

	@SuppressWarnings("rawtypes")
	protected	String	calcShippingCharges (YFSEnvironment env, YFCElement eleOrder)
	{
	  BigDecimal bdShippingCharge = new BigDecimal (calcMinimumShippingCharges(eleOrder)).setScale(2);
	  try {
		YFCElement	eleOverallTotals = eleOrder.getChildElement ("OverallTotals");

	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		eleCommonCode.setAttribute ("CodeType", "DEMO_SHIPPINGCHARGES");
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Input to getCommonCodeList() API:");
			System.out.println (docCommonCode.getString());
		}

		YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
		YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
		if (YFSUtil.getDebug()) {
			System.out.println ("Output from getCommonCodeList() API:");
			System.out.println (docOut.getString());
		}

		YFCElement	eleCommonCodes = docOut.getDocumentElement ();

		// make sure common code table configured
		if (eleCommonCodes != null && eleCommonCodes.getChildElement("CommonCode") != null)
		{		
			Iterator	iCommonCodes = eleCommonCodes.getChildren();
				
			BigDecimal bdLineSubTotal = new BigDecimal (eleOverallTotals.getAttribute ("LineSubTotal")).setScale(2);
			BigDecimal bdLastBreak = new BigDecimal ("0.00").setScale (2);
			while (iCommonCodes.hasNext())
			{	
				eleCommonCode = (YFCElement)iCommonCodes.next();
				BigDecimal bdBreak = new BigDecimal (eleCommonCode.getAttribute ("CodeValue")).setScale (2);

				if (bdLineSubTotal.compareTo (bdBreak) >= 0)
				{
					BigDecimal bdNewShippingCharge = new BigDecimal (eleCommonCode.getAttribute ("CodeShortDescription"));
					if (bdBreak.compareTo(bdLastBreak) >= 0)
					{
						bdShippingCharge = bdNewShippingCharge.setScale (2);
						bdLastBreak = bdBreak;
					}
				}
			}
		
			// if shipping charge is < 1.00 it's a percentage of the overall line total
			if (bdShippingCharge.compareTo (new BigDecimal ("1.00")) < 0)
				bdShippingCharge = bdLineSubTotal.multiply (bdShippingCharge).setScale (2, BigDecimal.ROUND_DOWN);
		}	
		else
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("DEMO_SHIPPINGCHARGES Common Code Table Not Configured");
			}
		}
	  } catch (Exception e) {
	  	if (YFSUtil.getDebug()) {
			System.out.println ("Exception in calcShippingCharges-Type="+e.getClass().getName()+" Message="+e.getMessage());
		}
	  }
	  return bdShippingCharge.toString();
	}

	@SuppressWarnings("rawtypes")
	protected String calcMinimumShippingCharges (YFCElement eleOrder)
	{
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
	  	BigDecimal	bdShippingCharge = new BigDecimal ("0.00").setScale(2);
		
		// if orderlines exist
		if (eleOrderLines != null)
		{
			Iterator	iOrderLines = eleOrderLines.getChildren();
			int			iHighestMinimum = 0;
			// iterate over order lines
			while (iOrderLines.hasNext ())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next ();
				String	sDeliveryMethod = eleOrderLine.getAttribute ("DeliveryMethod");
				
				// if ordline is not for Pickup (i.e. shipping appliles)
				if (sDeliveryMethod == null || sDeliveryMethod.equals("SHP"))
				{
					// get the parcel method of shipment for the line if specified
					String	sShippingMethod = eleOrderLine.getAttribute ("CarrierServiceCode");
					
					// if no parcel method for line given
					if (YFCCommon.isVoid (sShippingMethod))
						// get the parcel method of shipment for the order if specified
						sShippingMethod = eleOrder.getAttribute ("CarrierServiceCode");

					// default to standard shipping method if none yet selected						
					if (YFCCommon.isVoid (sShippingMethod))
						sShippingMethod = "MTRX_STANDARD";
					else
						sShippingMethod = sShippingMethod.toUpperCase ();

					// apply standard shipping charge as minimum						
					if ((sShippingMethod.contains ("STANDARD") || sShippingMethod.contains("GR")) && iHighestMinimum < 1)
					{
						bdShippingCharge = new BigDecimal ("7.50").setScale(2);
						iHighestMinimum = 1;
					}
					else if ((sShippingMethod.contains ("EXPRESS") || sShippingMethod.contains ("2"))  && iHighestMinimum < 2)
					{
						bdShippingCharge = new BigDecimal ("12.00").setScale(2);
						iHighestMinimum = 2;	
					}
					else if ((sShippingMethod.contains ("PREMIUM") || sShippingMethod.contains ("XT")) && iHighestMinimum < 3)
					{
						bdShippingCharge = new BigDecimal ("24.00").setScale(2);
						iHighestMinimum = 3;
					}			
				}
						
			}
		}
	  	return bdShippingCharge.toString();
	}
}