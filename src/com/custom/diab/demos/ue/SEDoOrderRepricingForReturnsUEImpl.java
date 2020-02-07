/**
  * SEDoOrderRepricingForReturnsUEImpl.java
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
import com.custom.diab.demos.ue.SEDoOrderRepricingForPromotionsUEImpl;


public class SEDoOrderRepricingForReturnsUEImpl extends SEDoOrderRepricingForPromotionsUEImpl implements YFSOrderRepricingUE 
{
    public SEDoOrderRepricingForReturnsUEImpl()
    {
    }

    public Document orderReprice(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
		// find the free shipping promotion element if it's already on the order
		YFCDocument	docReturnOrder = YFCDocument.getDocumentFor (docIn);

		dumpOrderDocument (docReturnOrder.getDocumentElement(), "Entering SEDoOrderRepricingForReturnsUEImpl:");

		// reprice the return order		
		try {
			repriceReturnOrder (env, docReturnOrder);				
		} catch (Exception e) {
			e.printStackTrace (System.out);
			throw new YFSUserExitException (e.getMessage())	;		
		} finally {
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Exiting SEDoOrderRepricingForReturnsUEImpl:");
		}
		return docReturnOrder.getDocument();
	}
	
	protected void repriceReturnOrder (YFSEnvironment env, YFCDocument docReturnOrder) throws Exception
	{
		//YFCDocument docReturnOrderDerivatives = getReturnOrderDerivatives (env, eleOrder.getAttribute ("OrderHeaderKey"));
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement();
		Hashtable<String, Hashtable<String, YFCElement>>	htOrdersAndLineKeys = getDerivedOrderAndLineKeys (eleReturnOrder);		
		Enumeration<String>	enumOrdersAndLineKeys = htOrdersAndLineKeys.keys();

		// remove any existing return fee's currently on the return order
		removeExistingChargeBacks (eleReturnOrder);
		
		// iterate over all the orders that are part of this return		
		while (enumOrdersAndLineKeys.hasMoreElements())
		{
			String			sOrderHeaderKey = (String)enumOrdersAndLineKeys.nextElement();
			Hashtable<?, ?>		htOrderLines = (Hashtable<?, ?>)htOrdersAndLineKeys.get(sOrderHeaderKey);

			// get the parent fisrt/next parent order associated to this return			
			YFCDocument	docParentOrder = getParentOrder (env, sOrderHeaderKey);
			
			// save the original award amounts
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Saving Original Award Amount");
			saveOriginalAwardAmounts (docParentOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Saving Original Award Amount");

			// remove the returned quantities fromt he original order
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Removing Returned Items");
			removeReturnedQuantitiesFromParentOrder (docParentOrder, htOrderLines);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Removing Returned Items");
			
			// recalculate overall totals on the original order
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Recalculating Overall Totals");
			recalculateOverallTotals (docParentOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Recalculating Overall Totals");
			
			// pass new order to repricing engine to determine the effects of the return on original order
			// each award element (header/line) will have the original award amount, the new award amount, and the
			// charge back for the return
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order Before Repricing");
			docParentOrder = YFCDocument.getDocumentFor (super.orderReprice (env, docParentOrder.getDocument()));
		
			// recalculate the total awards for each line and the award per unit		
			recalculateTotalAwards(docParentOrder);
			dumpOrderDocument (docParentOrder.getDocumentElement(), "Sales Order After Repricing");
			
			// charge-back promotion breaks
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order Before Charge Backs Applied");
			chargeBackPromotionBreaks (env, docReturnOrder, docParentOrder, htOrderLines);
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order After Charge Backs Applied");
			
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order Before Tax Credits Applied");
			applyTaxCredits (env, docReturnOrder, docParentOrder);
			dumpOrderDocument (docReturnOrder.getDocumentElement(), "Return Order After Tax Credits Applied");
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
	
	protected	void removeExistingChargeBacks (YFCElement eleReturnOrder)
	{
		YFCElement	eleChargeBack; 

		if ((eleChargeBack = getShippingDiscount(eleReturnOrder)) != null)
			eleChargeBack.setAttribute ("ChargeAmount", "0.00");

		if ((eleChargeBack = get100OrMoreDiscount (eleReturnOrder)) != null)
			eleChargeBack.setAttribute ("ChargeAmount", "0.00");
		YFCElement	eleOrderLines = eleReturnOrder.getChildElement ("OrderLines");
		Iterator<?>	iOrderLines = eleOrderLines.getChildren();
	
		while (iOrderLines.hasNext ())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();

			// charge back the order line promotions		
			if ((eleChargeBack = get10PercentLineDiscount (eleOrderLine)) != null)
				eleChargeBack.setAttribute ("ChargePerUnit", "0.00");
		}
	}
	
	protected	void chargeBackPromotionBreaks (YFSEnvironment env, YFCDocument docReturnOrder, YFCDocument docParentOrder, Hashtable<?, ?> htOrderLines)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleReturnOrder = docReturnOrder.getDocumentElement ();
		YFCNodeList<?> nodeAwardList = eleParentOrder.getElementsByTagName("Award");
		Iterator<?>	iAwardList = nodeAwardList.iterator();
		
		while (iAwardList.hasNext())
		{
			YFCElement	eleAward = (YFCElement)iAwardList.next();
		

			// if promotion was broken (i.e. AwardApplied="N" and WasAwardApplied="Y")
			if (!eleAward.getAttribute ("AwardApplied").equals (eleAward.getAttribute ("WasAwardApplied")))
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Promotion Broken - Award Details To Be Charged Back");
					System.out.println (eleAward.getString());
				}	
				if (eleAward.getAttribute ("PromotionId").equals (ID_FREESHIP))
				{
					// charge back free ship promotion
					dumpOrderDocument (eleReturnOrder, "Return Order Before Charge Back Shipping");
					chargeBackFreeShipAward (eleAward, eleParentOrder, eleReturnOrder);
					dumpOrderDocument (eleReturnOrder, "Return Order After Charge Back Shipping");
				}
				else if (eleAward.getAttribute ("PromotionId").equals (ID_100ORMORE))
				{
					// charge back 100 or more order discount
					dumpOrderDocument (eleReturnOrder, "Return Order Before Charge Back 100 Or More");
					chargeBack100OrMoreAward (eleAward, eleParentOrder, eleReturnOrder);	
					dumpOrderDocument (eleReturnOrder, "Return Order After Charge Back 100 Or More");
				}
				else if (eleAward.getAttribute ("PromotionId").equals (ID_10PERCENTOFF))
				{
					// charge back 10% discount 
					YFCElement	eleParentOrderLine = eleAward.getParentElement().getParentElement(); 
					YFCElement	eleReturnOrderLine = (YFCElement)htOrderLines.get(eleParentOrderLine.getAttribute ("OrderLineKey"));

					dumpOrderDocument (eleReturnOrder, "Return Order Before Charge Back 10 Percent");
					chargeBack10PercentOffLineAward (eleAward, eleParentOrderLine, eleReturnOrderLine);
					dumpOrderDocument (eleReturnOrder, "Return Order After Charge Back 10 Percent");
				}
			}
		}
	}

	protected	void chargeBack10PercentOffLineAward (YFCElement eleAward, YFCElement eleParentOrderLine, YFCElement eleReturnOrderLine)
	{
		YFCElement	eleParent10PercentCharge = get10PercentLineDiscount (eleParentOrderLine);
		YFCElement	eleReturn10PercentCharge = get10PercentLineDiscount (eleReturnOrderLine);
		YFCElement	eleReturnLineCharges;
		
		if (eleReturn10PercentCharge == null)
		{
			eleReturnLineCharges = eleReturnOrderLine.getChildElement ("LineCharges");
			if (eleReturnLineCharges == null)
				eleReturnLineCharges = eleReturnOrderLine.createChild ("LineCharges");
			eleReturn10PercentCharge = eleReturnLineCharges.createChild ("LineCharge");
		}

		// compute the discounted amount and add that back as a deduction	
		BigDecimal	bd10PercentCharge = new BigDecimal (eleParent10PercentCharge.getAttribute ("OriginalChargePerUnit"));
		//BigDecimal	bdUnits = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));
		//bd10PercentCharge = bdUnits.multiply (bd10PercentCharge);
					
		// add the discount charges on the return order
		eleReturn10PercentCharge.setAttribute ("ChargeCategory", eleParent10PercentCharge.getAttribute ("ChargeCategory"));
		eleReturn10PercentCharge.setAttribute ("ChargeName", eleParent10PercentCharge.getAttribute ("ChargeName"));
		eleReturn10PercentCharge.setAttribute ("ChargePerUnit", bd10PercentCharge.setScale (2, BigDecimal.ROUND_DOWN).toString());
	}
	
	protected	void chargeBackFreeShipAward (YFCElement eleAward, YFCElement eleParentOrder, YFCElement eleReturnOrder)
	{
					
		// charge back shipping costs
		YFCElement	eleParentCharge = getShippingDiscount (eleParentOrder);
		YFCElement	eleReturnCharge = getShippingDiscount (eleReturnOrder);
		YFCElement	eleReturnCharges;					
		String		sOriginalCharge;
		
		if (eleReturnCharge == null)
		{
			eleReturnCharges = eleReturnOrder.getChildElement ("HeaderCharges");
			if (eleReturnCharges == null)
				eleReturnCharges = eleReturnOrder.createChild ("HeaderCharges");
			eleReturnCharge = eleReturnCharges.createChild ("HeaderCharge");
			sOriginalCharge = "0.00";
		}
		else
		{
			sOriginalCharge = eleReturnCharge.getAttribute ("ChargeAmount");
		}

		// when a return is created from more than one order, it's possible that more than one shipping charge will apply
		// to the return.  for this reason, we add the charges for all orders on which the FREESHIP promotion was broken
		BigDecimal	bdCharge = new BigDecimal (sOriginalCharge);
		bdCharge = bdCharge.add (new BigDecimal (eleParentCharge.getAttribute ("OriginalCharge")));
					
		// set the shipping charges on the return order to correspond with aggregate shipping charges for all orders
		// for which the FREEESHIP was removed
		// NOTE:  THIS CODE ASSUMES THAT ONE SHIPMENT PER ORDER WAS MADE AND THAT SHIPPING CHARGES ARE CHARGED ONCE ON THE
		//        HEADER AND THAT ONLY ONE RETURN IS MADE AGAINST AN ORDER.  IF MULTIPLE RETURNS FOR THE SAME ORDER ARE CREATED
		//        THE SHIPPING CHARGES MAY BE CHARGED BACK A SECOND TIME
		eleReturnCharge.setAttribute ("ChargeCategory", eleParentCharge.getAttribute ("ChargeCategory"));
		eleReturnCharge.setAttribute ("ChargeName", eleParentCharge.getAttribute ("ChargeName"));
		eleReturnCharge.setAttribute ("ChargeAmount", bdCharge.setScale (2, BigDecimal.ROUND_DOWN).toString());
	}

	protected	void chargeBack100OrMoreAward (YFCElement eleAward, YFCElement eleParentOrder, YFCElement eleReturnOrder)
	{
					
		// charge back shipping costs
		YFCElement	eleParentCharge = get100OrMoreDiscount (eleParentOrder);
		YFCElement	eleReturnCharge = get100OrMoreDiscount (eleReturnOrder);
		YFCElement	eleReturnCharges;
					
		String		sOriginalCharge;

		if (eleReturnCharge == null)
		{
			eleReturnCharges = eleReturnOrder.getChildElement ("HeaderCharges");
			if (eleReturnCharges == null)
				eleReturnCharges = eleReturnOrder.createChild ("HeaderCharges");
			eleReturnCharge = eleReturnCharges.createChild ("HeaderCharge");
			sOriginalCharge = "0.00";
		}
		else
		{
			sOriginalCharge = eleReturnCharge.getAttribute ("ChargeAmount");
		}
		// when a return is created from more than one order, it's possible that more than one shipping charge will apply
		// to the return.  for this reason, we add the charges for all orders on which the FREESHIP promotion was broken
		BigDecimal	bdCharge = new BigDecimal (sOriginalCharge);
		bdCharge = bdCharge.add (new BigDecimal (eleParentCharge.getAttribute ("OriginalCharge")));
					
		// set the shipping charges on the return order to correspond with aggregate shipping charges for all orders
		// for which the FREEESHIP was removed
		eleReturnCharge.setAttribute ("ChargeCategory", eleParentCharge.getAttribute ("ChargeCategory"));
		eleReturnCharge.setAttribute ("ChargeName", eleParentCharge.getAttribute ("ChargeName"));
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
	
	protected	void recalculateTotalAwards (YFCDocument docParentOrder)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleParentOrderLines = eleParentOrder.getChildElement ("OrderLines");
		Iterator<?>	iOrderLines = eleParentOrderLines.getChildren();
		
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
	
	
	protected	void removeReturnedQuantitiesFromParentOrder (YFCDocument docParentOrder, Hashtable<?, ?> htOrderLines)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		Enumeration<?>	enumOrderLines = htOrderLines.keys();
		
		while (enumOrderLines.hasMoreElements ())
		{
			String          sDerivedFromOrderLineKey = (String)enumOrderLines.nextElement ();
			YFCElement		eleReturnOrderLine = (YFCElement)htOrderLines.get(sDerivedFromOrderLineKey);
			YFCElement		eleOrderLine = getCorrespondingOrderLine (eleParentOrder, sDerivedFromOrderLineKey);

			// if corresponding orderline found			
			if (eleOrderLine != null)
			{
				// get the orginal and return quantities from the respective orders
				BigDecimal		bdOrderedQty = new BigDecimal (eleOrderLine.getAttribute ("OrderedQty"));
				BigDecimal		bdReturnQty = new BigDecimal (eleReturnOrderLine.getAttribute ("OrderedQty"));
				BigDecimal		bdTotalAwards = new BigDecimal (getTotalAwards (eleOrderLine));
				BigDecimal		bdAwardPerUnit = bdTotalAwards.divide (bdOrderedQty);
			
				// decrement the original orderline quantity to reflect the quantity being returned
				eleOrderLine.setAttribute ("OriginalOrderedQty", eleOrderLine.getAttribute ("OrderedQty"));	
				eleOrderLine.setAttribute ("OriginalTotalAwards", bdTotalAwards.toString());
				eleOrderLine.setAttribute ("OriginalAwardPerUnit", bdAwardPerUnit.toString());
				eleOrderLine.setAttribute ("OrderedQty", bdOrderedQty.subtract (bdReturnQty).toString());
			}
		}
		return;
	}
	
	protected	void recalculateOverallTotals (YFCDocument	docParentOrder)
	{
		YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
		YFCElement	eleOrderLines = eleParentOrder.getChildElement ("OrderLines");
		Iterator<?>	iOrderLines = eleOrderLines.getChildren();
		BigDecimal	bdOverallSubTotal = new BigDecimal ("0.00");
		
		while (iOrderLines.hasNext ())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next ();
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
		
	protected YFCDocument	getParentOrder(YFSEnvironment env, String sOrderHeaderKey) throws Exception
	{
		// load the corresponding parent order using the pricing UE template so as to have a copy of the original
		// sales order with all promotions and awards applied, order and line totals, etc.
		YFCDocument	docParentOrder = null;
		
		if (sOrderHeaderKey != null)
		{
			//YFCDocument	docRepricingTemplate = YFCDocument.getDocumentFor (new File (getClass().getClassLoader().getResource ("/template/userexit/extn/orderRepricing.xml").getFile()));
			YFCDocument	docRepricingTemplate = YFCDocument.parse(getClass().getResourceAsStream("/template/userexit/extn/orderRepricing.xml"));
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
					
			docParentOrder = YFCDocument.createDocument ("Order");
			YFCElement	eleParentOrder = docParentOrder.getDocumentElement ();
			eleParentOrder.setAttribute ("OrderHeaderKey", sOrderHeaderKey);
					
			env.setApiTemplate ("getOrderDetails", docRepricingTemplate.getDocument());
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Geting Parent Sales Order for Return");
				System.out.println ("Input to getOrderDetails");
				System.out.println (docParentOrder.getString());
			}	
			docParentOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docParentOrder.getDocument()));
			env.clearApiTemplate ("getOrderDetails");
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Parent Sales Order for Return ");
				System.out.println ("Output from getOrderDetails");
				System.out.println (docParentOrder.getString());
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

