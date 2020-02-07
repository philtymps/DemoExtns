// Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 10/18/2007 11:12:54 AM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SEGetExternalPricesForItemListUEImpl.java

package com.custom.diab.demos.ue;

import com.custom.yantra.util.*;

import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetExternalPricesForItemListUE;
import com.yantra.pca.ycd.demo.YCDGetExternalPricesForItemListUEImpl;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.core.YFCObject;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;

import java.io.IOException;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


@SuppressWarnings({ "unused", "deprecation" })
public class SEGetExternalPricesForItemListUEImpl extends YCDGetExternalPricesForItemListUEImpl implements YFSGetExternalPricesForItemListUE 
{

    public SEGetExternalPricesForItemListUEImpl()
    {
    }

    @SuppressWarnings("rawtypes")
	public Document getExternalPricesForItemList(YFSEnvironment env, Document inDoc) throws YFSUserExitException
    {
		YFCDocument	docItemListOut = YFCDocument.getDocumentFor (super.getExternalPricesForItemList (env, inDoc));
//		YFCDocument	docItemListOut = YFCDocument.getDocumentFor (inDoc);
		YFCElement	eleItemListOut = docItemListOut.getDocumentElement ();
		YFCDocument	docItemListIn = YFCDocument.getDocumentFor (inDoc);
		YFCElement	eleItemListIn = docItemListIn.getDocumentElement ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering getExternalPricesForItemList:");
			System.out.println (docItemListIn.getString());
		}

		if (YFCObject.isVoid (eleItemListIn.getAttribute ("PriceProgramName"))
		&&  YFCObject.isVoid (eleItemListIn.getAttribute ("PriceProgramKey")))
		{
			// default price prgram key to the default for MATRIX-CORP if none given in input
			eleItemListIn.setAttribute ("PriceProgramKey", getDefaultPriceProgramKey (env, eleItemListIn.getAttribute("CallingOrganizationCode")));
		}

		Iterator	iItemListIn = eleItemListIn.getChildren ();
		// iterate ove the input list
		while (iItemListIn.hasNext ())
		{
			YFCElement	eleItem = (YFCElement)iItemListIn.next ();

			if (!IsAlreadyInOutputList (eleItemListOut, eleItem))
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Price for Item " + eleItem.getAttribute ("ItemID") + " Is Missing.  Will attempt to Compute using Price Lists" );
				}
				try {
					computePriceForItem (env, eleItemListIn, eleItemListOut, eleItem);
				} catch (Exception e) {
					throw new YFSUserExitException (e.getMessage());
				}
			}
		}		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting getExternalPricesForItemList:");
			System.out.println (docItemListOut.getString());
		}
		return docItemListOut.getDocument();
    }
	
	@SuppressWarnings("rawtypes")
	protected boolean	IsAlreadyInOutputList (YFCElement eleItemListOut, YFCElement eleItemToTest)
	{
		Iterator	iItemListOut = eleItemListOut.getChildren();
		while (iItemListOut.hasNext())
		{
			YFCElement	eleItem = (YFCElement)iItemListOut.next();
			if (eleItem.getAttribute ("ItemID").equals (eleItemToTest.getAttribute ("ItemID"))
			&&  eleItem.getAttribute ("UnitOfMeasure").equals (eleItemToTest.getAttribute ("UnitOfMeasure")))
				return true;		
		}
		return false;
	}
	
	protected void computePriceForItem (YFSEnvironment env, YFCElement eleItemListIn, YFCElement eleItemListOut, YFCElement eleItem) throws Exception
	{

			YFCDocument	docComputePriceForItem = YFCDocument.createDocument ("ComputePriceForItem");
			YFCElement	eleComputePriceForItem = docComputePriceForItem.getDocumentElement();
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
						
			
			eleComputePriceForItem.setAttribute ("OrganizationCode", eleItemListIn.getAttribute ("CallingOrganizationCode"));
			eleComputePriceForItem.setAttribute ("Currency", eleItemListIn.getAttribute ("Currency"));
			eleComputePriceForItem.setAttribute ("PriceProgramName", eleItemListIn.getAttribute ("PriceProgramName"));
			eleComputePriceForItem.setAttribute ("PriceProgramKey", eleItemListIn.getAttribute ("PriceProgramKey"));
			eleComputePriceForItem.setAttribute ("PricingDate", eleItemListIn.getAttribute ("PricingDate"));

			if (eleItem.getChildElement ("PrimaryInformation") != null)
				eleComputePriceForItem.setAttribute ("ProductClass", eleItem.getChildElement ("PrimaryInformation").getAttribute ("DefaultProductClass"));
			
			eleComputePriceForItem.setAttribute ("ItemID", eleItem.getAttribute ("ItemID"));
			eleComputePriceForItem.setAttribute ("ItemGroupCode", eleItem.getAttribute ("ItemGroupCode"));
			String		sQty = "1";
			String		sUOM = eleItem.getAttribute ("UnitOfMeasure");
		
			if (eleItem.getAttribute ("ItemGroupCode").equals ("DS") || eleItem.getAttribute ("ItemGroupCode").equals ("PS"))
			{
				YFCElement	eleServiceAssoc = eleItem.getChildElement ("ItemServiceAssoc");
				if (eleServiceAssoc != null && (eleServiceAssoc.getAttribute ("ServiceQuantity") != null || !eleServiceAssoc.getAttribute ("ServiceQuantity").equals("0")))
				{
					sQty = eleServiceAssoc.getAttribute ("ServiceQuantity");
					sUOM = eleServiceAssoc.getAttribute ("ServiceUom");
				}		
			}
			eleComputePriceForItem.setAttribute ("Uom", sUOM);
			eleComputePriceForItem.setAttribute ("Quantity", sQty);
		
			// call Compute Price for Item API
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to computePriceForItem() for Item");
				System.out.println (docComputePriceForItem.getString());
			}

			docComputePriceForItem = YFCDocument.getDocumentFor (api.computePriceForItem (env, docComputePriceForItem.getDocument()));				
			eleComputePriceForItem = docComputePriceForItem.getDocumentElement ();
			
			// parse output and add price to item
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from computePriceForItem() for Item");
				System.out.println (docComputePriceForItem.getString());
			}
			
			YFCElement	eleItemPriced = eleItemListOut.createChild ("Item");
			eleItemPriced.setAttribute ("OrganizationCode", eleItem.getAttribute ("OrganizationCode"));
			eleItemPriced.setAttribute ("ItemID", eleItem.getAttribute ("ItemID"));
			eleItemPriced.setAttribute ("UnitOfMeasure", eleItem.getAttribute ("UnitOfMeasure"));
			YFCElement	eleComputedPrice = eleItemPriced.createChild ("ComputedPrice");
			eleComputedPrice.setAttribute ("UnitPrice", eleComputePriceForItem.getAttribute ("UnitPrice"));
			eleComputedPrice.setAttribute ("Retailprice", eleComputePriceForItem.getAttribute ("RetailPrice"));
			eleComputedPrice.setAttribute ("ListPrice", eleComputePriceForItem.getAttribute ("ListPrice"));
			YFCElement	eleComputedPriceBreaks = eleComputePriceForItem.getChildElement ("ItemPriceSetDtl");

			if (eleComputedPriceBreaks != null)
			{
				YFCElement	eleItemPricedPriceBreaks = eleComputedPrice.createChild ("QuantityRangePrice");
				eleItemPricedPriceBreaks.setAttribute ("BreakQtyHigh", eleComputedPrice.getAttribute ("BreakQtyHigh"));
				eleItemPricedPriceBreaks.setAttribute ("BreakQtyLow", eleComputedPrice.getAttribute ("BreakQtyLow"));
				eleItemPricedPriceBreaks.setAttribute ("UnitPrice", eleComputedPrice.getAttribute ("UnitPrice"));
			}
	}

	protected	String getDefaultPriceProgramKey (YFSEnvironment env, String sCallingOrganization)
	{
	  String		sPriceProgramKey = "2007102010220739505";
	  YFCDocument	docOrganization = YFCDocument.createDocument ("Organization");
	  YFCElement	eleOrganization = docOrganization.getDocumentElement();
	  YFCDocument	docOrganizationTemplate = YFCDocument.getDocumentFor ("<Organization OrganizationCode=\"\" DefaultPriceProgramKey=\"\"/>");

	  if (sCallingOrganization != null)
	  {	
		eleOrganization.setAttribute ("OrganizationCode", sCallingOrganization);
		env.setApiTemplate ("getOrganizationHierarchy", docOrganizationTemplate.getDocument());
		
		// call Compute Price for Item API
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getOrganizationHierarchy:");
			System.out.println (docOrganization.getString());
		}
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
			docOrganization = YFCDocument.getDocumentFor (api.getOrganizationHierarchy (env, docOrganization.getDocument()));
		} catch (Exception ignore) {}
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getOrganizationHierarchy:");
			System.out.println (docOrganization.getString());
		}
		env.clearApiTemplate ("getOrganizationHierarchy");
		
		eleOrganization = docOrganization.getDocumentElement();
		if (!YFCObject.isVoid (eleOrganization.getAttribute ("DefaultPriceProgramKey")))
		{
			sPriceProgramKey = eleOrganization.getAttribute ("DefaultPriceProgramKey");
		}
	  }
	  return sPriceProgramKey;		
	}

}

