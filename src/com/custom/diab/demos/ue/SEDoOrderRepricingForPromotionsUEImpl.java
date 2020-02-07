/**
  * SEDoOrderRepricingForPromotionsUEImpl.java
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


public class SEDoOrderRepricingForPromotionsUEImpl implements YFSOrderRepricingUE
{
	protected	final String	ID_FREESHIP = "FREESHIP";
	protected	final String	ID_100ORMORE = "100ORMORE";
	protected	final String	ID_10PERCENTOFF = "10PERCENTOFF";

    public SEDoOrderRepricingForPromotionsUEImpl()
    {
    }

    public Document orderReprice(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
		// process promotions on the order
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SEDoOrderRepricingForPromotionsUEImpl:");
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
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Order after Shipping Charges Applied:");
			System.out.println (docOrder.getString());
		}
		
		// *********************************************************************************************
		// NOW APPLY ANY PROMOTIONS
		// *********************************************************************************************
		addPromotions (env, eleOrder);		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Order after Promotions Applied:");
			System.out.println (docOrder.getString());
		}

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting SEDoOrderRepricingForPromotionsUEImpl:");
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
		eleHeaderCharge.setAttribute ("OriginalCharge", sOriginalCharge);
		eleHeaderCharge.setAttribute ("ChargeAmount", calcShippingCharges (env, eleOrder));
		return eleHeaderCharge;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement addPromotions (YFSEnvironment env, YFCElement eleOrder)
	{
		YFCElement	elePromotions = eleOrder.getChildElement ("Promotions");
		// see if any promotions are to be added to the order
		if (elePromotions == null)
			return eleOrder;

		Iterator	iPromotions = elePromotions.getChildren();
		while (iPromotions.hasNext())
		{
			YFCElement	elePromotion = (YFCElement)iPromotions.next();
			
			if (ID_FREESHIP.equalsIgnoreCase (elePromotion.getAttribute ("PromotionId")))
			{
				addFreeShipPromotion (env, eleOrder, elePromotion, getShippingDiscount(eleOrder));
			}
			else if (ID_100ORMORE.equalsIgnoreCase (elePromotion.getAttribute ("PromotionId")))
			{
				add100OrMorePromotion (env, eleOrder, elePromotion, get100OrMoreDiscount(eleOrder));
			}
			else if (ID_10PERCENTOFF.equalsIgnoreCase (elePromotion.getAttribute("PromotionId")))
			{
				add10PercentOffLinePromotion (env, eleOrder, elePromotion);
			}
			else
			{
				addNotApplied (eleOrder, elePromotion);
			}
		}
		return eleOrder;
	}
	
	@SuppressWarnings("rawtypes")
	protected void		add10PercentOffLinePromotion (YFSEnvironment env, YFCElement eleOrder, YFCElement elePromotion)
	{
		boolean	bAdd, bRemove, bExistingPromotion;	

		System.out.println (elePromotion.getAttribute ("PromotionId") + " Promotion being added, removed or updated on Order:");
		System.out.println (elePromotion.getString());

		// NOTE:
		// The following code can be used to add ANY line level promotion.
		bAdd = bRemove = false;
		bExistingPromotion = isExistingPromotion(eleOrder, elePromotion, elePromotion.getAttribute ("PromotionId"));
	
		// see if we're to remove this promotion
		if (isRemovingPromotion (eleOrder, elePromotion.getAttribute ("PromotionId")))
			bRemove = bExistingPromotion;
		else
			bAdd = !bExistingPromotion;			

		// if adding, removing or updating the promotion
		if (bAdd || bRemove || bExistingPromotion)
		{
			if (YFSUtil.getDebug())
			{
				if (bAdd)
				{
					System.out.println ("Adding the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				// else if removing the promotion
				else if (bRemove)
				{
					System.out.println ("Removing the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				else if (bExistingPromotion)
				{	
					System.out.println ("Updating the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
			}
			YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
			if (eleOrderLines != null)
			{
				Iterator	iOrderLines = eleOrderLines.getChildren();
				while (iOrderLines.hasNext())
				{
					YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
					YFCElement	eleLineDiscount = get10PercentLineDiscount (eleOrderLine);
					String	sOriginalDiscount, sOriginalDiscountPerUnit;
					
					if (eleLineDiscount == null)
					{
						YFCElement eleLineCharges = eleOrderLine.getChildElement ("LineCharges");
						if (eleLineCharges == null)
							eleLineCharges = eleOrderLine.createChild ("LineCharges");
						eleLineDiscount = eleLineCharges.createChild ("LineCharge");
						sOriginalDiscount = "0.00";
						sOriginalDiscountPerUnit = "0.00";
					}
					else
					{
						sOriginalDiscount = eleLineDiscount.getAttribute ("ChargeAmount");
						sOriginalDiscountPerUnit = eleLineDiscount.getAttribute ("ChargePerUnit");
						if (YFCCommon.isVoid (sOriginalDiscountPerUnit) || new BigDecimal(sOriginalDiscountPerUnit).compareTo (BigDecimal.ZERO) == 0)
							sOriginalDiscountPerUnit = sOriginalDiscount;
					}
					// set original charges in line discount 
					eleLineDiscount.setAttribute ("ChargeCategory", "PROMOTION");
					eleLineDiscount.setAttribute ("ChargeName", "10 Percent Off");
					eleLineDiscount.setAttribute ("OriginalCharge", sOriginalDiscount);
					eleLineDiscount.setAttribute ("OriginalChargePerUnit", sOriginalDiscountPerUnit);

					// if removing the promotion
					if (bRemove)
					{
						eleLineDiscount.setAttribute ("ChargePerUnit", "0.00");
						eleLineDiscount.setAttribute ("ChargePerLine", "0.00");
					}
					else
					{
						// adding or updating the promotion
						// adjust amount to reflect the 10% off each item
						YFCElement	eleAward = addAwardsFor10PercentOff (env, eleOrderLine, elePromotion);
						YFCElement	eleLinePriceInfo = eleOrderLine.getChildElement ("LinePriceInfo");
						BigDecimal	bdUnitPrice = new BigDecimal (eleLinePriceInfo.getAttribute ("UnitPrice"));
						BigDecimal	bdDiscountPerUnit = bdUnitPrice.multiply (new BigDecimal (".10"));
						BigDecimal	bdRepricingQty = new BigDecimal (eleLinePriceInfo.getAttribute ("RepricingQty"));
						// eleLineDiscount.setAttribute ("ChargePerLine", eleAward.getAttribute ("AwardAmount"));
						
						// if single unit, charge per line should equal charge per unit.  if more than one unit then rounding
						// may cause charge per unit and charge per line not to equate which is acceptable when we have more than one unit
						if (bdRepricingQty.compareTo (new BigDecimal ("1")) == 0)	
							eleLineDiscount.setAttribute ("ChargePerUnit", eleAward.getAttribute ("AwardAmount"));
						else
							eleLineDiscount.setAttribute ("ChargePerUnit", bdDiscountPerUnit.setScale(2, BigDecimal.ROUND_DOWN).toString());			
					}
				}
			}
		}
	}

	protected void		addFreeShipPromotion (YFSEnvironment env, YFCElement eleOrder, YFCElement elePromotion, YFCElement eleHeaderDiscount)
	{
		boolean	bAdd, bRemove, bExistingPromotion;	

		System.out.println (elePromotion.getAttribute ("PromotionId") + " Promotion being added, removed or updated on Order:");
		System.out.println (elePromotion.getString());

		// NOTE:
		// The following code can be used to add ANY header level promotion.
		bAdd = bRemove = false;
		bExistingPromotion = isExistingPromotion(eleOrder, elePromotion, elePromotion.getAttribute ("PromotionId"));
				
		// see if we're to remove this promotion
		if (isRemovingPromotion (eleOrder, elePromotion.getAttribute ("PromotionId")))
			bRemove = bExistingPromotion;
		else
			bAdd = !bExistingPromotion;			

		// if adding, removing or updating the promotion
		if (bAdd || bRemove || bExistingPromotion)
		{
			if (YFSUtil.getDebug())
			{
				if (bAdd)
				{
					System.out.println ("Adding the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				// else if removing the promotion
				else if (bRemove)
				{
					System.out.println ("Removing the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				else if (bExistingPromotion)
				{	
					System.out.println ("Updating the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
			}
			// get any original discount that was applied by this promotion if any
			String		sOriginalCharge;
			if (eleHeaderDiscount == null)
			{
				YFCElement eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
				if (eleHeaderCharges == null)
					eleHeaderCharges = eleOrder.createChild ("HeaderCharges");
				eleHeaderDiscount = eleHeaderCharges.createChild ("HeaderCharge");
				sOriginalCharge = "0.00";
			}
			else
			{
				sOriginalCharge = eleHeaderDiscount.getAttribute ("ChargeAmount");
			}
			// set original charges in header discount 
			eleHeaderDiscount.setAttribute ("ChargeCategory", "PROMOTION");
			eleHeaderDiscount.setAttribute ("ChargeName", "Free Shipping");
			eleHeaderDiscount.setAttribute ("OriginalCharge", sOriginalCharge);

			// if removing the promotion
			if (bRemove)
			{
				eleHeaderDiscount.setAttribute ("ChargeAmount", "0.00");
			}
			else
			{
				// adding or updating the promotion
				// adjust amount to reflect the actual shipping charges
				YFCElement	eleAward = addAwardsForFreeShip (env, eleOrder, elePromotion);
				eleHeaderDiscount.setAttribute ("ChargeAmount", eleAward.getAttribute ("AwardAmount"));
			}	
		}
	}


	protected void		add100OrMorePromotion (YFSEnvironment env, YFCElement eleOrder, YFCElement elePromotion, YFCElement eleHeaderDiscount)
	{
		boolean	bAdd, bRemove, bExistingPromotion;	

		System.out.println (elePromotion.getAttribute ("PromotionId") + " Promotion being added, removed or updated on Order:");
		System.out.println (elePromotion.getString());

		// NOTE:
		// The following code can be used to add ANY header level promotion.
		bAdd = bRemove = false;
		bExistingPromotion = isExistingPromotion(eleOrder, elePromotion, elePromotion.getAttribute ("PromotionId"));
				
		// see if we're to remove this promotion
		if (isRemovingPromotion (eleOrder, elePromotion.getAttribute ("PromotionId")))
			bRemove = bExistingPromotion;
		else
			bAdd = !bExistingPromotion;			

		// if adding, removing or updating the promotion
		if (bAdd || bRemove || bExistingPromotion)
		{
			if (YFSUtil.getDebug())
			{
				if (bAdd)
				{
					System.out.println ("Adding the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				// else if removing the promotion
				else if (bRemove)
				{
					System.out.println ("Removing the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
				else if (bExistingPromotion)
				{	
					System.out.println ("Updating the " + elePromotion.getAttribute ("PromotionId") + " Promotion");
				}
			}
			// get any original discount that was applied by this promotion if any
			String		sOriginalCharge;
			if (eleHeaderDiscount == null)
			{
				YFCElement eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
				if (eleHeaderCharges == null)
					eleHeaderCharges = eleOrder.createChild ("HeaderCharges");
				eleHeaderDiscount = eleHeaderCharges.createChild ("HeaderCharge");
				sOriginalCharge = "0.00";
			}
			else
			{
				sOriginalCharge = eleHeaderDiscount.getAttribute ("ChargeAmount");
			}
			// set original charges in header discount 
			eleHeaderDiscount.setAttribute ("ChargeCategory", "PROMOTION");
			eleHeaderDiscount.setAttribute ("ChargeName", "100 Or More");
			eleHeaderDiscount.setAttribute ("OriginalCharge", sOriginalCharge);

			// if removing the promotion
			if (bRemove)
			{
				eleHeaderDiscount.setAttribute ("ChargeAmount", "0.00");
			}
			else
			{
				// adding or updating the promotion
				// adjust amount to reflect the actual shipping charges
				YFCElement	eleAward = addAwardsFor100OrMore (env, eleOrder, elePromotion);
				eleHeaderDiscount.setAttribute ("ChargeAmount", eleAward.getAttribute ("AwardAmount"));
			}	
		}
	}


    protected YFCElement addAwardsFor10PercentOff (YFSEnvironment env, YFCElement eleOrderLine, YFCElement elePromotion)
    {
		YFCElement	eleAwards = elePromotion.getChildElement("Awards");
		YFCElement	eleAward  = null;

		// first see if the award alredy exists
		if (eleAwards != null)
		{
			eleAward = eleAwards.getChildElement ("Award");
			if (eleAward != null)
				eleAward = getLinePromotionAward (eleOrderLine, eleAward.getAttribute ("AwardId"));
		}
		
		// if award doesn't exist create it		
		if (eleAward == null)
		{
			eleAwards = eleOrderLine.getChildElement ("Awards");
			if (eleAwards == null)
		        eleAwards = eleOrderLine.createChild("Awards");
    	    eleAward = eleAwards.createChild("Award");
            eleAward.setAttribute("AwardId", new String("" + System.currentTimeMillis()));
		}
        eleAward.setAttribute("PromotionId", elePromotion.getAttribute("PromotionId"));
        eleAward.setAttribute("Description", elePromotion.getAttribute("PromotionId") + " Promotion Applied To Order");

		// get overall line total
        YFCElement eleLineOverallTotals = eleOrderLine.getChildElement("LineOverallTotals");
    	BigDecimal bdLineTotal = new BigDecimal ("0.00");
    	BigDecimal bdLineDiscount = new BigDecimal ("0.00");
		if (eleLineOverallTotals != null)
		{
	        String sLineTotal = eleLineOverallTotals.getAttribute("ExtendedPrice");
        	if(sLineTotal != null && sLineTotal.trim().length() != 0)
			{
            	bdLineTotal = new BigDecimal (sLineTotal);
				bdLineDiscount = bdLineTotal.multiply (new BigDecimal (".10"));
			}
		}
		else
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("WARNING - tempate\\userexit\\orderRepricing.xml Template is Incomplete");
				System.out.println ("Missing the <LineOverallTotals> Element so We Can't determine 10% of Line Total");
				System.out.println ("Award Can't be Applied to the Order - please correct orderRepricing.xml user exit template");
			}
		}
		BigDecimal	bdUnits = new BigDecimal(eleOrderLine.getAttribute ("OrderedQty"));
		// if the line has at least one unit on it
		if (bdUnits.compareTo (BigDecimal.ZERO) > 0)
		{
	        eleAward.setAttribute("AwardApplied", "Y");
    	    eleAward.setAttribute("AwardAmount", bdLineDiscount.setScale (2, BigDecimal.ROUND_DOWN).toString());
	        eleAward.setAttribute("AwardType", "Order Line Discount");
    	    elePromotion.setAttribute("PromotionApplied", "Y");
        	elePromotion.setAttribute("PromotionType", "10 Percent Off");
	        elePromotion.setAttribute("Description", "10 Percent Off Each Item");
		}
		else
		{
            eleAward.setAttribute("AwardApplied", "N");
			eleAward.setAttribute("AwardAmount", "0");
            eleAward.setAttribute("DenialReason", "Order Line Cancelled or Returned");
            elePromotion.setAttribute("PromotionApplied", "Y");
            elePromotion.setAttribute("Description", "10 Percent Off");
            elePromotion.setAttribute("DenialReason", "10 Percent Off Each Item");
		}
        return eleAward;
    }

	
    protected YFCElement addAwardsForFreeShip(YFSEnvironment env, YFCElement eleOrder, YFCElement elePromotion)
    {
		YFCElement	eleAwards = elePromotion.getChildElement("Awards");
		YFCElement	eleAward  = null;

		// first see if the award alredy exists
		if (eleAwards != null)
		{
			eleAward = eleAwards.getChildElement ("Award");
			if (eleAward != null)
				eleAward = getPromotionAward (eleOrder, eleAward.getAttribute ("AwardId"));
		}
		// if award doesn't exist create it		
		if (eleAward == null)
		{
			eleAwards = eleOrder.getChildElement ("Awards");
			if (eleAwards == null)
		        eleAwards = eleOrder.createChild("Awards");
    	    eleAward = eleAwards.createChild("Award");
            eleAward.setAttribute("AwardId", new String("" + System.currentTimeMillis()));
		}
        eleAward.setAttribute("PromotionId", elePromotion.getAttribute("PromotionId"));
        eleAward.setAttribute("Description", elePromotion.getAttribute("PromotionId") + " Promotion Applied To Order");

		// get line total on order.  Must be > $29.99 for promotion to be valid
        YFCElement eleOverallTotals = eleOrder.getChildElement("OverallTotals");
    	BigDecimal bdTotal = new BigDecimal ("0.00");
		if (eleOverallTotals != null)
		{
	        String sOrderTotal = eleOverallTotals.getAttribute("LineSubTotal");
        	if(sOrderTotal != null && sOrderTotal.trim().length() != 0)
            	bdTotal = new BigDecimal (sOrderTotal);
		}
		else
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("WARNING - tempate\\userexit\\orderRepricing.xml Template is Incomplete");
				System.out.println ("Missing the <OverallTotals> Element so We Can't determine if Order Reaches Promotion Requirement of $29.99");
				System.out.println ("Award Can't be Applied to the Order - please correct orderRepricing.xml user exit template");
			}
		}
		// get the line total for the order
        if(bdTotal.compareTo (new BigDecimal ("29.99")) > 0)
        {
            eleAward.setAttribute("AwardApplied", "Y");
            eleAward.setAttribute("AwardAmount", getShippingCharges(eleOrder).getAttribute ("ChargeAmount"));
            eleAward.setAttribute("AwardType", "Shipping Discount");
            elePromotion.setAttribute("PromotionApplied", "Y");
            elePromotion.setAttribute("PromotionType", "Shipping Discount");
            elePromotion.setAttribute("Description", "Free shipping for orders over $29.99");
        }
		else
        {
            eleAward.setAttribute("AwardApplied", "N");
			eleAward.setAttribute("AwardAmount", "0");
            eleAward.setAttribute("DenialReason", "Order Total not over $29.99");
            elePromotion.setAttribute("PromotionApplied", "N");
            elePromotion.setAttribute("Description", "Not Available");
            elePromotion.setAttribute("DenialReason", "Order Total not over $29.99");
        }
        return eleAward;
    }

    protected YFCElement addAwardsFor100OrMore(YFSEnvironment env, YFCElement eleOrder, YFCElement elePromotion)
    {
		YFCElement	eleAwards = elePromotion.getChildElement("Awards");
		YFCElement	eleAward  = null;

		// first see if the award alredy exists
		if (eleAwards != null)
		{
			eleAward = eleAwards.getChildElement ("Award");
			if (eleAward != null)
				eleAward = getPromotionAward (eleOrder, eleAward.getAttribute ("AwardId"));
		}
		// if award doesn't exist create it		
		if (eleAward == null)
		{
			eleAwards = eleOrder.getChildElement ("Awards");
			if (eleAwards == null)
		        eleAwards = eleOrder.createChild("Awards");
    	    eleAward = eleAwards.createChild("Award");
            eleAward.setAttribute("AwardId", new String("" + System.currentTimeMillis()));
		}
        eleAward.setAttribute("PromotionId", elePromotion.getAttribute("PromotionId"));
        eleAward.setAttribute("Description", elePromotion.getAttribute("PromotionId") + " Promotion Applied To Order");

		// get line total on order.  Must be > $99.99 for promotion to be valid
        YFCElement eleOverallTotals = eleOrder.getChildElement("OverallTotals");
    	BigDecimal bdTotal = new BigDecimal ("0.00");
		if (eleOverallTotals != null)
		{
	        String sOrderTotal = eleOverallTotals.getAttribute("LineSubTotal");
        	if(sOrderTotal != null && sOrderTotal.trim().length() != 0)
            	bdTotal = new BigDecimal (sOrderTotal);
		}
		else
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("WARNING - tempate\\userexit\\orderRepricing.xml Template is Incomplete");
				System.out.println ("Missing the <OverallTotals> Element so We Can't determine if Order Reaches Promotion Requirement of $99.99");
				System.out.println ("Award Can't be Applied to the Order - please correct orderRepricing.xml user exit template");
			}
		}
		// get the line total for the order
        if(bdTotal.compareTo (new BigDecimal ("99.99")) > 0)
        {
            eleAward.setAttribute("AwardApplied", "Y");
            eleAward.setAttribute("AwardAmount", "10.00");
            eleAward.setAttribute("AwardType", "Order Total Discount");
            elePromotion.setAttribute("PromotionApplied", "Y");
            elePromotion.setAttribute("PromotionType", "Order Total Discount");
            elePromotion.setAttribute("Description", "$10 Discount for orders over $99.99");
        }
		else
        {
            eleAward.setAttribute("AwardApplied", "N");
			eleAward.setAttribute("AwardAmount", "0");
            eleAward.setAttribute("DenialReason", "Order Total not over $99.99");
            elePromotion.setAttribute("PromotionApplied", "N");
            elePromotion.setAttribute("Description", "Not Available");
            elePromotion.setAttribute("DenialReason", "Order Total not over $99.99");
        }
        return eleAward;
    }

	protected YFCElement addNotApplied(YFCElement eleOrder, YFCElement elePromotion)
    {
		YFCElement	eleAward = getPromotionAward (eleOrder, elePromotion.getAttribute ("PromotionId"));
		if (eleAward == null)
		{
			YFCElement	eleAwards = eleOrder.getChildElement ("Awards");
			if (eleAwards == null)
		        eleAwards = eleOrder.createChild("Awards");
    	    eleAward = eleAwards.createChild("Award");
            eleAward.setAttribute("AwardId", new String("" + System.currentTimeMillis()));
		}
        eleAward.setAttribute("PromotionId", elePromotion.getAttribute("PromotionId"));
        eleAward.setAttribute("Description", elePromotion.getAttribute("PromotionId") + " Promotion Applied To Order");
        elePromotion.setAttribute("PromotionApplied", "N");
        elePromotion.setAttribute("DenialReason", "Promotion "+ elePromotion.getAttribute ("PromotionId") + " is not a valid promotion code.");
        elePromotion.setAttribute("Description", "Requested Promotion Not Available");
        return eleOrder;
    }

	protected YFCElement addLineNotApplied(YFCElement eleOrderLine, YFCElement elePromotion)
    {
		YFCElement	eleAward = getLinePromotionAward (eleOrderLine, elePromotion.getAttribute ("PromotionId"));
		if (eleAward == null)
		{
			YFCElement	eleAwards = eleOrderLine.getChildElement ("Awards");
			if (eleAwards == null)
		        eleAwards = eleOrderLine.createChild("Awards");
    	    eleAward = eleAwards.createChild("Award");
            eleAward.setAttribute("AwardId", new String("" + System.currentTimeMillis()));
		}
        eleAward.setAttribute("PromotionId", elePromotion.getAttribute("PromotionId"));
        eleAward.setAttribute("Description", elePromotion.getAttribute("PromotionId") + " Promotion Applied To Order");
        elePromotion.setAttribute("PromotionApplied", "N");
        elePromotion.setAttribute("DenialReason", "Promotion "+ elePromotion.getAttribute ("PromotionId") + " is not a valid promotion code.");
        elePromotion.setAttribute("Description", "Requested Promotion Not Available");
        return eleOrderLine;
    }

	@SuppressWarnings("rawtypes")
	protected YFCElement	getPromotion (YFCElement eleOrder, String sPromotionId)
	{
		YFCElement	elePromotion = null;
				
		// lcoate the given promotion element
		YFCElement	elePromotions = eleOrder.getChildElement ("Promotions");
		if (elePromotions != null)
		{
			Iterator	iPromotions = elePromotions.getChildren ();
			while (iPromotions.hasNext())
			{
				YFCElement	eleTestPromotion = (YFCElement)iPromotions.next();
				if (sPromotionId.equalsIgnoreCase (eleTestPromotion.getAttribute ("PromotionId")))
				{
					elePromotion = eleTestPromotion;
				}
			}
		}
		return elePromotion;
	}		

	@SuppressWarnings("rawtypes")
	protected boolean	isRemovingPromotion(YFCElement eleOrder, String sPromotionId)
	{
		YFCElement	eleModificationTypes = eleOrder.getChildElement ("ModificationTypes");
		boolean		bRemovingPromotion = false;
				
		if (eleModificationTypes != null)
		{
			Iterator	iModificationTypes = eleModificationTypes.getChildren();
			while (iModificationTypes.hasNext ())
			{
				YFCElement	eleModification = (YFCElement)iModificationTypes.next();
				if (eleModification.getAttribute ("Name").equalsIgnoreCase("REMOVE_PROMOTION"))
				{
					// YFCElement	eleAward = getPromotionAward (eleOrder, sPromotionId);
					// bRemovingPromotion = (eleAward != null && "REMOVE".equalsIgnoreCase (eleAward.getAttribute ("Action")));
					YFCElement	elePromotion = getPromotion (eleOrder, sPromotionId);
					bRemovingPromotion = (elePromotion != null && "REMOVE".equalsIgnoreCase (elePromotion.getAttribute ("Action")));
				}
			}
		}
		return bRemovingPromotion;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement	getShippingDiscount (YFCElement eleOrder)
	{
		YFCElement	eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
		if (eleHeaderCharges != null)
		{
			Iterator	iHeaderCharges = eleHeaderCharges.getChildren ();
			while (iHeaderCharges.hasNext())
			{
				YFCElement	eleHeaderCharge = (YFCElement)iHeaderCharges.next();
				if ("PROMOTION".equalsIgnoreCase (eleHeaderCharge.getAttribute ("ChargeCategory")) && "Free Shipping".equalsIgnoreCase(eleHeaderCharge.getAttribute ("ChargeName")))
				{
					return eleHeaderCharge;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement	get100OrMoreDiscount (YFCElement eleOrder)
	{
		YFCElement	eleHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
		if (eleHeaderCharges != null)
		{
			Iterator	iHeaderCharges = eleHeaderCharges.getChildren ();
			while (iHeaderCharges.hasNext())
			{
				YFCElement	eleHeaderCharge = (YFCElement)iHeaderCharges.next();
				if ("PROMOTION".equalsIgnoreCase (eleHeaderCharge.getAttribute ("ChargeCategory")) && "100 Or More".equalsIgnoreCase(eleHeaderCharge.getAttribute ("ChargeName")))
				{
					return eleHeaderCharge;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement	get10PercentLineDiscount(YFCElement eleOrderLine)
	{
		YFCElement	eleLineCharges = eleOrderLine.getChildElement ("LineCharges");
		if (eleLineCharges != null)
		{
			Iterator	iLineCharges = eleLineCharges.getChildren ();
			while (iLineCharges.hasNext())
			{
				YFCElement	eleLineCharge = (YFCElement)iLineCharges.next();
				if ("PROMOTION".equalsIgnoreCase (eleLineCharge.getAttribute ("ChargeCategory")) && "10 Percent Off".equalsIgnoreCase(eleLineCharge.getAttribute ("ChargeName")))
				{
					return eleLineCharge;
				}
			}
		}
		return null;
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
		eleCommonCode.setAttribute ("CodeType", "SHIPPINGCHARGES");
		
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
				System.out.println ("SHIPPINGCHARGES Common Code Table Not Configured");
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
				if (sDeliveryMethod == null || !sDeliveryMethod.equals("PICK"))
				{
					// get the parcel method of shipment for the line if specified
					String	sShippingMethod = eleOrderLine.getAttribute ("CarrierServiceCode");
					
					// if no parcel method for line given
					if (YFCCommon.isVoid (sShippingMethod))
						// get the parcel method of shipment for the order if specified
						sShippingMethod = eleOrder.getAttribute ("CarrierServiceCode");

					// default to standard shipping method if none yet selected						
					if (YFCCommon.isVoid (sShippingMethod))
						sShippingMethod = "Y_STANDARD";
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
	
	@SuppressWarnings("rawtypes")
	protected boolean	isExistingPromotion(YFCElement eleOrder, YFCElement elePromotion, String sPromotionId)
	{
		boolean	bRet = false;
		
		if (elePromotion != null && sPromotionId.equals(elePromotion.getAttribute ("PromotionId")))
		{
			// see if an award element exists for this promotion
			YFCElement	eleAwards = elePromotion.getChildElement ("Awards");
			if (eleAwards != null)
			{
				Iterator	iAwards = eleAwards.getChildren ();
				while (iAwards.hasNext ())
				{
					YFCElement	eleAward = (YFCElement)iAwards.next();
					if (!YFCCommon.isVoid(eleAward.getAttribute ("PromotionId")) && sPromotionId.equals (eleAward.getAttribute ("PromotionId")))
					{
						bRet = true;
						break;
					}
				}
			}
		}
		return bRet;
	}

	@SuppressWarnings("rawtypes")
	protected YFCElement	getPromotionAward (YFCElement eleOrder, String sAwardId)
	{
		YFCElement	eleAward = null;

		// locate the free shipping award element				
		YFCElement	eleAwards = eleOrder.getChildElement ("Awards");
		if (eleAwards != null)
		{
			Iterator	iAwards = eleAwards.getChildren ();
			while (iAwards.hasNext())
			{
				YFCElement	eleTestAward = (YFCElement)iAwards.next();
				if (sAwardId.equalsIgnoreCase (eleTestAward.getAttribute ("AwardId")))
				{
					eleAward = eleTestAward;
				}
			}
		}	
		return eleAward;
	}


	@SuppressWarnings("rawtypes")
	protected YFCElement	getLinePromotionAward (YFCElement eleOrderLine, String sAwardId)
	{
		YFCElement	eleAward = null;

		// locate the free shipping award element				
		YFCElement	eleAwards = eleOrderLine.getChildElement ("Awards");
		if (eleAwards != null)
		{
			Iterator	iAwards = eleAwards.getChildren ();
			while (iAwards.hasNext())
			{
				YFCElement	eleTestAward = (YFCElement)iAwards.next();
				if (sAwardId.equalsIgnoreCase (eleTestAward.getAttribute ("AwardId")))
				{
					eleAward = eleTestAward;
				}
			}
		}	
		return eleAward;
	}
}

