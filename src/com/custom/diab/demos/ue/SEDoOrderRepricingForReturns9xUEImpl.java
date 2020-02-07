/**
  * SEDoOrderRepricingForReturns9xUEImpl.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.ue;

import com.yantra.yfc.core.YFCObject;
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

public class SEDoOrderRepricingForReturns9xUEImpl implements YFSOrderRepricingUE 
{
	// required charge categories.  Any Promotion, Coupon or Appeasement must be made to these respective charge categories
	// these categories must be set up on DocumentType=0001 (Sales Order) as Discounts and on DocumentType=0003 (Returns) as Fees
	protected	final String	ID_PROMOTION_CATEGORY	= "PROMOTION";
	protected	final String	ID_COUPON_CATEGORY		= "COUPON";
	protected	final String	ID_APPEASEMENT_CATEGORY	= "CUSTOMER_APPEASEMENT";

	// required charge names for each of the above charge categories
	protected	final String	ID_SHIPORDER			= "SHIPORDER";
	protected	final String	ID_ORDERTOTAL			= "ORDERTOTAL";
	protected	final String	ID_ITEMCOMBO			= "ITEMCOMBO";
	protected	final String	ID_ITEMQTY				= "ITEMQTY";
	protected	final String	ID_SHIPSURCHARGE		= "SHIPSURCHARGE";
	protected	final String	ID_APPEASEMENT			= "CUSTOMER_APPEASEMENT";

    public SEDoOrderRepricingForReturns9xUEImpl()
    {
    }

    public Document orderReprice(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
		// find the free shipping promotion element if it's already on the order
		YFCDocument	docReturnOrder = YFCDocument.getDocumentFor (docIn);

		dumpOrderDocument (docReturnOrder.getDocumentElement(), "Entering SEDoOrderRepricingForReturns9xUEImpl:");

		// reprice the return order		
		try {
			repriceReturnOrder (env, docReturnOrder);				
		} catch (Exception e) {
			e.printStackTrace (System.out);
			throw new YFSUserExitException (e.getMessage())	;		
		} finally {
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Exiting SEDoOrderRepricingForReturns9xUEImpl:");
		}
		return docReturnOrder.getDocument();
	}
	
	protected void repriceReturnOrder (YFSEnvironment env, YFCDocument docReturnOrder) throws Exception
	{
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement();
		Hashtable<String, Hashtable<String, YFCElement>>	htOrdersAndLineKeys = getDerivedOrderAndLineKeys (eleReturnOrder);		
		Enumeration<String>	enumOrdersAndLineKeys = htOrdersAndLineKeys.keys();

		// iterate over all the orders that are part of this return		
		while (enumOrdersAndLineKeys.hasMoreElements())
		{
			String			sOrderHeaderKey = (String)enumOrdersAndLineKeys.nextElement();
			Hashtable<?, ?>		htOrderLines = (Hashtable<?, ?>)htOrdersAndLineKeys.get(sOrderHeaderKey);
			Hashtable<String, ?>		htFeesAlreadyAssessed;
			
			// get the parent fisrt/next parent order associated to this return using the re-price order template			
			YFCDocument	docParentOrder = getParentOrder (env, sOrderHeaderKey);
			
			// get the same order using the temp order template (used to create a new temporary order)
			YFCDocument docTempOrder   = getTempOrder (env, sOrderHeaderKey);
			
			// remove any quantities already returned from the original parent order.  Existing fees returned in a Hashtable
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Removing Already Returned Items");
			htFeesAlreadyAssessed = removeAlreadyReturnedQuantitiesFromOrder (docParentOrder, docTempOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Removing Already Returned Items");

			// if the parent order already has returns against it
			if (htFeesAlreadyAssessed != null)
			{
				// re-price the original order having now removed those already returned quantities and any existing awards
				dumpOrderDocument (docParentOrder.getDocumentElement(), "Temporary Order Required Because Returns Exist for the Sales Order");
				docParentOrder = YFCDocument.getDocumentFor (repriceOrder (env, docTempOrder));
				dumpOrderDocument (docParentOrder.getDocumentElement(), "Temporary Order Repriced and Now Represents the Parent Order");
			}

			// save the original award amounts
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Saving Original Award Amount");
			saveOriginalAwardAmounts (docParentOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Saving Original Award Amount");

			// remove the quantities of the items being returned from the temporary order
			dumpOrderDocument (docTempOrder.getDocumentElement(), "Sales Order Before Removing Newly Returned Items");
			removeReturnedQuantitiesFromOrder (docTempOrder, htOrderLines);
			dumpOrderDocument (docTempOrder.getDocumentElement(), "Sales Order After Removing Newly Returned Items");
/*			
			// recalculate overall totals on the original order - This code may needed if WSC doesn't recalculate these totals
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Recalculating Overall Totals");
			recalculateOverallTotals (docParentOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Recalculating Overall Totals");
*/
			// pass new order to re-pricing engine to determine the effects of the return on original order
			// each award element (header/line) will have the original award amount, the new award amount, and the
			// charge back for the return
			dumpOrderDocument (docTempOrder.getDocumentElement(), "Sales Order Before Repricing");
			docTempOrder = YFCDocument.getDocumentFor (repriceOrder (env, docTempOrder));

			// recalculate the total awards for each line and the award per unit		
			recalculateTotalAwards(docTempOrder);
			recalculateTotalAwards(docParentOrder);
			dumpOrderDocument (docTempOrder.getDocumentElement(), "Sales Order After Repricing");
			
			// charge-back promotion breaks
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order Before Charge Backs Applied");
			chargeBackPromotionBreaks (env, docReturnOrder, docParentOrder, docTempOrder, htOrderLines);
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order After Charge Backs Applied");
			
			// apply tax credits
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order Before Tax Credits Applied");
			applyTaxCredits (env, docReturnOrder, docTempOrder);
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order After Tax Credits Applied");

			// apply fee credits
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order Before Fee Credits Applied");
			if (htFeesAlreadyAssessed != null)
				applyFeeCredits (env, docReturnOrder, htFeesAlreadyAssessed);
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order After Fee Credits Applied");
		}
	}

	protected Document	repriceOrder (YFSEnvironment env, YFCDocument docOrder) throws Exception
	{
		// create a temporary draft order based on the original order - NOTE: assumes draft order pricing enabled
		return(createTempDraftOrder (env, docOrder).getDocument());		
	}

	
	protected	void applyFeeCredits (YFSEnvironment env, YFCDocument docReturnOrder, Hashtable<String, ?> htFeesAssessed)
	{
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement();
		Enumeration<String>	enumFeesAssessedKeys = htFeesAssessed.keys();

		
		// iterate over all the orders that are part of this return		
		while (enumFeesAssessedKeys.hasMoreElements())
		{
			String		sDataKey = (String)enumFeesAssessedKeys.nextElement();
			YFCElement	eleReturnOrderOrLine = getCorrespondingReturnOrderOrLine (eleReturnOrder, sDataKey);
			Boolean		bIsOrderLine = eleReturnOrderOrLine.getParentElement().getParentElement().getNodeName().equals ("OrderLine");
			
			if (eleReturnOrderOrLine != null)
			{
				String		sFeesAssessed = (String)htFeesAssessed.get (sDataKey);
				String		sFees[] = sFeesAssessed.split(",");
				String		sChargeName = sFees[1];
				String		sChargeCategory = bIsOrderLine ? "LineCharges" : "HeaderCharges";
				BigDecimal	bdFee = new BigDecimal (sFees[0]);
				
				YFCElement	eleCharge = getPromotionOrCoupon (eleReturnOrderOrLine, sChargeName, sChargeCategory);
				BigDecimal	bdCharge = new BigDecimal (eleCharge.getAttribute("ChargeAmount"));
				bdCharge = bdCharge.subtract(bdFee);
				eleCharge.setAttribute("ChargeAmount", bdCharge.toString());
			}
			
		}
	}
	
	protected	void applyTaxCredits (YFSEnvironment env, YFCDocument docReturnOrder, YFCDocument docParentOrder)
	{
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement ();
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleReturnOrderLines = eleReturnOrder.getChildElement ("OrderLines");
		Iterator<?>	iReturnOrderLines = eleReturnOrderLines.getChildren ();
				
		while (iReturnOrderLines.hasNext ())
		{
			YFCElement	eleReturnOrderLine = (YFCElement)iReturnOrderLines.next();
			YFCElement	eleParentOrderLine = getCorrespondingOrderLine (eleParentOrder, eleReturnOrderLine.getAttribute("DerivedFromOrderLineKey"));

			// if corresponding return line is not from this parent order			
			if (eleParentOrderLine == null)
				continue;
				
			YFCElement	eleParentLineTaxes = eleParentOrderLine.getChildElement ("LineTaxes");
			if (eleParentLineTaxes != null)
			{
				Iterator<?>	iLineTaxes = eleParentLineTaxes.getChildren ();
				while (iLineTaxes.hasNext ())
				{
					YFCElement	eleParentLineTax = (YFCElement)iLineTaxes.next ();
					YFCElement	eleReturnLinePriceInfo = eleReturnOrderLine.getChildElement ("LinePriceInfo");
					BigDecimal	bdTaxPercentage = new BigDecimal(eleParentLineTax.getAttribute ("TaxPercentage")).divide(new BigDecimal("100"));
					BigDecimal	bdReturnQty = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));
					BigDecimal	bdUnitPrice = new BigDecimal (eleReturnLinePriceInfo.getAttribute ("UnitPrice"));
					BigDecimal	bdExtendedPrice = bdReturnQty.multiply (bdUnitPrice);
					BigDecimal	bdDiscountPerUnit = new BigDecimal (eleParentOrderLine.getAttribute ("AwardPerUnit"));
					BigDecimal	bdExtendedDiscount = bdReturnQty.multiply (bdDiscountPerUnit);
					BigDecimal	bdTaxableAmount = bdExtendedPrice.subtract (bdExtendedDiscount);
					BigDecimal	bdTax = bdTaxableAmount.multiply(bdTaxPercentage).setScale (2, BigDecimal.ROUND_HALF_UP);
					
					YFCElement	eleReturnLineTaxes = eleReturnOrderLine.getChildElement ("LineTaxes");
					YFCElement	eleReturnLineTax = getLineTax (eleReturnLineTaxes, eleParentLineTax.getAttribute ("TaxName"));
					if (eleReturnLineTax == null)
					{
						if (eleReturnLineTaxes == null)						
							eleReturnLineTaxes = eleReturnOrderLine.createChild ("LineTaxes");
						eleReturnLineTax = eleReturnLineTaxes.createChild ("LineTax");
					}
					eleReturnLineTax.setAttribute ("ChargeCategory", eleParentLineTax.getAttribute ("ChargeCategory"));
					eleReturnLineTax.setAttribute ("TaxName", eleParentLineTax.getAttribute ("TaxName"));
					eleReturnLineTax.setAttribute ("TaxPercentage", eleParentLineTax.getAttribute ("TaxPercentage"));
					eleReturnLineTax.setAttribute ("Tax", bdTax.toString());
				}
			}
		}
		
	}
	
	protected	YFCElement getLineTax (YFCElement eleLineTaxes, String sTaxName)
	{
		if (eleLineTaxes != null)
		{
			Iterator<?>	iLineTaxes = eleLineTaxes.getChildren ();
			while (iLineTaxes.hasNext ())
			{
				YFCElement	eleLineTax = (YFCElement)iLineTaxes.next();
				if (sTaxName.equals (eleLineTax.getAttribute ("TaxName")))
					return eleLineTax;
			}
		}
		return null;
	}
	
	protected	void chargeBackPromotionBreaks (YFSEnvironment env, YFCDocument docReturnOrder, YFCDocument docParentOrder, YFCDocument docTempOrder, Hashtable<?, ?> htOrderLines)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleTempOrder   = docTempOrder.getDocumentElement();
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement ();
		YFCNodeList<?> nodeAwardList  = eleParentOrder.getElementsByTagName("Award");
		Iterator<?>	iAwardList = nodeAwardList.iterator();
		
		while (iAwardList.hasNext())
		{
			YFCElement	eleAward = (YFCElement)iAwardList.next();
			YFCElement	eleOrderOrOrderLine = eleAward.getParentElement().getParentElement(); 
			YFCElement	eleTempAward; 
			Boolean		bIsOrderLineAward = eleOrderOrOrderLine.getNodeName().equals("OrderLine");
			Boolean		bIsPromotionBroken;
			
			if (bIsOrderLineAward)
				eleTempAward = getCorrespondingAward (eleTempOrder, eleOrderOrOrderLine.getAttribute("OrderLineKey"), eleAward);
			else
				eleTempAward = getCorrespondingAward (eleTempOrder, eleOrderOrOrderLine.getAttribute ("OrderHeaderKey"), eleAward);
			
			// if award still applies after re-pricing
			if (eleTempAward != null)
			{
				eleAward.setAttribute("AwardApplied", eleTempAward.getAttribute("AwardApplied"));
				eleAward.setAttribute("AwardAmount", eleTempAward.getAttribute("AwardAmount"));
				
				BigDecimal	bdOriginalAwardAmount = new BigDecimal (eleAward.getAttribute("OriginalAwardAmount"));
				BigDecimal	bdAwardAmount = new BigDecimal (eleAward.getAttribute ("AwardAmount"));

				// the promotion is broken if the award applied or award amount changes
				bIsPromotionBroken = !eleAward.getAttribute ("AwardApplied").equals (eleAward.getAttribute ("WasAwardApplied")) || !bdOriginalAwardAmount.equals(bdAwardAmount);
				// NOTE:  You may wish to add Notes (Line or Header) to explain each promotion that is broken.  
				//		  To do so simply add Note elements to eleOrderOrOrderLine Element
				if (bIsPromotionBroken)
					// e.g.   NoteText="Promotion Awarded Reduced by " + eleAward.getAttribute("OriginalAwardAmount") - eleAward.getAttribute ("AwardAmount")
					System.out.println ("Award Amount Changed - Old Amount: " + eleAward.getAttribute("OriginalAwardAmount") + " New Amount: " + eleAward.getAttribute ("AwardAmount"));
			}
			else
			{
				// the promotion was removed from the re-priced order all together
				eleAward.setAttribute ("AwardAmount", "0.00");
				eleAward.setAttribute ("AwardApplied", "N");
				bIsPromotionBroken = true;
				// e.g.   NoteText="Order or Order Line No Longer Qualifies for Discount of " +  eleAward.getAttribute("OriginalAwardAmount")
				System.out.println ("Award Lost and No Longer Applies");	
			}
			
			// if promotion was broken (i.e. AwardApplied="N" and WasAwardApplied="Y" or no Corresponding Award Found on Temp Order)
			if (bIsPromotionBroken)
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Promotion Broken - Award Details To Be Charged Back");
					System.out.println (eleAward.getString());
				}	
				if (eleAward.getAttribute ("ChargeName").equals (ID_SHIPORDER) || eleAward.getAttribute ("ChargeName").equals (ID_ORDERTOTAL)
				||  eleAward.getAttribute ("ChargeName").equals (ID_ITEMQTY)   || eleAward.getAttribute ("ChargeName").equals(ID_ITEMCOMBO)
				||  eleAward.getAttribute ("ChargeName").equals (ID_APPEASEMENT))
				{
					// charge back any shipping promotions
					dumpOrderDocument (eleReturnOrder, "Return Order Before Charge Back of " + eleAward.getAttribute("ChargeName"));
					chargeBackAward (eleAward, eleParentOrder, eleReturnOrder, bIsOrderLineAward);
					dumpOrderDocument (eleReturnOrder, "Return Order After Charge Back  of " + eleAward.getAttribute("ChargeName"));
				}
				else if (eleAward.getAttribute ("ChargeName").equals(ID_SHIPSURCHARGE))
				{
					// charge back any shipping promotions
					dumpOrderDocument (eleReturnOrder, "Return Order Before Refund of " + eleAward.getAttribute("ChargeName"));
					refundSurchargeAward (eleAward, eleParentOrder, eleReturnOrder, bIsOrderLineAward);
					dumpOrderDocument (eleReturnOrder, "Return Order After Refund of "  + eleAward.getAttribute("ChargeName"));
				}
			}
		}
	}

	protected	void refundSurchargeAward (YFCElement eleAward, YFCElement eleParentOrderOrLine, YFCElement eleReturnOrder, boolean bIsOrderLineAward)
	{
			chargeBackAward (eleAward, eleParentOrderOrLine, eleReturnOrder, bIsOrderLineAward, true);
	}
	
	protected	void chargeBackAward (YFCElement eleAward, YFCElement eleParentOrderOrLine, YFCElement eleReturnOrder, boolean bIsOrderLineAward)
	{
		chargeBackAward (eleAward, eleParentOrderOrLine, eleReturnOrder, bIsOrderLineAward, false);
		
	}
	protected	void chargeBackAward (YFCElement eleAward, YFCElement eleParentOrderOrLine, YFCElement eleReturnOrder, boolean bIsOrderLineAward, boolean bIsSurCharge)
	{
					
		// charge back a promotion on the return
		YFCElement	eleReturnCharge = getPromotionOrCoupon (eleReturnOrder, eleAward.getAttribute ("ChargeName"), bIsOrderLineAward);
		YFCElement	eleReturnOrderOrLine;
		YFCElement	eleReturnCharges;					
		String		sOriginalCharge;
		String		sCharges, sCharge;
		
		if (bIsOrderLineAward)
		{
			sCharges="LineCharges";
			sCharge ="LineCharge";
		}
		else
		{
			sCharges="HeaderCharges";
			sCharge ="HeaderCharge";
		}
		if (YFCObject.isVoid(eleAward.getAttribute ("OrderHeaderKey")))
			eleReturnOrderOrLine = getCorrespondingReturnOrderOrLine (eleReturnOrder, eleAward.getAttribute("OrderLineKey"));
		else
			eleReturnOrderOrLine = eleReturnOrder;

		if (eleReturnCharge == null)
		{
			eleReturnCharges = eleReturnOrderOrLine.getChildElement (sCharges);
			if (eleReturnCharges == null)
				eleReturnCharges = eleReturnOrderOrLine.createChild (sCharges);
			eleReturnCharge = eleReturnCharges.createChild (sCharge);
			sOriginalCharge = "0.00";
		}
		else
		{
			sOriginalCharge = eleReturnCharge.getAttribute ("ChargeAmount");
		}

		// when a return is created from more than one order, it's possible that more than one broken promotion may apply
		// to the return.  for this reason, we add the charges for all orders on which the promotion was broken
		BigDecimal	bdOriginalChargeAmount = new BigDecimal (eleAward.getAttribute ("OriginalAwardAmount")).abs();
		BigDecimal	bdCharge = new BigDecimal (0);

		// if the applied before and it still applies now - (i.e. award amount amount unchanged or has changed)
		if (eleAward.getBooleanAttribute("WasAwardApplied")  && eleAward.getBooleanAttribute("AwardApplied"))
		{

			// reduce the charge back by the difference between the original award amount and the new award amount
			if (bIsSurCharge)
			{
				bdCharge = bdCharge.subtract (new BigDecimal (eleAward.getAttribute ("AwardAmount")).abs());
				bdCharge = bdOriginalChargeAmount.add(bdCharge);
				
			}
			else
			{
				bdCharge = bdCharge.add (new BigDecimal (eleAward.getAttribute ("AwardAmount")).abs());
				bdCharge = bdOriginalChargeAmount.subtract (bdCharge);
				
			}
		}
		// else if promotion no longer applies (removed)	
		else if (eleAward.getBooleanAttribute("WasAwardApplied"))
		{
			// set the charge back to the original award amount
			if(bIsSurCharge)
				bdCharge = bdOriginalChargeAmount.subtract(bdCharge);
			else
				bdCharge = bdOriginalChargeAmount.add(bdCharge);
		}
		
		//  NOTE: if returning an item results in a new award all together, we would need a new charge category to assess additional credits
		//		  to the return.  This credit could be applied here inside one more else branch 

		// add the overall charge amount for this particular broken promotion fee
		bdCharge = bdCharge.add(new BigDecimal (sOriginalCharge));
		
		// set the charge back on the return order to correspond with aggregate charges for all orders
		// for which the promotion was removed
		eleReturnCharge.setAttribute ("ChargeCategory", eleAward.getAttribute ("ChargeCategory"));
		eleReturnCharge.setAttribute ("ChargeName", eleAward.getAttribute ("ChargeName"));
		eleReturnCharge.setAttribute ("ChargeAmount", bdCharge.setScale (2, BigDecimal.ROUND_DOWN).toString());

	}

	protected	void saveOriginalAwardAmounts (YFCDocument docParentOrder)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCNodeList<?> nodeAwardsList = eleParentOrder.getElementsByTagName("Award");
		
		for(int i = 0; i < nodeAwardsList.getLength(); i++)
		{
			YFCElement	eleAward = (YFCElement)nodeAwardsList.item(i);
			eleAward.setAttribute ("OriginalAwardAmount", eleAward.getAttribute ("AwardAmount"));
			eleAward.setAttribute ("WasAwardApplied", eleAward.getAttribute ("AwardApplied"));
		}
		return;
	}
	
	protected	void recalculateTotalAwards (YFCDocument docOrder)
	{
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		Iterator<?>	iOrderLines = eleOrderLines.getChildren();
		
		while (iOrderLines.hasNext ())
		{
			YFCElement	eleParentOrderLine = (YFCElement)iOrderLines.next();
			
			BigDecimal	bdOrderedQty = new BigDecimal (eleParentOrderLine.getAttribute ("OrderedQty"));
			BigDecimal	bdTotalAwards = new BigDecimal (getTotalAwards (eleParentOrderLine));
			BigDecimal	bdAwardPerUnit;
			if (bdOrderedQty.compareTo (BigDecimal.ZERO) != 0)
				bdAwardPerUnit = bdTotalAwards.divide (bdOrderedQty);	
			else
				bdAwardPerUnit = new BigDecimal ("0.00");
			eleParentOrderLine.setAttribute ("TotalAwards", bdTotalAwards.toString());
			eleParentOrderLine.setAttribute ("AwardPerUnit", bdAwardPerUnit.toString());
		}
	}
		
	protected	void removeReturnedQuantitiesFromOrder (YFCDocument docOrder, Hashtable<?, ?> htOrderLines)
	{
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		Enumeration<?>	enumOrderLines = htOrderLines.keys();
		
		while (enumOrderLines.hasMoreElements ())
		{
			String          sDerivedFromOrderLineKey = (String)enumOrderLines.nextElement ();
			YFCElement		eleReturnOrderLine = (YFCElement)htOrderLines.get(sDerivedFromOrderLineKey);
			YFCElement		eleOrderLine = getCorrespondingOrderLine (eleOrder, sDerivedFromOrderLineKey);

			// if corresponding order line found			
			if (eleOrderLine != null)
			{
				// get the return quantities from the respective order lines
				BigDecimal		bdOrderedQty = new BigDecimal (eleOrderLine.getAttribute ("OrderedQty"));
				BigDecimal		bdReturnQty = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));

				//BigDecimal		bdTotalAwards = new BigDecimal (getTotalAwards (eleOrderLine));
				//BigDecimal		bdAwardPerUnit = bdTotalAwards.divide (bdOrderedQty);
					
				// decrement any line level Award Values based on line being removed
				//eleOrderLine.setAttribute ("OriginalTotalAwards", bdTotalAwards.toString());
				//eleOrderLine.setAttribute ("OriginalAwardPerUnit", bdAwardPerUnit.toString());

				// decrement the original order line quantity to reflect the quantity being returned
				eleOrderLine.setAttribute ("OriginalOrderedQty", eleOrderLine.getAttribute ("OrderedQty"));	
				eleOrderLine.setAttribute ("OrderedQty", bdOrderedQty.subtract (bdReturnQty).toString());
				
			}
			else
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("WARNING - Return Order Line with No Corresponding Order Line Found");
				}
			}
		}
		return;
	}

	protected	Hashtable<String, ?> removeAlreadyReturnedQuantitiesFromOrder (YFCDocument docParentOrder, YFCDocument docTempOrder)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleTempOrder = docTempOrder.getDocumentElement();
		YFCElement	eleReturnOrders = eleParentOrder.getChildElement ("ReturnOrders");
		Hashtable<String, ?>	htFeesAssessedToReturns = null;
		
		Iterator<?>	iReturnOrders = eleReturnOrders.getChildren(); 
		
		while (iReturnOrders.hasNext())
		{
			YFCElement	eleReturnOrder = (YFCElement) iReturnOrders.next();
			Iterator<?> 	iReturnOrderLines = eleReturnOrder.getChildElement("OrderLines").getChildren();
			
			if (htFeesAssessedToReturns == null)
				htFeesAssessedToReturns = new Hashtable<String, Object>();
			
			accumulateReturnFees (eleReturnOrder, htFeesAssessedToReturns, false);

			while(iReturnOrderLines.hasNext())
			{
				YFCElement	eleReturnOrderLine = (YFCElement)iReturnOrderLines.next();
				YFCElement	eleParentOrderLine, eleTempOrderLine;
				
				eleParentOrderLine = getCorrespondingOrderLine (eleParentOrder, eleReturnOrderLine.getAttribute ("DerivedFromOrderLineKey"));
				eleTempOrderLine   = getCorrespondingOrderLine (eleTempOrder,   eleReturnOrderLine.getAttribute ("DerivedFromOrderLineKey"));

				// if the order line from the existing return is for any of the lines on the current order
				if (eleParentOrderLine != null && eleTempOrderLine != null)
				{
					// if returns already exist for the order line decrement those quantities as well
					BigDecimal		bdReturnQty  = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));
					BigDecimal		bdOrderedQty = new BigDecimal (eleParentOrderLine.getAttribute ("OrderedQty"));
					BigDecimal		bdNewQty;

					bdReturnQty = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));
					bdNewQty = bdOrderedQty.subtract (bdReturnQty);
					eleParentOrderLine.setAttribute ("OrderedQty", bdNewQty.toString());
					eleTempOrderLine.setAttribute ("OrderedQty", bdNewQty.toString());
							
					accumulateReturnFees (eleReturnOrderLine, htFeesAssessedToReturns, true);				
				}
			}
		}
		return htFeesAssessedToReturns;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void	accumulateReturnFees (YFCElement eleReturnOrderOrLine, Hashtable htFeesAssessedToReturns, boolean bIsReturnLine)
	{
		String		sFeesElement = bIsReturnLine ? "LineCharges" : "HeaderCharges";
		Iterator<?>	iFeesAssessed = eleReturnOrderOrLine.getChildren (sFeesElement);
		
		while (iFeesAssessed.hasNext())
		{
			YFCElement	eleFeeAssessed = (YFCElement)iFeesAssessed.next();
			String		sChargeCategory = eleFeeAssessed.getAttribute("ChargeCategory");
			
			// NOTE: all assessed fees for promotion breaks must be in the same category as the promotions themselves
			if (ID_PROMOTION_CATEGORY.equals(sChargeCategory) || ID_APPEASEMENT_CATEGORY.equals(sChargeCategory))
			{
				String		sDataKey = bIsReturnLine ? eleReturnOrderOrLine.getAttribute ("DerivedOrderLineKey") : eleReturnOrderOrLine.getAttribute("DerivedOrderHeaderKey");
				String		sFeesAssessed = (String)htFeesAssessedToReturns.get(sDataKey);
				
				BigDecimal	bdFeesAssessedOverallTotal = new BigDecimal (0);

				// if fees already assessed for this order or order line being returned
				if (sFeesAssessed != null)
				{
					String	sFeeParts[] = sFeesAssessed.split (",");
					bdFeesAssessedOverallTotal.add(new BigDecimal (sFeeParts[0]));
				}

				// add the charge amount assessed as fee to the overall fees for the line or header
				bdFeesAssessedOverallTotal.add(new BigDecimal(eleFeeAssessed.getAttribute("ChargeAmount")));
				htFeesAssessedToReturns.put(sDataKey, bdFeesAssessedOverallTotal.toString() + "," + eleFeeAssessed.getAttribute("ChargeName"));					
			}
		}
		
		return;
	}
	
	/*
	protected	void recalculateOverallTotals (YFCDocument	docParentOrder)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleOrderLines = eleParentOrder.getChildElement ("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
		BigDecimal	bdOverallSubTotal = new BigDecimal ("0.00");
		
		while (iOrderLines.hasNext ())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next ();
			YFCElement	eleLineOverallTotals = eleOrderLine.getChildElement ("LineOverallTotals");
			YFCElement	eleLinePriceInfo = eleOrderLine.getChildElement ("LinePriceInfo");
			BigDecimal	bdOrderedQty = new BigDecimal (eleOrderLine.getAttribute ("OrderedQty"));
			BigDecimal	bdUnitPrice  = new BigDecimal (eleLinePriceInfo.getAttribute ("UnitPrice"));
			BigDecimal	bdExtendedPrice = bdOrderedQty.multiply (bdUnitPrice).setScale (2, BigDecimal.ROUND_DOWN);
			
			eleLinePriceInfo.setAttribute ("OriginalExtendedPrice", eleLinePriceInfo.getAttribute ("ExtendedPrice"));
			eleLinePriceInfo.setAttribute ("ExtendedPrice", bdExtendedPrice.toString());
			bdOverallSubTotal = bdOverallSubTotal.add (bdExtendedPrice);	
		}
		YFCElement	eleOverallTotals = eleParentOrder.getChildElement ("OverallTotals");
		eleOverallTotals.setAttribute ("LineSubTotal", bdOverallSubTotal.toString());	
	}
*/	
	protected	String	getTotalAwards (YFCElement eleOrderOrLine)
	{
		YFCNodeList<?> nodeAwardsList = eleOrderOrLine.getElementsByTagName("Award");
		BigDecimal	bdAwardTotal = new BigDecimal ("0.00");
		
		for(int i = 0; i < nodeAwardsList.getLength(); i++)
		{
			YFCElement	eleAward = (YFCElement)nodeAwardsList.item(i);
			if (eleAward.getAttribute ("AwardApplied").equals ("Y"))
				bdAwardTotal = bdAwardTotal.add (new BigDecimal (eleAward.getAttribute ("OriginalAwardAmount")));
		}
		return bdAwardTotal.toString();
	}

	protected	YFCElement	getCorrespondingAward (YFCElement eleOrder, String sDataKey, YFCElement eleAwardToFind)
	{
		YFCNodeList<?> nodeAwardsList = eleOrder.getElementsByTagName("Award");

		for(int i = 0; i < nodeAwardsList.getLength(); i++)
		{
			YFCElement	eleAward = (YFCElement)nodeAwardsList.item(i);
			if (eleAward.getAttribute("ChargeCategory").equals(eleAwardToFind.getAttribute ("ChargeCategory")) && eleAward.getAttribute("ChargeName").equals(eleAwardToFind.getAttribute ("ChargeName"))
			&&  eleAward.getAttribute ("OrderHeaderKey").equals(sDataKey) || eleAward.getAttribute ("OrderLineKey").equals(sDataKey))
				return eleAward;
		}
		return null;
	}
	
	protected	YFCElement	getCorrespondingOrderLine (YFCElement eleParentOrder, String sOrderLineKey)
	{
		YFCElement	eleOrderLines = eleParentOrder.getChildElement ("OrderLines");
		
		if (eleOrderLines != null)
		{
			Iterator<?>	iOrderLines = eleOrderLines.getChildren ();
			while (iOrderLines.hasNext ())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next ();
				if (sOrderLineKey.equals(eleOrderLine.getAttribute ("OrderLineKey")))
					return eleOrderLine;
			}
		}
		return null;
	}

	protected	YFCElement	getCorrespondingReturnOrderOrLine (YFCElement eleReturnOrder, String sOrderOrLineKey)
	{
		
		if (!eleReturnOrder.getAttribute ("OrderHeaderKey").equals(sOrderOrLineKey))
		{	
			YFCNodeList<?> nodeOrderLineList = eleReturnOrder.getElementsByTagName("OrderLine");
			if (nodeOrderLineList != null)
			{
				for(int i = 0; i < nodeOrderLineList.getLength(); i++)
				{
					YFCElement	eleOrderLine = (YFCElement)nodeOrderLineList.item(i);
					
					if (sOrderOrLineKey.equals(eleOrderLine.getAttribute ("DerivedFromOrderLineKey")) || sOrderOrLineKey.equals(eleOrderLine.getAttribute ("OrderLineKey")))
					{
						return eleOrderLine;
					}
				}
			}
			return null;
		}
		return eleReturnOrder;
	}

	protected YFCElement	getOrderTotalDiscount(YFCElement eleOrderOrLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, ID_ORDERTOTAL, bIsLineCharges);
	}

	protected YFCElement	getItemComboDiscount(YFCElement eleOrderOrLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, ID_ITEMCOMBO, bIsLineCharges);
	}

	protected YFCElement	getShipOrderDiscount(YFCElement eleOrderOrLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, ID_SHIPORDER, bIsLineCharges);
	}

	protected YFCElement	getShipSurchargeDiscount(YFCElement eleOrderLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderLine, ID_SHIPSURCHARGE, bIsLineCharges);
	}

	protected YFCElement	getItemQtyDiscount(YFCElement eleOrderOrLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, ID_ITEMQTY, bIsLineCharges);
	}

	protected YFCElement	getPromotionOrCoupon (YFCElement eleOrderOrLine, String sChargeName, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, sChargeName, bIsLineCharges ? "LineCharges" : "HeaderCharges");
	}

	protected YFCElement	getAppeasementDiscount(YFCElement eleOrderOrLine, boolean bIsLineCharges)
	{
		return getPromotionOrCoupon (eleOrderOrLine, ID_APPEASEMENT, bIsLineCharges);
	}
	
	protected YFCElement	getPromotionOrCoupon(YFCElement eleOrderOrLine, String sChargeName, String sHeaderOrLineCharges)
	{
		YFCElement	eleCharges = eleOrderOrLine.getChildElement (sHeaderOrLineCharges);
		if (eleCharges != null)
		{
			Iterator<?>	iCharges = eleCharges.getChildren ();
			while (iCharges.hasNext())
			{
				YFCElement	eleCharge = (YFCElement)iCharges.next();
				String		sChargeCategory = eleCharge.getAttribute ("ChargeCategory");
				if (ID_PROMOTION_CATEGORY.equalsIgnoreCase (sChargeCategory) || ID_COUPON_CATEGORY.equalsIgnoreCase(sChargeCategory) || ID_APPEASEMENT_CATEGORY.equalsIgnoreCase(sChargeCategory))
				{
					if (sChargeName.equalsIgnoreCase(eleCharge.getAttribute ("ChargeName")))
					{
						return eleCharge;
					}
				}
			}
		}
		return null;
	}

	protected	Hashtable<String, Hashtable<String, YFCElement>> getDerivedOrderAndLineKeys (YFCElement	eleReturnOrder)
	{
		YFCElement	eleReturnOrderLines = eleReturnOrder.getChildElement ("OrderLines");
		Hashtable<String, Hashtable<String, YFCElement>>	htOrders = new Hashtable<String, Hashtable<String, YFCElement>>();
		
		// NOTE this logic assumes the return being created is for items on a single order
		
		// rebuild the original sales order with the returned items removed from the original order
		if (eleReturnOrderLines != null)
		{
			Iterator<?>	iReturnOrderLines = eleReturnOrderLines.getChildren();
			
			while(iReturnOrderLines.hasNext())
			{
				YFCElement	eleReturnOrderLine = (YFCElement)iReturnOrderLines.next();
				String		sDerivedFromOrderHeaderKey = eleReturnOrderLine.getAttribute ("DerivedFromOrderHeaderKey");
				
				if (!YFCCommon.isVoid (sDerivedFromOrderHeaderKey))
				{
					Hashtable<String, YFCElement>		htOrderLines = (Hashtable<String, YFCElement>)htOrders.get (sDerivedFromOrderHeaderKey);
				
					if (htOrderLines == null)
					{
						htOrderLines = new Hashtable<String, YFCElement>();
						htOrders.put (sDerivedFromOrderHeaderKey, htOrderLines);
					}
					htOrderLines.put (eleReturnOrderLine.getAttribute ("DerivedFromOrderLineKey"), eleReturnOrderLine);
				}
			}
		}	
		return htOrders;
	}
		
	protected YFCDocument	createTempDraftOrder (YFSEnvironment env, YFCDocument docOrder) throws Exception
	{
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
		YFCDocument	docRepricedOrder = null;
		YFCDocument	docRepricingTemplate = YFCDocument.parse(getClass().getResourceAsStream("/global/template/userexit/extn/orderRepricing.xml"));
		YFCElement	eleOrder = docOrder.getDocumentElement();

		// replace the OrderHeader and OrderLineKeys and Order Number with temporary values
		eleOrder.setAttribute("OrderHeaderKey", "R"+eleOrder.getAttribute ("OrderHeaderKey"));
		eleOrder.setAttribute("OrderNo", "R"+eleOrder.getAttribute ("OrderNo"));
		eleOrder.setAttribute("DraftOrderFlag", "Y");
		
		YFCNodeList<?> nodeOrderLineList = eleOrder.getElementsByTagName("OrderLine");
		
		for(int i = 0; i < nodeOrderLineList.getLength(); i++)
		{
			YFCElement	eleOrderLine = (YFCElement)nodeOrderLineList.item(i);
			eleOrderLine.setAttribute ("OrderLineKey", "R"+eleOrderLine.getAttribute ("OrderLineKey"));
			eleOrderLine.setDateAttribute("PricingDate", eleOrder.getDateAttribute ("ActualPricingDate"));
		}
				
		// fist remove any existing header awards
		YFCNodeList<?> nodeAwardList = eleOrder.getElementsByTagName("Award");
		Iterator<?>	iAwardList = nodeAwardList.iterator();
		
		while (iAwardList.hasNext())
		{
			YFCElement	eleAward = (YFCElement)iAwardList.next();
			boolean	bIsOrderAward = "Order".equals(eleAward.getParentElement().getParentElement().getNodeName());
			boolean	bIsLineAward  = "OrderLine".equals(eleAward.getParentElement().getParentElement().getNodeName());
			if (bIsOrderAward)	
				eleAward.setAttribute ("OrderHeaderKey", "R"+eleAward.getAttribute ("OrderHeaderKey"));
			else if (bIsLineAward)
				eleAward.setAttribute ("OrderLineKey", "R"+eleAward.getAttribute ("OrderLineKey"));			
		}
				
		// create a temporary draft order IMPORTANT NOTE must have pricing of Draft Orders Enabled!
		env.setApiTemplate ("createOrder", docRepricingTemplate.getDocument());
		try {
			docRepricedOrder = getParentOrder(env, api.createOrder (env, docOrder.getDocument()).getDocumentElement().getAttribute ("OrderHeaderKey"));
		} catch (Exception e) {
			throw e;
		} finally {
			env.clearApiTemplate ("getOrderDetails");			
		}
		YFCElement eleRepricedOrder = docRepricedOrder.getDocumentElement();

		// delete the temporary order Note: assumes DELETE modification type is allowed for Draft Orders
		eleRepricedOrder.setAttribute("Action", "DELETE");
		api.deleteOrder(env, docRepricedOrder.getDocument());

		// restore the original OrderHeader and OrderLineKeys on the re-priced order
		eleRepricedOrder.setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey").substring(1));
		eleRepricedOrder.setAttribute("OrderNo", eleOrder.getAttribute ("OrderNo").substring(1));
		nodeOrderLineList = eleOrder.getElementsByTagName("OrderLine");
		
		for(int i = 0; i < nodeOrderLineList.getLength(); i++)
		{
			YFCElement	eleOrderLine = (YFCElement)nodeOrderLineList.item(i);
			eleOrderLine.setAttribute ("OrderLineKey", eleOrderLine.getAttribute ("OrderLineKey").substring(1));
		}
		
		nodeAwardList = eleRepricedOrder.getElementsByTagName("Award");
		iAwardList = nodeAwardList.iterator();
		
		while (iAwardList.hasNext())
		{
			YFCElement	eleAward = (YFCElement)iAwardList.next();
			boolean	bIsOrderAward = "Order".equals(eleAward.getParentElement().getParentElement().getNodeName());
			boolean	bIsLineAward  = "OrderLine".equals(eleAward.getParentElement().getParentElement().getNodeName());
			if (bIsOrderAward)	
				eleAward.setAttribute ("OrderHeaderKey", eleAward.getAttribute ("OrderHeaderKey").substring(1));
			else if (bIsLineAward)
				eleAward.setAttribute ("OrderLineKey", eleAward.getAttribute ("OrderLineKey").substring(1));			
		}

		return docRepricedOrder;
	}
	
	protected YFCDocument	getParentOrder(YFSEnvironment env, String sOrderHeaderKey) throws Exception
	{
		return (getOrderDetails (env, sOrderHeaderKey, "/global/template/userexit/extn/orderRepricing.xml"));
	}

	protected YFCDocument	getTempOrder(YFSEnvironment env, String sOrderHeaderKey) throws Exception
	{
		return (getOrderDetails (env, sOrderHeaderKey, "/global/template/userexit/extn/orderRepricing_createTempOrder.xml"));
	}

	protected YFCDocument	getOrderDetails (YFSEnvironment env, String sOrderHeaderKey, String sOutputTemplate) throws Exception
	{
		// load the corresponding parent order using the pricing UE template so as to have a copy of the original
		// sales order with all promotions and awards applied, order and line totals, etc.
		YFCDocument	docParentOrder = null;
		
		if (sOrderHeaderKey != null)
		{
			YFCDocument	docRepricingTemplate = YFCDocument.parse(getClass().getResourceAsStream(sOutputTemplate));
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
					
			docParentOrder = YFCDocument.createDocument ("Order");
			YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
			eleParentOrder.setAttribute ("OrderHeaderKey", sOrderHeaderKey);
					
			env.setApiTemplate ("getCompleteOrderDetails", docRepricingTemplate.getDocument());
			try {
				docParentOrder = YFCDocument.getDocumentFor (api.getCompleteOrderDetails (env, docParentOrder.getDocument()));
			} catch (Exception e){
				throw e;
			} finally {
				env.clearApiTemplate ("getCompleteOrderDetails");				
			}
		}
		return docParentOrder;
	}

	protected	void dumpOrderDocument (YFCElement eleOrder, String sContext)
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println (sContext);
			System.out.println (eleOrder.getString());
		}
	}

}
