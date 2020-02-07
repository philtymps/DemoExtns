/**
  * DendreonBeforeCreateOrderUEImpl.java
  *
  **/

// PACKAGE
package com.custom.dendreon.ue;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.ue.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;
import com.custom.yantra.util.*;

import java.util.*;

public class DendreonBeforeCreateOrderUEImpl  implements YFSBeforeCreateOrderUE
{
    public DendreonBeforeCreateOrderUEImpl()
    {
    }
	
	public String beforeCreateOrder(YFSEnvironment env, String inXML)
                                       throws YFSUserExitException
	{
		try {
			return YFSXMLUtil.getXMLString (beforeCreateOrder (env, YFCDocument.createDocument (inXML).getDocument()));
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	}
	
	public Document beforeCreateOrder(YFSEnvironment env, Document inXML)
                                       throws YFSUserExitException
	{
	  try {
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeCreateOrder() UE is: ");
				System.out.println (YFSXMLUtil.getXMLString (inXML));
		}

		// find any items missing item id's and use the line no from order to
		// get the corresponding item id
		YFCDocument	docOrder = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		String		sOrderType = eleOrder.getAttribute ("OrderType");

		// if not a provenge order type				
		if (sOrderType == null || !sOrderType.equals("PROVENGE"))
			return inXML;
		
		// now add pre-items to order
		int iLine = 1;
		YFCElement	eleOrderLines = eleOrder.createChild ("OrderLines");
		addOrderLine (env, eleOrderLines, "APHERESIS", "1", "EACH", iLine++);
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from beforeCreateOrder() UE is: ");
			System.out.println (YFSXMLUtil.getXMLString (docOrder.getDocument()));
		}
		return docOrder.getDocument();
	  } 
	  catch (YFSException e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }		
	  catch (Exception e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }		
	}	

	private void addOrderLine (YFSEnvironment env, YFCElement eleOrderLines, String sItemID, String sQty, String sUOM, int iLineNo) throws Exception
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Adding New Order Line: "+sItemID);
		}
		YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
		eleOrderLine.setAttribute ("ShipNode", "APH1");
		eleOrderLine.setAttribute ("ReceivingNode", "CPC");
		
		YFCElement	eleItem = eleOrderLine.createChild ("Item");
		YFCElement	eleOrderLineTranQuantity = eleOrderLine.createChild ("OrderLineTranQuantity");
		eleItem.setAttribute ("ItemID", sItemID);
		eleOrderLineTranQuantity.setAttribute ("TransactionalUOM", sUOM);
		eleOrderLineTranQuantity.setAttribute ("OrderedQty", sQty);
		eleOrderLine.setAttribute ("TransactionalLineId", "DID-"+Integer.toString (iLineNo));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("New Order Line Added: "+sItemID);
		}
		
		// add any associated services to the line
		addAdditionalServiceLines (env, eleOrderLines.getParentElement(), eleOrderLine);
	}

	@SuppressWarnings("rawtypes")
	private void addAdditionalServiceLines (YFSEnvironment env, YFCElement eleOrder, YFCElement eleOrderLine) throws Exception
	{			
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();								
		YFCDocument	docItemDetails = YFCDocument.getDocumentFor (eleOrderLine.getChildElement("Item").getString());
		YFCElement	eleItemDetails = docItemDetails.getDocumentElement ();
		eleItemDetails.setAttribute ("OrganizationCode", eleOrder.getAttribute ("EnterpriseCode"));
		eleItemDetails.setAttribute ("UnitOfMeasure", eleOrderLine.getChildElement("OrderLineTranQuantity").getAttribute ("TransactionalUOM"));

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Adding New Order Line Services: ");
		}
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("getItemDetails API Input: ");
			System.out.println (docItemDetails.getString());
		}
		docItemDetails = YFCDocument.getDocumentFor (api.getItemDetails (env, docItemDetails.getDocument()));					
		if (YFSUtil.getDebug ())
		{
			System.out.println ("getItemDetails API Output: ");
			System.out.println (docItemDetails.getString());
		}
		YFCElement	eleItemServiceAssocList = docItemDetails.getDocumentElement().getChildElement ("ItemServiceAssocList");

		// if additional service items found for this item
		if (eleItemServiceAssocList != null)
		{
			// set a trans id into the product order line
			YFCElement	eleProductServiceAssocs = eleOrder.createChild ("ProductServiceAssocs");
			int iService = 1;
			for (Iterator i = eleItemServiceAssocList.getChildren(); i.hasNext(); iService++)
			{
				YFCElement	eleItemServiceAssoc = (YFCElement)i.next();
				if (!eleItemServiceAssoc.getAttribute("ServiceItemGroupCode").equals("PS"))
					continue;
						
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Adding New Order Line Service: "+eleItemServiceAssoc.getAttribute ("ServiceItemId"));
				}

				// create a new order line containing the service item
				YFCElement	eleNewOrderLine = eleOrder.getChildElement("OrderLines").createChild ("OrderLine");
				eleNewOrderLine.setAttribute ("TransactionalLineId", eleOrderLine.getAttribute ("TransactionalLineId")+"-"+Integer.toString (iService));
				YFCElement	eleItem = eleNewOrderLine.createChild ("Item");
				eleItem.setAttribute ("ItemID", eleItemServiceAssoc.getAttribute ("ServiceItemId"));
				YFCElement	eleOrderLineTranQuantity = eleNewOrderLine.createChild ("OrderLineTranQuantity");
				eleOrderLineTranQuantity.setAttribute ("TransactionalUOM", eleItemServiceAssoc.getAttribute ("ServiceUOM"));
				eleOrderLineTranQuantity.setAttribute ("OrderedQty", eleItemServiceAssoc.getAttribute ("ServiceQuantity"));

				// add product/service association to order 
				YFCElement	eleProductServiceAssoc = eleProductServiceAssocs.createChild ("ProductServiceAssoc");
				YFCElement	eleProductLine = eleProductServiceAssoc.createChild("ProductLine");
				eleProductLine.setAttribute ("TransactionalLineId", eleOrderLine.getAttribute ("TransactionalLineId"));

				YFCElement	eleServiceLine = eleProductServiceAssoc.createChild("ServiceLine");
				eleServiceLine.setAttribute ("TransactionalLineId", eleNewOrderLine.getAttribute ("TransactionalLineId"));
			}
		}										
	}
}
